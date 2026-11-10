package com.example.ui.screens.tasbeeh

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppPreferences
import com.example.data.model.LocalizedStrings
import com.example.ui.components.IslamicOrnamentCard
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright

val DhikrPresets = listOf(
    "SubhanAllah (سُبْحَانَ اللَّهِ)",
    "Alhamdulillah (الْحَمْدُ لِلَّهِ)",
    "Allahu Akbar (اللَّهُ أَكْبَرُ)",
    "Astaghfirullah (أَسْتَغْفِرُ اللَّهَ)",
    "La ilaha illallah (لَا إِلَهَ إِلَّا اللَّهُ)",
    "SubhanAllahi wa bihamdihi (سُبْحَانَ اللَّهِ وَبِحَمْدِهِ)",
    "Allahumma salli 'ala Muhammad (اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ)"
)

@Composable
fun TasbeehScreen(
    strings: LocalizedStrings,
    appPreferences: AppPreferences
) {
    val context = LocalContext.current

    var currentCount by remember { mutableIntStateOf(appPreferences.getTasbeehCount()) }
    var targetCount by remember { mutableIntStateOf(appPreferences.getTasbeehTarget()) }
    var lifetimeCount by remember { mutableIntStateOf(appPreferences.getTasbeehLifetime()) }
    var selectedDhikr by remember { mutableStateOf(appPreferences.getSelectedDhikr()) }
    var vibrationEnabled by remember { mutableStateOf(appPreferences.isVibrationEnabled()) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomDhikrDialog by remember { mutableStateOf(false) }
    var customDhikrText by remember { mutableStateOf("") }

    var buttonScale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(targetValue = buttonScale, animationSpec = spring(), label = "Scale")

    fun triggerVibration() {
        if (!vibrationEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    val progress = if (targetCount > 0) (currentCount.toFloat() / targetCount).coerceIn(0f, 1f) else 0f
    val isGoalReached = targetCount > 0 && currentCount >= targetCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("tasbeeh_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selected Dhikr Display Card
        IslamicOrnamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.selectDhikr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDhikr,
                    fontFamily = AmiriFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dhikr Presets Horizontal Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dhikr Presets",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = { showCustomDhikrDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = IslamicGold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.customDhikr, color = IslamicGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Dhikr selection chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            DhikrPresets.take(3).forEach { dhikr ->
                val isSelected = selectedDhikr == dhikr
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedDhikr = dhikr
                            appPreferences.setSelectedDhikr(dhikr)
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IslamicEmerald else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                ) {
                    Text(
                        text = dhikr,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Target Presets: 33, 99, 100, Unlimited (0)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(33, 99, 100, 0).forEach { targetOption ->
                val label = if (targetOption == 0) "∞" else targetOption.toString()
                FilterChip(
                    selected = targetCount == targetOption,
                    onClick = {
                        targetCount = targetOption
                        appPreferences.setTasbeehTarget(targetOption)
                    },
                    label = { Text(label, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGold,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // Goal reached banner
        if (isGoalReached) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = IslamicEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.goalReached, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LARGE TACTILE CIRCULAR COUNTER BUTTON
        Box(
            modifier = Modifier
                .size(230.dp)
                .scale(animatedScale)
                .testTag("tasbeeh_count_button"),
            contentAlignment = Alignment.Center
        ) {
            // Circular Progress Indicator
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = IslamicGoldBright,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 10.dp
            )

            // Inner Clickable Disc
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                IslamicEmerald,
                                IslamicEmeraldDark
                            )
                        )
                    )
                    .border(3.dp, IslamicGold.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        buttonScale = 0.94f
                        currentCount += 1
                        lifetimeCount += 1
                        appPreferences.setTasbeehCount(currentCount)
                        appPreferences.addTasbeehLifetime(1)
                        triggerVibration()
                        // Spring back scale
                        buttonScale = 1f
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentCount.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (targetCount > 0) "/ $targetCount" else strings.tapToCount,
                        fontSize = 13.sp,
                        color = IslamicGoldBright,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset and Vibration Control Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showResetDialog = true }
                    .testTag("btn_tasbeeh_reset"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.resetCounter,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Vibration Feedback Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = IslamicEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = {
                        vibrationEnabled = it
                        appPreferences.setVibrationEnabled(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = IslamicGold,
                        checkedTrackColor = IslamicEmerald
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lifetime Counter Badge
        IslamicOrnamentCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.totalCount,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = lifetimeCount.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGold
                )
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(strings.resetConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.resetConfirmMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentCount = 0
                        appPreferences.setTasbeehCount(0)
                        showResetDialog = false
                    }
                ) {
                    Text(strings.confirm, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Custom Dhikr Dialog
    if (showCustomDhikrDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDhikrDialog = false },
            title = { Text(strings.customDhikr, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = customDhikrText,
                        onValueChange = { customDhikrText = it },
                        placeholder = { Text(strings.customDhikrHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customDhikrText.isNotBlank()) {
                            selectedDhikr = customDhikrText.trim()
                            appPreferences.setSelectedDhikr(selectedDhikr)
                            customDhikrText = ""
                        }
                        showCustomDhikrDialog = false
                    }
                ) {
                    Text(strings.save, color = IslamicEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDhikrDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
