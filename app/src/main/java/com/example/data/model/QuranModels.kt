package com.example.data.model

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishMeaning: String,
    val ayahCount: Int,
    val isMeccan: Boolean,
    val juzNumber: Int
)

data class Ayah(
    val numberInSurah: Int,
    val textArabic: String,
    val translationEn: String,
    val translationSo: String = "",
    val translationTr: String = "",
    val translationFr: String = "",
    val audioUrl: String = ""
) {
    fun getTranslation(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.SOMALI -> translationSo.ifEmpty { translationEn }
            AppLanguage.ARABIC -> "" // Native Arabic does not need translation line
            AppLanguage.ENGLISH -> translationEn
            AppLanguage.TURKISH -> translationTr.ifEmpty { translationEn }
            AppLanguage.FRENCH -> translationFr.ifEmpty { translationEn }
        }
    }
}

data class Juz(
    val number: Int,
    val nameArabic: String,
    val startSurahNumber: Int,
    val startSurahName: String,
    val startAyah: Int,
    val endSurahName: String
)

data class Qari(
    val id: String,
    val name: String,
    val subName: String,
    val audioSubfolder: String,
    val quality: String = "128 kbps",
    val style: String = "Murattal"
)

val AvailableQaris = listOf(
    Qari("alafasy", "Mishary Rashid Alafasy", "مشاري بن راشد العفاسي", "Alafasy_128kbps", "128 kbps (High)", "Murattal"),
    Qari("abdulbaset", "AbdulBaset AbdulSamad", "عبد الباسط عبد الصمد", "Abdul_Basit_Murattal_192kbps", "192 kbps (Studio)", "Murattal"),
    Qari("ghamdi", "Saad Al-Ghamdi", "سعد الغامدي", "Ghamadi_40kbps", "40 kbps (Light)", "Murattal"),
    Qari("husary", "Mahmoud Khalil Al-Husary", "محمود خليل الحصري", "Husary_128kbps", "128 kbps (Teacher)", "Murattal"),
    Qari("shatri", "Abu Bakr Al-Shatri", "أبو بكر الشاطري", "Abu_Bakr_Ash-Shaatree_128kbps", "128 kbps (High)", "Murattal"),
    Qari("hudhaify", "Ali Al-Hudhaify", "علي بن عبد الرحمن الحذيفي", "Hudhaify_128kbps", "128 kbps (High)", "Murattal"),
    Qari("muaiqly", "Maher Al-Muaiqly", "ماهر المعيقلي", "MaherAlMuaiqly128kbps", "128 kbps (High)", "Murattal")
)

enum class DownloadState {
    IDLE,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class SurahDownloadStatus(
    val surahNumber: Int,
    val qariId: String,
    val state: DownloadState = DownloadState.IDLE,
    val progressPercent: Int = 0,
    val downloadedAyahs: Int = 0,
    val totalAyahs: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val localDirectoryPath: String? = null,
    val errorMessage: String? = null
)

enum class AudioRepeatMode {
    OFF,
    REPEAT_ONE,
    REPEAT_SURAH
}

data class ReadingBookmark(
    val id: Long = System.currentTimeMillis(),
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
