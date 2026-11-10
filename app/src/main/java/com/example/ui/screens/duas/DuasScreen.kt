package com.example.ui.screens.duas

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.DuaCategory
import com.example.data.model.DuaItem
import com.example.data.model.LocalizedStrings
import com.example.data.repository.DuasRepository
import com.example.ui.components.IslamicOrnamentCard
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold

@Composable
fun DuasScreen(
    strings: LocalizedStrings,
    language: AppLanguage
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }
    val countsMap = remember { mutableStateMapOf<String, Int>() }

    val categories = listOf(
        null to strings.viewAll,
        DuaCategory.MORNING_EVENING to strings.morningEvening,
        DuaCategory.SLEEP_WAKE to strings.sleepWake,
        DuaCategory.FOOD_DRINK to strings.foodDrink,
        DuaCategory.TRAVEL to strings.travel,
        DuaCategory.PROTECTION to strings.protection,
        DuaCategory.MOSQUE_PRAYER to strings.prayerMosque
    )

    val filteredDuas = remember(selectedCategory) {
        if (selectedCategory == null) {
            DuasRepository.allDuas
        } else {
            DuasRepository.allDuas.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("duas_screen")
    ) {
        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (cat, label) ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicEmerald,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Duas List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredDuas, key = { it.id }) { dua ->
                val currentCount = countsMap[dua.id] ?: 0
                val isCompleted = currentCount >= dua.targetCount

                IslamicOrnamentCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dua_card_${dua.id}")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Title & Copy Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dua.getTitle(language),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val textToCopy = "${dua.getTitle(language)}\n\n${dua.arabicText}\n\n${dua.getTranslation(language)}\n[${dua.reference}]"
                                    cm.setPrimaryClip(ClipData.newPlainText("Dua", textToCopy))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Arabic Text
                        Text(
                            text = dua.arabicText,
                            fontFamily = AmiriFontFamily,
                            fontSize = 22.sp,
                            lineHeight = 36.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Transliteration
                        Text(
                            text = dua.transliteration,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = IslamicGold,
                            fontWeight = FontWeight.Medium
                        )

                        // Translation
                        val trans = dua.getTranslation(language)
                        if (trans.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = trans,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reference & Interactive Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dua.reference,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            // Interactive Counter Button
                            Button(
                                onClick = {
                                    val next = (currentCount + 1)
                                    if (next <= dua.targetCount) {
                                        countsMap[dua.id] = next
                                    } else {
                                        countsMap[dua.id] = 0 // reset loop
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCompleted) IslamicGold else IslamicEmerald
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${dua.targetCount}/${dua.targetCount}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Text(
                                        text = "$currentCount / ${dua.targetCount} ${strings.timesCount}",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
