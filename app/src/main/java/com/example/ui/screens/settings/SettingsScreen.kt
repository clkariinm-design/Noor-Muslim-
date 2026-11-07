package com.example.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.AvailableQaris
import com.example.data.model.CalculationMethod
import com.example.data.model.JuristicMethod
import com.example.data.model.LocalizedStrings
import com.example.data.model.Qari
import com.example.ui.components.IslamicOrnamentCard
import com.example.ui.screens.quran.ReciterSelectionDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold

@Composable
fun SettingsScreen(
    strings: LocalizedStrings,
    currentLanguage: AppLanguage,
    appPreferences: AppPreferences,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (String) -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showQariDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    var themeMode by remember { mutableStateOf(appPreferences.getThemeMode()) }
    var arabicFontSize by remember { mutableFloatStateOf(appPreferences.getArabicFontSize()) }
    var translationFontSize by remember { mutableFloatStateOf(appPreferences.getTranslationFontSize()) }
    var prayerNotifs by remember { mutableStateOf(appPreferences.isPrayerNotificationsEnabled()) }

    var selectedQariId by remember { mutableStateOf(appPreferences.getSelectedQariId()) }
    val currentQari = AvailableQaris.firstOrNull { it.id == selectedQariId } ?: AvailableQaris[0]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Language Option
        item {
            SettingsCard(
                icon = Icons.Default.Language,
                title = strings.language,
                subtitle = "${currentLanguage.nativeName} (${currentLanguage.displayName})",
                testTag = "setting_language",
                onClick = { showLanguageDialog = true }
            )
        }

        // 2. Appearance & Theme Mode
        item {
            val themeLabel = when (themeMode) {
                "LIGHT" -> strings.themeLight
                "DARK" -> strings.themeDark
                else -> strings.themeSystem
            }
            SettingsCard(
                icon = Icons.Default.Palette,
                title = strings.theme,
                subtitle = themeLabel,
                testTag = "setting_theme",
                onClick = { showThemeDialog = true }
            )
        }

        // 3. Audio & Reciters
        item {
            SettingsCard(
                icon = Icons.Default.RecordVoiceOver,
                title = strings.audioSettings,
                subtitle = "${currentQari.name} (${currentQari.subName})",
                testTag = "setting_qari",
                onClick = { showQariDialog = true }
            )
        }

        // 4. Notifications
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = IslamicEmerald,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = strings.prayerReminders,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (prayerNotifs) strings.notificationsEnabled else strings.notificationsDisabled,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = prayerNotifs,
                        onCheckedChange = {
                            prayerNotifs = it
                            appPreferences.setPrayerNotificationsEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IslamicGold,
                            checkedTrackColor = IslamicEmerald
                        )
                    )
                }
            }
        }

        // 5. Typography & Font Size Sliders
        item {
            IslamicOrnamentCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = IslamicEmerald,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = strings.typography,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "${strings.arabicFontSize}: ${arabicFontSize.toInt()} sp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = arabicFontSize,
                        onValueChange = {
                            arabicFontSize = it
                            appPreferences.setArabicFontSize(it)
                        },
                        valueRange = 20f..38f,
                        colors = SliderDefaults.colors(
                            thumbColor = IslamicEmerald,
                            activeTrackColor = IslamicEmerald
                        )
                    )

                    // Live sample Arabic text preview
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontFamily = AmiriFontFamily,
                        fontSize = arabicFontSize.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${strings.translationFontSize}: ${translationFontSize.toInt()} sp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = translationFontSize,
                        onValueChange = {
                            translationFontSize = it
                            appPreferences.setTranslationFontSize(it)
                        },
                        valueRange = 12f..22f,
                        colors = SliderDefaults.colors(
                            thumbColor = IslamicEmerald,
                            activeTrackColor = IslamicEmerald
                        )
                    )
                }
            }
        }

        // 6. About App
        item {
            SettingsCard(
                icon = Icons.Default.Info,
                title = strings.aboutApp,
                subtitle = "${strings.appVersion} 1.0.0",
                testTag = "setting_about",
                onClick = { showAboutDialog = true }
            )
        }

        // 7. Privacy Policy
        item {
            SettingsCard(
                icon = Icons.Default.Security,
                title = strings.privacyPolicy,
                subtitle = "100% on-device private & secure",
                testTag = "setting_privacy",
                onClick = { showPrivacyDialog = true }
            )
        }
    }

    // Language Selector Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.language, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    AppLanguage.entries.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = lang == currentLanguage,
                                onClick = {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = lang.nativeName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = lang.displayName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Theme Selector Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(strings.theme, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        "SYSTEM" to strings.themeSystem,
                        "LIGHT" to strings.themeLight,
                        "DARK" to strings.themeDark
                    ).forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    themeMode = mode
                                    onThemeChange(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    themeMode = mode
                                    onThemeChange(mode)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Qari Selector Dialog
    if (showQariDialog) {
        ReciterSelectionDialog(
            selectedQariId = selectedQariId,
            strings = strings,
            onSelectQari = { qari ->
                selectedQariId = qari.id
                appPreferences.setSelectedQariId(qari.id)
                showQariDialog = false
            },
            onDismiss = { showQariDialog = false }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(strings.aboutApp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = strings.aboutText, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = strings.developerNote, fontSize = 12.sp, color = IslamicGold, fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = IslamicEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(strings.privacyPolicy, fontWeight = FontWeight.Bold) },
            text = {
                Text(text = strings.privacyText, fontSize = 14.sp, lineHeight = 20.sp)
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK", color = IslamicEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IslamicEmerald,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
