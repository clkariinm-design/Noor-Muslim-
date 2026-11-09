import "jsr:@supabase/functions-js/edge-runtime.d.ts";

const ALLOWED_EDITIONS = "quran-uthmani,en.sahih,so.abduh,tr.diyanet,fr.hamidullah";

Deno.serve(async (req: Request) => {
  const raw = new URL(req.url).searchParams.get("surah");
  const surah = Number(raw);

  if (!Number.isInteger(surah) || surah < 1 || surah > 114) {
    return new Response(JSON.stringify({ error: "surah must be an integer from 1 to 114" }), {
      status: 400,
      headers: { "Content-Type": "application/json" },
    });
  }

  try {
    const upstream = await fetch(
      `https://api.alquran.cloud/v1/surah/${surah}/editions/${ALLOWED_EDITIONS}`,
      { headers: { "Accept": "application/json" } },
    );

    if (!upstream.ok) {
      return new Response(JSON.stringify({ error: "Quran text provider is temporarily unavailable" }), {
        status: 502,
        headers: { "Content-Type": "application/json", "Cache-Control": "no-store" },
      });
    }

    const payload = await upstream.json();
    const editions = payload?.data;
    const arabic = Array.isArray(editions)
      ? editions.find((edition: { edition?: { identifier?: string } }) =>
          edition?.edition?.identifier === "quran-uthmani")
      : null;

    if (!arabic || !Array.isArray(arabic.ayahs)) {
      return new Response(JSON.stringify({ error: "Quran text response failed validation" }), {
        status: 502,
        headers: { "Content-Type": "application/json", "Cache-Control": "no-store" },
      });
    }

    return new Response(JSON.stringify(payload), {
      status: 200,
      headers: {
        "Content-Type": "application/json; charset=utf-8",
        "Cache-Control": "public, max-age=3600, s-maxage=86400",
        "Access-Control-Allow-Origin": "*",
      },
    });
  } catch {
    return new Response(JSON.stringify({ error: "Unable to reach Quran text provider" }), {
      status: 502,
      headers: { "Content-Type": "application/json", "Cache-Control": "no-store" },
    });
  }
});
