insert into public.quran_reciters (slug, name, country, style, is_active, sort_order)
values
  ('ar.minshawi', 'Muhammad Siddiq Al-Minshawi', 'Egypt', 'Murattal', true, 8),
  ('ar.shuraim', 'Saud Al-Shuraim', 'Saudi Arabia', 'Murattal', true, 9),
  ('ar.muhammadayoub', 'Muhammad Ayyoub', 'Saudi Arabia', 'Murattal', true, 10)
on conflict (slug) do update
set name = excluded.name,
    country = excluded.country,
    style = excluded.style,
    is_active = true,
    sort_order = excluded.sort_order;
