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
    Qari("alafasy", "Mishary Rashid Alafasy", "مشاري بن راشد العفاسي", "ar.alafasy", "128 kbps (High)", "Murattal"),
    Qari("abdulbaset", "AbdulBaset AbdulSamad", "عبد الباسط عبد الصمد", "ar.abdulsamad", "64 kbps", "Murattal"),
    Qari("sudais", "Abdurrahmaan As-Sudais", "عبدالرحمن السديس", "ar.abdurrahmaansudais", "192 kbps", "Murattal"),
    Qari("husary", "Mahmoud Khalil Al-Husary", "محمود خليل الحصري", "ar.husary", "128 kbps", "Murattal"),
    Qari("shatri", "Abu Bakr Al-Shatri", "أبو بكر الشاطري", "ar.shaatree", "128 kbps", "Murattal"),
    Qari("hudhaify", "Ali Al-Hudhaify", "علي بن عبد الرحمن الحذيفي", "ar.hudhaify", "128 kbps", "Murattal"),
    Qari("muaiqly", "Maher Al-Muaiqly", "ماهر المعيقلي", "ar.mahermuaiqly", "128 kbps", "Murattal")
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
