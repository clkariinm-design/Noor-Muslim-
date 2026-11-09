package com.example.data.repository

import android.content.Context
import com.example.data.model.AvailableQaris
import com.example.data.model.Ayah
import com.example.data.model.Juz
import com.example.data.model.Qari
import com.example.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object QuranRepository {

    // Complete verified index of all 114 Surahs
    val allSurahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", 7, true, 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", 286, false, 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, false, 3),
        Surah(4, "النساء", "An-Nisa", "The Women", 176, false, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, false, 6),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", 165, true, 7),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", 206, true, 8),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", 75, false, 9),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", 129, false, 10),
        Surah(10, "يونس", "Yunus", "Jonah", 109, true, 11),
        Surah(11, "هود", "Hud", "Hud", 123, true, 11),
        Surah(12, "يوسف", "Yusuf", "Joseph", 111, true, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", 43, false, 13),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", 52, true, 13),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", 99, true, 14),
        Surah(16, "النحل", "An-Nahl", "The Bee", 128, true, 14),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", 111, true, 15),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", 110, true, 15),
        Surah(19, "مريم", "Maryam", "Mary", 98, true, 16),
        Surah(20, "طه", "Ta-Ha", "Ta-Ha", 135, true, 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", 112, true, 17),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", 78, false, 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", 118, true, 18),
        Surah(24, "النور", "An-Nur", "The Light", 64, false, 18),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", 77, true, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", 227, true, 19),
        Surah(27, "النمل", "An-Naml", "The Ant", 93, true, 19),
        Surah(28, "القصص", "Al-Qasas", "The Stories", 88, true, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", 69, true, 20),
        Surah(30, "الروم", "Ar-Rum", "The Romans", 60, true, 21),
        Surah(31, "لقمان", "Luqman", "Luqman", 34, true, 21),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", 30, true, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", 73, false, 21),
        Surah(34, "سبأ", "Saba", "Sheba", 54, true, 22),
        Surah(35, "فاطر", "Fatir", "Originator", 45, true, 22),
        Surah(36, "يس", "Ya-Sin", "Ya Sin", 83, true, 22),
        Surah(37, "الصافات", "As-Saffat", "Those who set the Ranks", 182, true, 23),
        Surah(38, "ص", "Sad", "The Letter Sad", 88, true, 23),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", 75, true, 23),
        Surah(40, "غافر", "Ghafir", "The Forgiver", 85, true, 24),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", 54, true, 24),
        Surah(42, "الشورى", "Ash-Shuraa", "The Consultation", 53, true, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", 89, true, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, true, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", 37, true, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, true, 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", 38, false, 26),
        Surah(48, "الفتح", "Al-Fath", "The Victory", 29, false, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", 18, false, 26),
        Surah(50, "ق", "Qaf", "The Letter Qaf", 45, true, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", 60, true, 26),
        Surah(52, "الطور", "At-Tur", "The Mount", 49, true, 27),
        Surah(53, "النجم", "An-Najm", "The Star", 62, true, 27),
        Surah(54, "القمر", "Al-Qamar", "The Moon", 55, true, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, false, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, true, 27),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", 29, false, 27),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", 22, false, 28),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", 24, false, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She that is to be examined", 13, false, 28),
        Surah(61, "الصف", "As-Saf", "The Ranks", 14, false, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation", 11, false, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", 11, false, 28),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", 18, false, 28),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", 12, false, 28),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", 12, false, 28),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", 30, true, 29),
        Surah(68, "القلم", "Al-Qalam", "The Pen", 52, true, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", 52, true, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", 44, true, 29),
        Surah(71, "نوح", "Nuh", "Noah", 28, true, 29),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", 28, true, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", 20, true, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", 56, true, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", 40, true, 29),
        Surah(76, "الإنسان", "Al-Insan", "Man", 31, false, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", 50, true, 29),
        Surah(78, "النبأ", "An-Naba", "The Tidings", 40, true, 30),
        Surah(79, "النازعات", "An-Nazi'at", "Those who drag forth", 46, true, 30),
        Surah(80, "عبس", "'Abasa", "He Frowned", 42, true, 30),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", 29, true, 30),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", 19, true, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "The Defrauding", 36, true, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", 25, true, 30),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", 22, true, 30),
        Surah(86, "الطارق", "At-Tariq", "The Nightcometh", 17, true, 30),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", 19, true, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", 26, true, 30),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", 30, true, 30),
        Surah(90, "البلد", "Al-Balad", "The City", 20, true, 30),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", 15, true, 30),
        Surah(92, "الليل", "Al-Layl", "The Night", 21, true, 30),
        Surah(93, "الضحى", "Ad-Duhaa", "The Morning Hours", 11, true, 30),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", 8, true, 30),
        Surah(95, "التين", "At-Tin", "The Fig", 8, true, 30),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", 19, true, 30),
        Surah(97, "القدر", "Al-Qadr", "The Power", 5, true, 30),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", 8, false, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", 8, false, 30),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", 11, true, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", 11, true, 30),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry in world increase", 8, true, 30),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", 3, true, 30),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", 9, true, 30),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", 5, true, 30),
        Surah(106, "قريش", "Quraysh", "Quraysh", 4, true, 30),
        Surah(107, "الماعون", "Al-Ma'un", "The Small Kindnesses", 7, true, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "The Abundance", 3, true, 30),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, true, 30),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", 3, false, 30),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", 5, true, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, true, 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", 5, true, 30),
        Surah(114, "الناس", "An-Nas", "Mankind", 6, true, 30)
    )

    // All 30 Juz
    val allJuz: List<Juz> = (1..30).map { juzNum ->
        when (juzNum) {
            1 -> Juz(1, "الم", 1, "Al-Fatihah", 1, "Al-Baqarah (141)")
            2 -> Juz(2, "سيقول", 2, "Al-Baqarah", 142, "Al-Baqarah (252)")
            3 -> Juz(3, "تلك الرسل", 2, "Al-Baqarah", 253, "Ali 'Imran (92)")
            4 -> Juz(4, "لن تنالوا", 3, "Ali 'Imran", 93, "An-Nisa (23)")
            5 -> Juz(5, "والمحصنات", 4, "An-Nisa", 24, "An-Nisa (147)")
            6 -> Juz(6, "لا يحب الله", 4, "An-Nisa", 148, "Al-Ma'idah (81)")
            7 -> Juz(7, "وإذا سمعوا", 5, "Al-Ma'idah", 82, "Al-An'am (110)")
            8 -> Juz(8, "ولو أننا", 6, "Al-An'am", 111, "Al-A'raf (87)")
            9 -> Juz(9, "قال الملأ", 7, "Al-A'raf", 88, "Al-Anfal (40)")
            10 -> Juz(10, "واعلموا", 8, "Al-Anfal", 41, "At-Tawbah (92)")
            11 -> Juz(11, "يعتذرون", 9, "At-Tawbah", 93, "Hud (5)")
            12 -> Juz(12, "وما من دابة", 11, "Hud", 6, "Yusuf (52)")
            13 -> Juz(13, "وما أبرئ", 12, "Yusuf", 53, "Ibrahim (52)")
            14 -> Juz(14, "ربما", 15, "Al-Hijr", 1, "An-Nahl (128)")
            15 -> Juz(15, "سبحان الذي", 17, "Al-Isra", 1, "Al-Kahf (74)")
            16 -> Juz(16, "قال ألم", 18, "Al-Kahf", 75, "Ta-Ha (135)")
            17 -> Juz(17, "اقترب للناس", 21, "Al-Anbiya", 1, "Al-Hajj (78)")
            18 -> Juz(18, "قد أفلح", 23, "Al-Mu'minun", 1, "Al-Furqan (20)")
            19 -> Juz(19, "وقال الذين", 25, "Al-Furqan", 21, "An-Naml (55)")
            20 -> Juz(20, "فما كان", 27, "An-Naml", 56, "Al-'Ankabut (45)")
            21 -> Juz(21, "اتل ما أوحي", 29, "Al-'Ankabut", 46, "Al-Ahzab (30)")
            22 -> Juz(22, "ومن يقنت", 33, "Al-Ahzab", 31, "Ya-Sin (27)")
            23 -> Juz(23, "وما أنزلنا", 36, "Ya-Sin", 28, "Az-Zumar (31)")
            24 -> Juz(24, "فمن أظلم", 39, "Az-Zumar", 32, "Fussilat (46)")
            25 -> Juz(25, "إليه يرد", 41, "Fussilat", 47, "Al-Jathiyah (37)")
            26 -> Juz(26, "حم", 46, "Al-Ahqaf", 1, "Adh-Dhariyat (30)")
            27 -> Juz(27, "قال فما خطبكم", 51, "Adh-Dhariyat", 31, "Al-Hadid (29)")
            28 -> Juz(28, "قد سمع الله", 58, "Al-Mujadila", 1, "At-Tahrim (12)")
            29 -> Juz(29, "تبارك الذي", 67, "Al-Mulk", 1, "Al-Mursalat (50)")
            30 -> Juz(30, "عم يتساءلون", 78, "An-Naba", 1, "An-Nas (6)")
            else -> Juz(juzNum, "جزء $juzNum", 1, "", 1, "")
        }
    }

    // In-memory cache for loaded surahs
    private val memoryCache = mutableMapOf<Int, List<Ayah>>()

    /**
     * Offline-first Quran reading. A validated network payload is cached privately;
     * if the service is unavailable, the bundled Tanzil Uthmani text is used immediately.
     * Translations are available when supplied by the online payload; the bundled asset is Arabic-only.
     */
    suspend fun getSurahAyahs(
        context: Context,
        surahNumber: Int,
        selectedQari: Qari = AvailableQaris[0]
    ): List<Ayah> = withContext(Dispatchers.IO) {
        val surah = allSurahs.firstOrNull { it.number == surahNumber }
            ?: return@withContext emptyList()

        memoryCache[surahNumber]?.let { cached ->
            return@withContext cached.map { ayah ->
                ayah.copy(audioUrl = getAyahAudioUrl(selectedQari, surahNumber, ayah.numberInSurah))
            }
        }

        val cacheFile = surahCacheFile(context, surahNumber)
        val cachedJson = cacheFile.takeIf { it.isFile && it.length() > 0L }
            ?.readText(Charsets.UTF_8)

        val json = cachedJson ?: try {
            cacheSurahPayload(context, surahNumber)
            cacheFile.readText(Charsets.UTF_8)
        } catch (error: Exception) {
            if (cacheFile.isFile && cacheFile.length() > 0L) {
                cacheFile.readText(Charsets.UTF_8)
            } else {
                throw error
            }
        }

        val ayahs = parseAyahsJson(json, surahNumber, selectedQari)
        check(ayahs.size == surah.ayahCount) {
            "Quran response incomplete for surah $surahNumber: expected ${surah.ayahCount}, received ${ayahs.size}"
        }
        memoryCache[surahNumber] = ayahs
        ayahs
    }

    /**
     * Downloads and persists a validated surah payload for offline reading.
     * The cache is private to the app and is never exposed as a public file.
     */
    suspend fun cacheSurahPayload(context: Context, surahNumber: Int) = withContext(Dispatchers.IO) {
        require(surahNumber in 1..114) { "Surah number must be between 1 and 114" }
        val surah = allSurahs.first { it.number == surahNumber }
        val target = surahCacheFile(context, surahNumber)
        if (target.isFile && target.length() > 0L && hasCompleteTranslations(context, surahNumber)) {
            return@withContext
        }

        // Prefer the online payload (including available translations), but never block
        // Quran reading when the user is offline: the verified Arabic text ships in assets.
        val payload = try {
            com.example.data.remote.SupabaseQuranApi.getSurahPayload(surahNumber)
        } catch (_: Exception) {
            bundledSurahPayload(context, surahNumber)
        }
        val validated = parseAyahsJson(payload.toString(), surahNumber, AvailableQaris[0])
        check(validated.size == surah.ayahCount) {
            "Quran response incomplete for surah $surahNumber: expected ${surah.ayahCount}, received ${validated.size}"
        }

        target.parentFile?.mkdirs()
        val temporary = File(target.parentFile, "${target.name}.tmp")
        temporary.writeText(payload.toString(), Charsets.UTF_8)
        if (!temporary.renameTo(target)) {
            target.writeText(payload.toString(), Charsets.UTF_8)
            temporary.delete()
        }
    }

    fun hasCompleteTranslations(context: Context, surahNumber: Int): Boolean {
        if (surahNumber !in 1..114) return false
        val file = surahCacheFile(context, surahNumber)
        if (!file.isFile || file.length() <= 0L) return false
        return try {
            val data = JSONObject(file.readText(Charsets.UTF_8)).getJSONArray("data")
            val required = listOf("english_saheeh", "somali_yacob", "turkish_rwwad", "french_rashid")
            val present = mutableSetOf<String>()
            for (i in 0 until data.length()) {
                val identifier = data.getJSONObject(i).getJSONObject("edition").optString("identifier")
                if (identifier in required) present.add(identifier)
            }
            present.containsAll(required)
        } catch (_: Exception) {
            false
        }
    }

    fun isSurahCached(context: Context, surahNumber: Int): Boolean {
        if (surahNumber !in 1..114) return false
        val file = surahCacheFile(context, surahNumber)
        return file.isFile && file.length() > 0L
    }

    private fun surahCacheFile(context: Context, surahNumber: Int): File =
        File(File(context.filesDir, "quran_cache"), "surah_${surahNumber}.json")

    private val bundledPayloadLock = Any()

    @Volatile
    private var bundledPayloads: Map<Int, JSONObject>? = null

    /**
     * Converts the verbatim Tanzil asset into the same read-only payload shape used by
     * the online parser. Ayah number 0 is the separately-listed basmalah marker and is
     * omitted so each surah contains its canonical numbered ayahs only.
     */
    private fun bundledSurahPayload(context: Context, surahNumber: Int): JSONObject {
        val payloads = bundledPayloads ?: synchronized(bundledPayloadLock) {
            bundledPayloads ?: run {
                val source = context.assets.open("quran_uthmani.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { JSONObject(it.readText()) }
                val surahs = source.getJSONArray("surahs")
                val parsed = mutableMapOf<Int, JSONObject>()

                for (i in 0 until surahs.length()) {
                    val surah = surahs.getJSONObject(i)
                    val number = surah.getInt("number")
                    val sourceAyahs = surah.getJSONArray("ayahs")
                    val ayahs = JSONArray()
                    for (j in 0 until sourceAyahs.length()) {
                        val sourceAyah = sourceAyahs.getJSONObject(j)
                        val ayahNumber = sourceAyah.getInt("number")
                        if (ayahNumber > 0) {
                            ayahs.put(
                                JSONObject()
                                    .put("numberInSurah", ayahNumber)
                                    .put("text", sourceAyah.getString("text"))
                            )
                        }
                    }

                    val edition = JSONObject().put("identifier", "quran-uthmani")
                    val data = JSONObject().put("edition", edition).put("ayahs", ayahs)
                    parsed[number] = JSONObject().put("data", JSONArray().put(data))
                }
                parsed.toMap().also { bundledPayloads = it }
            }
        }
        return payloads[surahNumber]
            ?: throw IllegalStateException("Bundled Quran text is missing surah $surahNumber")
    }

    private fun parseAyahsJson(jsonString: String, surahNumber: Int, selectedQari: Qari): List<Ayah> {
        val root = JSONObject(jsonString)
        val dataArray = root.getJSONArray("data")

        var arabicEd: JSONObject? = null
        var enEd: JSONObject? = null
        var soEd: JSONObject? = null
        var trEd: JSONObject? = null
        var frEd: JSONObject? = null

        for (i in 0 until dataArray.length()) {
            val ed = dataArray.getJSONObject(i)
            val identifier = ed.getJSONObject("edition").getString("identifier")
            when {
                identifier.contains("quran-uthmani") -> arabicEd = ed
                identifier.contains("english_saheeh") || identifier.contains("en.sahih") -> enEd = ed
                identifier.contains("somali_yacob") || identifier.contains("so.abduh") -> soEd = ed
                identifier.contains("turkish_rwwad") || identifier.contains("tr.diyanet") -> trEd = ed
                identifier.contains("french_rashid") || identifier.contains("fr.hamidullah") -> frEd = ed
            }
        }

        val arabicAyahs = arabicEd?.getJSONArray("ayahs") ?: return emptyList()
        val enAyahs = enEd?.getJSONArray("ayahs")
        val soAyahs = soEd?.getJSONArray("ayahs")
        val trAyahs = trEd?.getJSONArray("ayahs")
        val frAyahs = frEd?.getJSONArray("ayahs")

        val result = mutableListOf<Ayah>()
        for (i in 0 until arabicAyahs.length()) {
            val aObj = arabicAyahs.getJSONObject(i)
            val numInSurah = aObj.getInt("numberInSurah")
            val arabicText = aObj.getString("text")

            val enText = if (enAyahs != null && i < enAyahs.length()) enAyahs.getJSONObject(i).getString("text") else ""
            val soText = if (soAyahs != null && i < soAyahs.length()) soAyahs.getJSONObject(i).getString("text") else ""
            val trText = if (trAyahs != null && i < trAyahs.length()) trAyahs.getJSONObject(i).getString("text") else ""
            val frText = if (frAyahs != null && i < frAyahs.length()) frAyahs.getJSONObject(i).getString("text") else ""

            val audioUrl = getAyahAudioUrl(selectedQari, surahNumber, numInSurah)

            result.add(
                Ayah(
                    numberInSurah = numInSurah,
                    textArabic = arabicText,
                    translationEn = enText,
                    translationSo = soText,
                    translationTr = trText,
                    translationFr = frText,
                    audioUrl = audioUrl
                )
            )
        }
        return result
    }

    fun getAyahAudioUrl(qari: Qari, surahNumber: Int, ayahNumber: Int): String {
        val sStr = String.format("%03d", surahNumber)
        val aStr = String.format("%03d", ayahNumber)
        return "https://everyayah.com/data/${qari.audioSubfolder}/$sStr$aStr.mp3"
    }

}
