import "jsr:@supabase/functions-js/edge-runtime.d.ts";

const ALLOWED_EDITIONS = "quran-uthmani,en.sahih,so.abduh,tr.diyanet,fr.hamidullah";
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
    try {
      const upstream = await fetch(
        `https://api.alquran.cloud/v1/surah/${surah}/editions/${ALLOWED_EDITIONS}`,
        { headers: { "Accept": "application/json" }, signal: AbortSignal.timeout(12000) },
      );
      if (!upstream.ok) throw new Error(`Upstream Quran API returned ${upstream.status}`);
      payload = await upstream.json() as { code?: number; status?: string; data?: QuranEdition[] };
      if (!Array.isArray(payload?.data)) throw new Error("Invalid upstream Quran response");
    } catch (error) {
      if (!databaseArabic) {
        console.error("No complete Quran source available.", error);
        return jsonResponse({ error: "Quran text service is temporarily unavailable" }, 502, {
          "Cache-Control": "no-store",
        });
      }
      payload = { code: 200, status: "OK", data: [] };
    }
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
