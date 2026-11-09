import "jsr:@supabase/functions-js/edge-runtime.d.ts";

const QURANENC_TRANSLATIONS = [
  {
    key: "english_saheeh",
    identifier: "english_saheeh",
    language: "en",
    name: "English Translation",
    translator: "Saheeh International",
    publisher: "Noor International Center",
    version: "1.1.2",
  },
  {
    key: "somali_yacob",
    identifier: "somali_yacob",
    language: "so",
    name: "Somali Translation",
    translator: "Abdullah Hasan Yaqoub",
    publisher: "QuranEnc.com",
    version: "1.0.27",
  },
  {
    key: "turkish_rwwad",
    identifier: "turkish_rwwad",
    language: "tr",
    name: "Turkish Translation",
    translator: "Rowwad Translation Center",
    publisher: "Rowwad Translation Center / QuranEnc.com",
    version: "1.0.4",
  },
  {
    key: "french_rashid",
    identifier: "french_rashid",
    language: "fr",
    name: "French Translation",
    translator: "Rachid Maach",
    publisher: "QuranEnc.com",
    version: "1.0.3",
  },
] as const;

async function fetchQuranEncEdition(
  surah: number,
  expectedAyahCount: number,
  translation: typeof QURANENC_TRANSLATIONS[number],
): Promise<QuranEdition> {
  const response = await fetch(
    `https://quranenc.com/api/v1/translation/sura/${translation.key}/${surah}`,
    { headers: { "Accept": "application/json" }, signal: AbortSignal.timeout(12000) },
  );
  if (!response.ok) throw new Error(`QuranEnc ${translation.key} returned ${response.status}`);
  const body = await response.json() as {
    result?: Array<{ aya: number; translation: string; footnotes?: string }>;
  };
  if (!Array.isArray(body.result) || body.result.length !== expectedAyahCount) {
    throw new Error(`QuranEnc ${translation.key} returned an incomplete surah`);
  }

  return {
    edition: {
      identifier: translation.identifier,
      language: translation.language,
      name: translation.name,
      englishName: translation.translator,
      publisher: translation.publisher,
      version: translation.version,
      sourceName: "QuranEnc.com",
      sourceUrl: "https://quranenc.com",
      licenseTerms: "Republish verbatim; retain publisher/source, version and transcript information; do not modify, add or delete content.",
      format: "text",
      type: "translation",
    },
    ayahs: body.result.map((row) => ({
      numberInSurah: Number(row.aya),
      text: row.translation,
      footnotes: row.footnotes ?? "",
    })),
  };
}
const JSON_HEADERS = {
  "Content-Type": "application/json; charset=utf-8",
  "Cache-Control": "public, max-age=900, s-maxage=3600",
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

type AyahRow = { ayah_number: number; text: string };
type ArabicRow = { ayah_number: number; text_uthmani: string };
type QuranEdition = {
  edition?: { identifier?: string; [key: string]: unknown };
  ayahs?: Array<{ numberInSurah: number; text: string; [key: string]: unknown }>;
  [key: string]: unknown;
};

async function databaseRows<T>(projectUrl: string, apiKey: string, path: string): Promise<T[]> {
  const headers: Record<string, string> = {
    "apikey": apiKey,
    "Accept": "application/json",
  };
  // A legacy anon key is a JWT. A publishable key is not a JWT and must not be
  // sent as a bearer token; the API gateway identifies it from the apikey header.
  if (apiKey.startsWith("eyJ")) headers["Authorization"] = `Bearer ${apiKey}`;

  const response = await fetch(`${projectUrl}/rest/v1/${path}`, { headers });
  if (!response.ok) throw new Error(`Supabase content query failed: ${response.status}`);
  return await response.json() as T[];
}

function jsonResponse(body: unknown, status = 200, extraHeaders: Record<string, string> = {}) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...JSON_HEADERS, ...extraHeaders },
  });
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: JSON_HEADERS });
  if (req.method !== "GET") return jsonResponse({ error: "Method not allowed" }, 405);

  const raw = new URL(req.url).searchParams.get("surah");
  const surah = Number(raw);
  if (!Number.isInteger(surah) || surah < 1 || surah > 114) {
    return jsonResponse({ error: "surah must be an integer from 1 to 114" }, 400);
  }

  const projectUrl = Deno.env.get("SUPABASE_URL");
  const apiKey = Deno.env.get("SUPABASE_ANON_KEY") ?? req.headers.get("apikey");
  let databaseArabic: ArabicRow[] | null = null;
  let expectedAyahCount: number | null = null;
  const databaseTranslations: QuranEdition[] = [];
  let databaseHasCompleteTranslations = false;

  // Supabase is primary. A surah is accepted only when every ayah is present,
  // preventing a partial import from replacing a complete upstream edition.
  if (projectUrl && apiKey) {
    try {
      const [surahRows, ayahRows] = await Promise.all([
        databaseRows<{ ayah_count: number }>(
          projectUrl,
          apiKey,
          `quran_surahs?select=ayah_count&surah_number=eq.${surah}`,
        ),
        databaseRows<ArabicRow>(
          projectUrl,
          apiKey,
          `quran_ayahs?select=ayah_number,text_uthmani&surah_number=eq.${surah}&order=ayah_number.asc`,
        ),
      ]);

      const expectedAyahs = surahRows[0]?.ayah_count;
      expectedAyahCount = expectedAyahs ?? null;
      if (expectedAyahs && ayahRows.length === expectedAyahs) {
        databaseArabic = ayahRows;

        const editions = await databaseRows<{
          id: string;
          edition_slug: string;
          language_code: string;
          language_name: string;
          translator_name: string;
        }>(
          projectUrl,
          apiKey,
          "quran_translations?select=id,edition_slug,language_code,language_name,translator_name&is_active=eq.true",
        );

        const translationResults = await Promise.all(editions.map(async (edition): Promise<QuranEdition | null> => {
          try {
            const rows = await databaseRows<AyahRow>(
              projectUrl,
              apiKey,
              `quran_ayah_translations?select=ayah_number,text&surah_number=eq.${surah}&translation_id=eq.${edition.id}&order=ayah_number.asc`,
            );
            if (rows.length !== expectedAyahs) return null;
            return {
              edition: {
                identifier: edition.edition_slug,
                language: edition.language_code,
                name: edition.language_name,
                englishName: edition.translator_name,
                format: "text",
                type: "translation",
              },
              ayahs: rows.map((row) => ({
                numberInSurah: row.ayah_number,
                text: row.text,
              })),
            };
          } catch {
            return null;
          }
        }));

        for (const result of translationResults) {
          if (result) databaseTranslations.push(result);
        }
        databaseHasCompleteTranslations =
          editions.length > 0 && databaseTranslations.length === editions.length;
      }
    } catch (error) {
      console.warn("Quran database unavailable; using upstream backup.", error);
    }
  }

  let payload: { code?: number; status?: string; data?: QuranEdition[] };
  if (databaseArabic && databaseHasCompleteTranslations) {
    // A complete database copy is authoritative; no upstream request is needed.
    payload = { code: 200, status: "OK", data: [] };
  } else {
    const translationResults = expectedAyahCount
      ? await Promise.allSettled(
          QURANENC_TRANSLATIONS.map((translation) =>
            fetchQuranEncEdition(surah, expectedAyahCount as number, translation)
          ),
        )
      : [];
    const onlineTranslations = translationResults
      .filter((result): result is PromiseFulfilledResult<QuranEdition> => result.status === "fulfilled")
      .map((result) => result.value);

    if (onlineTranslations.length === 0 && !databaseArabic) {
      console.error("No complete Quran source available.");
      return jsonResponse({ error: "Quran text service is temporarily unavailable" }, 502, {
        "Cache-Control": "no-store",
      });
    }
    payload = { code: 200, status: "OK", data: onlineTranslations };
  }

  const editions = payload.data ?? [];
  const arabicFromDatabase: QuranEdition | null = databaseArabic
    ? {
        edition: {
          identifier: "quran-uthmani",
          language: "ar",
          name: "Quran Uthmani",
          format: "text",
          type: "quran",
        },
        ayahs: databaseArabic.map((row) => ({
          numberInSurah: row.ayah_number,
          text: row.text_uthmani,
        })),
      }
    : null;

  const merged: QuranEdition[] = editions.filter((edition) =>
    edition.edition?.identifier !== "quran-uthmani"
  );
  if (arabicFromDatabase) merged.unshift(arabicFromDatabase);

  // Complete database translations replace upstream editions. Missing or partial
  // translations continue to use the upstream edition as a backup.
  for (const translation of databaseTranslations) {
    const identifier = translation.edition?.identifier;
    if (typeof identifier !== "string") continue;
    const existingIndex = merged.findIndex((edition) =>
      edition.edition?.identifier === identifier
    );
    if (existingIndex >= 0) merged[existingIndex] = translation;
    else merged.push(translation);
  }

  const finalArabic = merged.find((edition) =>
    edition.edition?.identifier === "quran-uthmani"
  );
  if (!finalArabic || !Array.isArray(finalArabic.ayahs)) {
    return jsonResponse({ error: "Quran text response failed validation" }, 502, {
      "Cache-Control": "no-store",
    });
  }

  return jsonResponse({
    ...payload,
    code: 200,
    status: "OK",
    data: merged,
  });
});
