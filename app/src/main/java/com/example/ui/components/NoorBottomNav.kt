package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.LocalizedStrings
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold

enum class NoorNavDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", Icons.Filled.Home, Icons.Outlined.Home),
    QURAN("quran", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    PRAYER("prayer", Icons.Filled.AccessTime, Icons.Outlined.AccessTime),
    QIBLA("qibla", Icons.Filled.Explore, Icons.Outlined.Explore),
    TASBEEH("tasbeeh", Icons.Filled.TouchApp, Icons.Outlined.TouchApp),
    DUAS("duas", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    SETTINGS("settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun NoorBottomNav(
    currentDestination: NoorNavDestination,
    onSelectDestination: (NoorNavDestination) -> Unit,
    strings: LocalizedStrings
) {
    NavigationBar(
        modifier = Modifier.testTag("noor_bottom_navigation"),
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Keep the primary navigation intentionally simple. Other tools remain
        // reachable from Home, while Settings stays in one predictable place.
        listOf(NoorNavDestination.HOME, NoorNavDestination.SETTINGS).forEach { destination ->
            val isSelected = destination == currentDestination
            val label = when (destination) {
                NoorNavDestination.HOME -> strings.navHome
                NoorNavDestination.QURAN -> strings.navQuran
                NoorNavDestination.PRAYER -> strings.navPrayer
                NoorNavDestination.QIBLA -> strings.navQibla
                NoorNavDestination.TASBEEH -> strings.navTasbeeh
                NoorNavDestination.DUAS -> strings.navDuas
                NoorNavDestination.SETTINGS -> strings.navSettings
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IslamicEmerald,
                    selectedTextColor = IslamicEmerald,
                    indicatorColor = IslamicGold.copy(alpha = 0.25f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${destination.route}")
            )
        }
    }
}
