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
    val audioSubfolder: String
)

val AvailableQaris = listOf(
    Qari("alafasy", "Mishary Rashid Alafasy", "مشاري بن راشد العفاسي", "Alafasy_128kbps"),
    Qari("abdulbaset", "AbdulBaset AbdulSamad", "عبد الباسط عبد الصمد", "Abdul_Basit_Murattal_192kbps"),
    Qari("ghamdi", "Saad Al-Ghamdi", "سعد الغامدي", "Ghamadi_40kbps"),
    Qari("husary", "Mahmoud Khalil Al-Husary", "محمود خليل الحصري", "Husary_128kbps")
)

data class ReadingBookmark(
    val id: Long = System.currentTimeMillis(),
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
