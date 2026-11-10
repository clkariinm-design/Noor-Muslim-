package com.example.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvailableQaris
import com.example.data.model.LocalizedStrings
import com.example.data.model.Qari
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright

@Composable
fun ReciterSelectionDialog(
    selectedQariId: String,
    strings: LocalizedStrings,
    onSelectQari: (Qari) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.selectReciter,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reciters_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(AvailableQaris, key = { it.id }) { qari ->
                    val isSelected = qari.id == selectedQariId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectQari(qari)
                                onDismiss()
                            }
                            .testTag("qari_item_${qari.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) IslamicEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, IslamicEmerald) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = qari.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) IslamicEmerald else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = qari.subName,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 13.sp,
                                    color = IslamicGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HighQuality,
                                        contentDescription = "Quality",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${qari.quality} • ${qari.style}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(IslamicEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = IslamicEmerald)
            }
        }
    )
}
