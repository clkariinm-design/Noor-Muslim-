package com.example.data.repository

import android.content.Context
import com.example.data.model.AvailableQaris
import com.example.data.model.Ayah
import com.example.data.model.Juz
import com.example.data.model.Qari
import com.example.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object QuranRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

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
     * Retrieves Ayahs for a Surah. Uses cache -> local fallback -> API.
     */
    suspend fun getSurahAyahs(
        context: Context,
        surahNumber: Int,
        selectedQari: Qari = AvailableQaris[0]
    ): List<Ayah> = withContext(Dispatchers.IO) {
        // 1. Check memory cache
        memoryCache[surahNumber]?.let { return@withContext it }

        // 2. Check disk cache
        val diskFile = File(context.cacheDir, "surah_$surahNumber.json")
        if (diskFile.exists()) {
            try {
                val json = diskFile.readText()
                val ayahs = parseAyahsJson(json, surahNumber, selectedQari)
                if (ayahs.isNotEmpty()) {
                    memoryCache[surahNumber] = ayahs
                    return@withContext ayahs
                }
            } catch (e: Exception) {
                // fall through
            }
        }

        // 3. Check bundled offline Surahs
        val bundled = getBundledSurah(surahNumber, selectedQari)
        if (bundled.isNotEmpty()) {
            memoryCache[surahNumber] = bundled
            return@withContext bundled
        }

        // 4. Fetch from verified AlQuran.cloud API with translations
        try {
            val url = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.sahih,so.abduh,tr.diyanet,fr.hamidullah"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                diskFile.writeText(body)
                val ayahs = parseAyahsJson(body, surahNumber, selectedQari)
                if (ayahs.isNotEmpty()) {
                    memoryCache[surahNumber] = ayahs
                    return@withContext ayahs
                }
            }
        } catch (e: Exception) {
            // Network failed or offline
        }

        // Return emptyList so UI can prompt user to connect to download verified Arabic text
        return@withContext emptyList<Ayah>()
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
                identifier.contains("en.sahih") -> enEd = ed
                identifier.contains("so.abduh") -> soEd = ed
                identifier.contains("tr.diyanet") -> trEd = ed
                identifier.contains("fr.hamidullah") -> frEd = ed
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

    /**
     * Bundled verified authentic Surahs for offline instant access (Al-Fatihah, Al-Ikhlas, Al-Falaq, An-Nas, Al-Kawthar, Al-Asr, Al-Kafirun).
     */
    private fun getBundledSurah(surahNumber: Int, qari: Qari): List<Ayah> {
        return when (surahNumber) {
            1 -> listOf(
                Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "Magaca Eebe yaan ku bilaabaynaa ee Naxariis Guud iyo mid Gaaraba Naxariista.", "Rahmân ve Rahîm olan Allah'ın adıyla.", "Au nom d'Allah, le Tout Miséricordieux, le Très Miséricordieux.", getAyahAudioUrl(qari, 1, 1)),
                Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "Mahad Eebaa iska leh ee Eebaha Caalamka ah.", "Hamd, Âlemlerin Rabbi olan Allah'a mahsustur.", "Louange à Allah, Seigneur de l'univers.", getAyahAudioUrl(qari, 1, 2)),
                Ayah(3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "Ee Naxariis Guud iyo mid Gaaraba Naxariista.", "O, Rahmândır ve Rahîmdir.", "Le Tout Miséricordieux, le Très Miséricordieux,", getAyahAudioUrl(qari, 1, 3)),
                Ayah(4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "Ee Xukunka Maalinta Abaalmarinta leh.", "Din (hesap ve ceza) gününün sahibidir.", "Maître du Jour de la rétribution.", getAyahAudioUrl(qari, 1, 4)),
                Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "Adiga kaliya yaan ku caabudaynaa, adigana kaliya yaan kaalmo kaa baryaynaa.", "(Rabbimiz!) Yalnız sana kulluk eder ve yalnız senden yardım dileriz.", "C'est Toi [Seul] que nous adorons, et c'est Toi [Seul] dont nous implorons secours.", getAyahAudioUrl(qari, 1, 5)),
                Ayah(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "Nagu hanuuni jidka toosan.", "Bizi doğru yola ilet.", "Guide-nous dans le droit chemin,", getAyahAudioUrl(qari, 1, 6)),
                Ayah(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "Jidka kuwii aad u nicmaysay aan ahayn kuwa loo cadhooday iyo kuwa haboobay midna.", "Kendilerine lütufta bulunduğun kimselerin yoluna; gazaba uğramışların ve sapmışların yoluna değil.", "le chemin de ceux que Tu as comblés de faveurs, non pas de ceux qui ont encouru Ta colère, ni des égarés.", getAyahAudioUrl(qari, 1, 7))
            )
            112 -> listOf(
                Ayah(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", "Waxaad dhahdaa: Eebe waa Kow.", "De ki: O, Allah'tır, bir tektir.", "Dis: «Il est Allah, Unique.", getAyahAudioUrl(qari, 112, 1)),
                Ayah(2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "Eebe waa kan baahida loo qabo oo aan cidna u baahnayn.", "Allah Samed'dir (her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir).", "Allah, Le Seul à être imploré pour ce que nous désirons.", getAyahAudioUrl(qari, 112, 2)),
                Ayah(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "Wax ma dhalin isna lama dhalin.", "O, doğurmamış ve doğmamıştır.", "Il n'a jamais engendré, n'a pas été engendré non plus.", getAyahAudioUrl(qari, 112, 3)),
                Ayah(4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"", "Mana jiro mid u dhigma haba yaraatee.", "Hiçbir şey O'na denk ve benzer değildir.", "Et nul n'est égal à Lui».", getAyahAudioUrl(qari, 112, 4))
            )
            113 -> listOf(
                Ayah(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", "Waxaad dhahdaa: waxaan magan galaa Eebaha waaga baryaya,", "De ki: Sabahın Rabbine sığınırım,", "Dis: «Je cherche protection auprès du Seigneur de l'aube naissante,", getAyahAudioUrl(qari, 113, 1)),
                Ayah(2, "مِنْ شَرِّ مَا خَلَقَ", "From the evil of that which He created", "Sharciiga wuxuu abuuray oo dhan,", "Yarattığı şeylerin şerrinden,", "contre le mal des êtres qu'Il a créés,", getAyahAudioUrl(qari, 113, 2)),
                Ayah(3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "Iyo sharka habeenka marka uu madoobaado,", "Karanlığı çöktüğü zaman gecenin şerrinden,", "contre le mal de l'obscurité quand elle s'approfondit,", getAyahAudioUrl(qari, 113, 3)),
                Ayah(4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "Iyo sharka kuwa guntimaha afuufa (saaxiriinta),", "Düğümlere üfürenlerin şerrinden,", "contre le mal de celles qui soufflent [les sorcières] sur les nœuds,", getAyahAudioUrl(qari, 113, 4)),
                Ayah(5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", "Iyo sharka xaasidka marka uu wax xaasido.", "Ve haset ettiği zaman hasetçinin şerrinden.", "et contre le mal de l'envieux quand il envie».", getAyahAudioUrl(qari, 113, 5))
            )
            114 -> listOf(
                Ayah(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", "Waxaad dhahdaa: waxaan magan galaa Eebaha dadka,", "De ki: İnsanların Rabbine sığınırım,", "Dis: «Je cherche protection auprès du Seigneur des hommes,", getAyahAudioUrl(qari, 114, 1)),
                Ayah(2, "مَلِكِ النَّاسِ", "The Sovereign of mankind,", "Boqorka dadka,", "İnsanların hükümranına,", "Le Souverain des hommes,", getAyahAudioUrl(qari, 114, 2)),
                Ayah(3, "إِلَٰهِ النَّاسِ", "The God of mankind,", "Ilaaha dadka,", "İnsanların ilâhına,", "L'Élu des hommes,", getAyahAudioUrl(qari, 114, 3)),
                Ayah(4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "Sharka waswaasiyaha dib u guranaya,", "Sinsice kalplere vesvese verenin şerrinden,", "contre le mal du mauvais conseiller, furtif,", getAyahAudioUrl(qari, 114, 4)),
                Ayah(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -", "Kaasoo ku waswaasiya laabaha dadka,", "O ki, insanların göğüslerine vesvese verir,", "qui souffle le mal dans les poitrines des hommes,", getAyahAudioUrl(qari, 114, 5)),
                Ayah(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"", "Hadday jinka ka mid yihiin ama dadka.", "Gerek cinlerden, gerek insanlardan.", "qu'il soit, [ce conseiller], parmi les djinns ou les hommes».", getAyahAudioUrl(qari, 114, 6))
            )
            108 -> listOf(
                Ayah(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", "Waxaan ku siinay khayr badan oo aan go'ayn (Kawthar).", "Şüphesiz biz sana Kevser'i verdik.", "Nous t'avons certes, accordé l'Abondance.", getAyahAudioUrl(qari, 108, 1)),
                Ayah(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "So pray to your Lord and sacrifice [to Him alone].", "Ee Rabbigaa u tuko una gowrac.", "Şimdi sen Rabbine kulluk et ve kurban kes.", "Accomplis donc la Salât pour ton Seigneur et sacrifie.", getAyahAudioUrl(qari, 108, 2)),
                Ayah(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Indeed, your enemy is the one cut off.", "Qofka ku neceb ayaa ah midka go'an ee khayrka laga xayuubiyay.", "Doğrusu sana kin besleyendir soyu kesik olan.", "Certes, celui qui te hait sera sans postérité.", getAyahAudioUrl(qari, 108, 3))
            )
            103 -> listOf(
                Ayah(1, "وَالْعَصْرِ", "By time,", "Waxaan ku dhaartay xilliga,", "Asra yemin olsun ki,", "Par le Temps!", getAyahAudioUrl(qari, 103, 1)),
                Ayah(2, "إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ", "Indeed, mankind is in loss,", "Dadka oo dhami khasaare ayay ku jiraan,", "İnsan gerçekten ziyan içindedir.", "L'homme est certes, en perdition,", getAyahAudioUrl(qari, 103, 2)),
                Ayah(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", "Marka laga reebo kuwa rumeeyey xaqa oo falay camal suuban, iskuna dardaarmay xaqa iyo samirka.", "Ancak iman edip iyi ameller işleyenler, birbirlerine hakkı tavsiye edenler ve sabrı tavsiye edenler müstesnadır.", "sauf ceux qui croient et accomplissent les bonnes œuvres, s'enjoignent mutuellement la vérité et s'enjoignent mutuellement l'endurance.", getAyahAudioUrl(qari, 103, 3))
            )
            109 -> listOf(
                Ayah(1, "قُلْ يَا أَيُّهَا الْكَافِرُونَ", "Say, \"O disbelievers,", "Waxaad dhahdaa: Kuwa Gaalada ahow,", "De ki: Ey kâfirler!", "Dis: «Ô vous les infidèles!", getAyahAudioUrl(qari, 109, 1)),
                Ayah(2, "لَا أَعْبُدُ مَا تَعْبُدُونَ", "I do not worship what you worship.", "Ma caabudo waxaad caabudaysaan,", "Ben sizin kulluk ettiklerinize kulluk etmem.", "Je n'adore pas ce que vous adorez.", getAyahAudioUrl(qari, 109, 2)),
                Ayah(3, "وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ", "Nor are you worshippers of what I worship.", "Idinkuna ma caabudaysaan waxaan caabudo,", "Siz de benim kulluk ettiğime kulluk edecek değilsiniz.", "Et vous n'êtes pas adorateurs de ce que j'adore.", getAyahAudioUrl(qari, 109, 3)),
                Ayah(4, "وَلَا أَنَا عَابِدٌ مَا عَبَدْتُمْ", "Nor will I be a worshipper of what you worship.", "Mana noqonayo mid caabuda waxaad caabuddeen,", "Ben de sizin kulluk ettiklerinize kulluk edecek değilim.", "Je ne suis pas adorateur de ce que vous avez adoré.", getAyahAudioUrl(qari, 109, 4)),
                Ayah(5, "وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ", "Nor will you be worshippers of what I worship.", "Idinkuna ma noqonaysaan kuwo caabuda waxaan caabudo,", "Siz de benim kulluk ettiğime kulluk edecek değilsiniz.", "Et vous n'êtes pas adorateurs de ce que j'adore.", getAyahAudioUrl(qari, 109, 5)),
                Ayah(6, "لَكُمْ دِينُكُمْ وَلِيَ دِينِ", "For you is your religion, and for me is my religion.\"", "Waxaad leedihiin diintiinna, anna diintaydaan leeyahay.", "Sizin dininiz size, benim dinim banadır.", "A vous votre religion, et à moi ma religion».", getAyahAudioUrl(qari, 109, 6))
            )
            110 -> listOf(
                Ayah(1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", "When the victory of Allah has come and the conquest,", "Markuu yimaado gargaarka Eebbe iyo furashadu (Makka),", "Allah'ın yardımı ve fetih geldiği,", "Lorsque vient le secours d'Allah ainsi que la victoire,", getAyahAudioUrl(qari, 110, 1)),
                Ayah(2, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", "And you see the people entering into the religion of Allah in multitudes,", "Ood aragto dadka oo diinta Eebbe u gelaya koox-koox,", "Ve insanların dalga dalga Allah'ın dinine girdiklerini gördüğün zaman,", "et que tu vois les gens entrer avec foule dans la religion d'Allah,", getAyahAudioUrl(qari, 110, 2)),
                Ayah(3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", "Then exalt [Him] with praise of your Lord and ask forgiveness of Him. Indeed, He is ever Accepting of repentance.", "U tasbiixso adoo mahadinaya Rabbigaa dambi-dhaafna weydiiso, Eebbe waa mid toobada aqbal badane.", "Hemen Rabbini hamd ile tesbih et ve O'ndan mağfiret dile. Çünkü O, tevbeleri çok kabul edendir.", "alors, par la louange, célèbre la gloire de ton Seigneur et implore Son pardon. Car c'est Lui le grand Accueillant au repentir.", getAyahAudioUrl(qari, 110, 3))
            )
            111 -> listOf(
                Ayah(1, "تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ", "May the hands of Abu Lahab be ruined, and ruined is he.", "Waxaa khasaaray labada gacmood ee Abii Lahab isna wuu khasaaray.", "Ebû Leheb'in iki eli kurusun! Kurudu da.", "Que périssent les deux mains d'Abu-Lahab, et que lui-même périsse.", getAyahAudioUrl(qari, 111, 1)),
                Ayah(2, "مَا أَغْنَىٰ عَنْهُ مَالُهُ وَمَا كَسَبَ", "His wealth will not avail him or that which he gained.", "Waxba uma tarin xoolihiisu iyo wuxuu kasbaday toona.", "Ona ne malı fayda verdi, ne de kazandığı.", "Sa fortune ne lui a servi à rien, ni ce qu'il a acquis.", getAyahAudioUrl(qari, 111, 2)),
                Ayah(3, "سَيَصْلَىٰ نَارًا ذَاتَ لَهَبٍ", "He will [enter to] burn in a Fire of [blazing] flame", "Wuxuu geli doonaa naar ololaysa,", "O, alevli bir ateşe girecektir.", "Il sera brûlé dans un Feu plein de flammes,", getAyahAudioUrl(qari, 111, 3)),
                Ayah(4, "وَامْرَأَتُهُ حَمَّالَةَ الْحَطَبِ", "And his wife [as well] - the carrier of firewood.", "Iyo haweeneydiisa qoryaha u sida (sharta iyo fidnada).", "Boynunda bükülmüş urganla odun taşıyıcı olarak karısı da.", "de même sa femme, la porteuse de bois,", getAyahAudioUrl(qari, 111, 4)),
                Ayah(5, "فِي جِيدِهَا حَبْلٌ مِنْ مَسَدٍ", "Around her neck is a rope of [twisted] fiber.", "Waxaa qoorteeda ku xirnaan xadhig liif ah.", "Boynunda sağlam bükülmüş bir ip olduğu halde.", "à son cou, une corde de fibres.", getAyahAudioUrl(qari, 111, 5))
            )
            105 -> listOf(
                Ayah(1, "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ", "Have you not considered, [O Muhammad], how your Lord dealt with the companions of the elephant?", "Miyaadan arag sida Rabbigaa ku sameeyey asxaabtii maroodiga?", "Görmedin mi Rabbin fil sahiplerine ne yaptı?", "N'as-tu pas vu comment ton Seigneur a agi envers les gens de l'Eléphant?", getAyahAudioUrl(qari, 105, 1)),
                Ayah(2, "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ", "Did He not make their plan into misguidance?", "Miyuusan dhigin dhagartooda khasaare?", "Onların tuzaklarını boşa çıkarmadı mı?", "N'a-t-Il pas rendu leur ruse complètement vaine?", getAyahAudioUrl(qari, 105, 2)),
                Ayah(3, "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ", "And He sent against them birds in flocks,", "Wuxuuna ku diray shimbiro koox-koox ah,", "Üzerlerine sürü sürü kuşlar gönderdi.", "et envoyé sur eux des oiseaux par volées,", getAyahAudioUrl(qari, 105, 3)),
                Ayah(4, "تَرْمِيهِمْ بِحِجَارَةٍ مِنْ سِجِّيلٍ", "Striking them with stones of hard clay,", "Kuwaasoo ku tuurayay dhagaxyo dhoobo gubatay ah,", "Onlara çamurdan sertleşmiş taşlar atan.", "qui leur lançaient des pierres d'argile,", getAyahAudioUrl(qari, 105, 4)),
                Ayah(5, "فَجَعَلَهُمْ كَعَصْفٍ مَأْكُولٍ", "And He made them like eaten straw.", "Wuxuuna ka dhigay sida baal la cunay.", "Nihayet onları yenilmiş ekin yaprağı gibi yaptı.", "et Il les a rendus semblables à une paille mâchée.", getAyahAudioUrl(qari, 105, 5))
            )
            106 -> listOf(
                Ayah(1, "لِإِيلَافِ قُرَيْشٍ", "For the accustomed security of the Quraysh -", "Isku xirnaanta iyo barwaaqada reer Qureysh awgeed,", "Kureyş'in güvenliği için,", "A cause du pacte des Quraysh,", getAyahAudioUrl(qari, 106, 1)),
                Ayah(2, "إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ", "Their accustomed security [in] the caravan of winter and summer -", "Isku xirnaanta socdaalkooda jiilaalka iyo gu'ga,", "Kış ve yaz seferlerinde onların güvenliğini sağladığı için,", "de leur pacte des voyages d'hiver et d'été.", getAyahAudioUrl(qari, 106, 2)),
                Ayah(3, "فَلْيَعْبُدُوا رَبَّ هَٰذَا الْبَيْتِ", "Let them worship the Lord of this House,", "Haddaba ha caabudeen Eebaha Gurigan (Kacbada),", "Şu evin (Kâbe'nin) Rabbine kulluk etsinler.", "Qu'ils adorent donc le Seigneur de cette Maison (la Ka'ba),", getAyahAudioUrl(qari, 106, 3)),
                Ayah(4, "الَّذِي أَطْعَمَهُمْ مِنْ جُوعٍ وَآمَنَهُمْ مِنْ خَوْفٍ", "Who has fed them, [saving them] from hunger and made them safe, [saving them] from fear.", "Kii cuntada ka siiyey baahida, kana nabadgeliyey cabsida.", "O ki, onları açlıktan doyurdu ve korkudan emin kıldı.", "qui les a nourris contre la faim et rassurés de la peur!", getAyahAudioUrl(qari, 106, 4))
            )
            107 -> listOf(
                Ayah(1, "أَرَأَيْتَ الَّذِي يُكَذِّبُ بِالدِّينِ", "Have you not considered him who denies the Recompense?", "Miyaad aragtay qofka beeninaya abaalmarinta (Aakhiro)?", "Dini (hesap gününü) yalanlayanı gördün mü?", "Vois-tu celui qui traite de mensonge la Rétribution?", getAyahAudioUrl(qari, 107, 1)),
                Ayah(2, "فَذَٰلِكَ الَّذِي يَدُعُّ الْيَتِيمَ", "For that is the one who drives away the orphan", "Kaasi waa kan erya agoonka (oo dhibaateeya),", "İşte o, öksüzü itip kakar.", "C'est bien lui qui repousse l'orphelin,", getAyahAudioUrl(qari, 107, 2)),
                Ayah(3, "وَلَا يَحُضُّ عَلَىٰ طَعَامِ الْمِسْكِينِ", "And does not encourage the feeding of the poor.", "Aanna ku dhiirrigelin quudinta masaakiinta.", "Ve yoksulu doyurmaya teşvik etmez.", "et qui n'encourage point à nourrir le pauvre.", getAyahAudioUrl(qari, 107, 3)),
                Ayah(4, "فَوَيْلٌ لِلْمُصَلِّينَ", "So woe to those who pray", "Halaag baa u sugnaaday kuwa tukanaya,", "Yazıklar olsun o namaz kılanlara ki,", "Malheur donc, à ceux qui prient", getAyahAudioUrl(qari, 107, 4)),
                Ayah(5, "الَّذِينَ هُمْ عَنْ صَلَاتِهِمْ سَاهُونَ", "[But] who are heedless of their prayer -", "Ee ka halmaansan salaaddooda (oo dayaca),", "Onlar namazlarından gafildirler.", "tout en étant négligents avec leur Salât,", getAyahAudioUrl(qari, 107, 5)),
                Ayah(6, "الَّذِينَ هُمْ يُرَاءُونَ", "Those who make a show [of their deeds]", "Kuwii is tus-tuska samaynaya,", "Onlar gösteriş yaparlar.", "qui sont pleins d'ostentation,", getAyahAudioUrl(qari, 107, 6)),
                Ayah(7, "وَيَمْنَعُونَ الْمَاعُونَ", "And withhold [simple] assistance.", "Ee diidaya gargaarka yar (alaabta yar).", "Ve en ufak bir yardımı dahi esirgerler.", "et refusent l'ustensile [le moindre secours]!", getAyahAudioUrl(qari, 107, 7))
            )
            97 -> listOf(
                Ayah(1, "إِنَّا أَنْزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", "Indeed, We sent the Qur'an down during the Night of Decree.", "Waxaan soo dejinnay Quraanka habeenka laylatul-qadriga.", "Şüphesiz, biz onu (Kur'an'ı) Kadir gecesinde indirdik.", "Nous l'avons certes, fait descendre pendant la nuit d'Al-Qadr.", getAyahAudioUrl(qari, 97, 1)),
                Ayah(2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", "And what can make you know what is the Night of Decree?", "Maxaase ku ogeysiiyey waxa uu yahay laylatul-qadrigu?", "Kadir gecesinin ne olduğunu sen ne bileceksin!", "Et qui te dira ce qu'est la nuit d'Al-Qadr?", getAyahAudioUrl(qari, 97, 2)),
                Ayah(3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِنْ أَلْفِ شَهْرٍ", "The Night of Decree is better than a thousand months.", "Habeenka laylatul-qadrigu wuxuu ka khayr badan yahay kun bilood.", "Kadir gecesi bin aydan daha hayırlıdır.", "La nuit d'Al-Qadr est meilleure que mille mois.", getAyahAudioUrl(qari, 97, 3)),
                Ayah(4, "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِمْ مِنْ كُلِّ أَمْرٍ", "The angels and the Spirit descend therein by permission of their Lord for every matter.", "Waxaa soo dega malaa'igta iyo Jibriil habeenkaas amar kasta ogolaanshaha Rabbigood awgeed.", "Melekler ve Ruh (Cebrail) o gecede, Rablerinin izniyle her türlü iş için iner de iner.", "Durant celle-ci descendent les Anges ainsi que l'Esprit, par permission de leur Seigneur pour tout ordre.", getAyahAudioUrl(qari, 97, 4)),
                Ayah(5, "سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ", "Peace it is until the emergence of dawn.", "Waa nabadgelyo ilaa waagu ka baryo.", "O gece, tanyeri ağarana kadar bir esenliktir.", "Elle est paix et salut jusqu'à l'apparition de l'aube.", getAyahAudioUrl(qari, 97, 5))
            )
            else -> emptyList()
        }
    }
}
