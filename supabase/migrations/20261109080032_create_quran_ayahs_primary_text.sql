create table if not exists public.quran_ayahs (
  surah_number smallint not null references public.quran_surahs(surah_number) on delete cascade,
  ayah_number smallint not null check (ayah_number > 0),
  text_uthmani text not null,
  juz_number smallint check (juz_number between 1 and 30),
  page_number smallint check (page_number > 0),
  source_name text not null default 'Tanzil Project',
  source_url text not null default 'https://tanzil.net',
  license_name text not null default 'Creative Commons Attribution 3.0',
  license_url text not null default 'https://tanzil.net/docs/Text_License',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  primary key (surah_number, ayah_number)
);

alter table public.quran_ayahs enable row level security;

drop policy if exists "Public can read Quran ayahs" on public.quran_ayahs;
create policy "Public can read Quran ayahs"
  on public.quran_ayahs
  for select
  to anon, authenticated
  using (true);

grant select on public.quran_ayahs to anon, authenticated;
