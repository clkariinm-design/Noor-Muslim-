package com.example.data.model

enum class DuaCategory {
    MORNING_EVENING,
    SLEEP_WAKE,
    FOOD_DRINK,
    TRAVEL,
    PROTECTION,
    MOSQUE_PRAYER
}

data class DuaItem(
    val id: String,
    val category: DuaCategory,
    val titleEn: String,
    val titleSo: String,
    val titleAr: String,
    val titleTr: String,
    val titleFr: String,
    val arabicText: String,
    val transliteration: String,
    val translationEn: String,
    val translationSo: String,
    val translationTr: String,
    val translationFr: String,
    val targetCount: Int = 1,
    val reference: String
) {
    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.SOMALI -> titleSo
        AppLanguage.ARABIC -> titleAr
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.TURKISH -> titleTr
        AppLanguage.FRENCH -> titleFr
    }

    fun getTranslation(lang: AppLanguage): String = when (lang) {
        AppLanguage.SOMALI -> translationSo
        AppLanguage.ARABIC -> ""
        AppLanguage.ENGLISH -> translationEn
        AppLanguage.TURKISH -> translationTr
        AppLanguage.FRENCH -> translationFr
    }
}

data class DailyInspiration(
    val verseArabic: String,
    val verseEn: String,
    val verseSo: String,
    val verseTr: String,
    val verseFr: String,
    val verseRef: String,
    val hadithArabic: String,
    val hadithEn: String,
    val hadithSo: String,
    val hadithTr: String,
    val hadithFr: String,
    val hadithRef: String
)
