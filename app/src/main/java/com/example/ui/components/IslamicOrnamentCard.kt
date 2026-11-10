package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGold

@Composable
fun IslamicOrnamentCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 2.dp,
    border: BorderStroke? = BorderStroke(1.dp, IslamicBorder),
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun IslamicGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            IslamicEmerald,
                            IslamicEmeraldDark
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}
