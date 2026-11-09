package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.MenuBook
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

enum class NoorNavDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", Icons.Filled.Home, Icons.Outlined.Home),
    QURAN("quran", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    PRAYER("prayer", Icons.Filled.AccessTime, Icons.Outlined.AccessTime),
    TOOLS("tools", Icons.Filled.Apps, Icons.Outlined.Apps),
    QIBLA("qibla", Icons.Filled.Apps, Icons.Outlined.Apps),
    TASBEEH("tasbeeh", Icons.Filled.Apps, Icons.Outlined.Apps),
    DUAS("duas", Icons.Filled.Apps, Icons.Outlined.Apps),
    SETTINGS("settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun NoorBottomNav(
    currentDestination: NoorNavDestination,
    onSelectDestination: (NoorNavDestination) -> Unit,
    strings: LocalizedStrings
) {
    val primaryDestinations = listOf(
        NoorNavDestination.HOME,
        NoorNavDestination.QURAN,
        NoorNavDestination.PRAYER,
        NoorNavDestination.TOOLS,
        NoorNavDestination.SETTINGS
    )
    NavigationBar(
        modifier = Modifier.testTag("noor_bottom_navigation"),
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        primaryDestinations.forEach { destination ->
            val isSelected = currentDestination == destination ||
                (destination == NoorNavDestination.TOOLS && currentDestination in setOf(
                    NoorNavDestination.QIBLA, NoorNavDestination.TASBEEH, NoorNavDestination.DUAS
                ))
            val label = when (destination) {
                NoorNavDestination.HOME -> strings.navHome
                NoorNavDestination.QURAN -> strings.navQuran
                NoorNavDestination.PRAYER -> strings.navPrayer
                NoorNavDestination.TOOLS -> "Tools"
                else -> strings.navSettings
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
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${destination.route}")
            )
        }
    }
}
