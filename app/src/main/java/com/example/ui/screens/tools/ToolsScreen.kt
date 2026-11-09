package com.example.ui.screens.tools

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NoorNavDestination

@Composable
fun ToolsScreen(onNavigate: (NoorNavDestination) -> Unit) {
    val tools = listOf(
        ToolItem("Prayer times", "Daily prayer schedule and reminders", Icons.Default.AccessTime, NoorNavDestination.PRAYER),
        ToolItem("Qibla", "Find the direction of the Kaaba", Icons.Default.Explore, NoorNavDestination.QIBLA),
        ToolItem("Digital Tasbeeh", "Keep track of your dhikr", Icons.Default.TouchApp, NoorNavDestination.TASBEEH),
        ToolItem("Duas & Dhikr", "Daily supplications and remembrance", Icons.Default.AutoAwesome, NoorNavDestination.DUAS)
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("tools_screen"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Your daily tools", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(5.dp))
            Text("Everything you need for your daily worship, in one place.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
        }
        items(tools.size) { index ->
            val tool = tools[index]
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(tool.destination) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(tool.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(tool.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Text("›", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private data class ToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val destination: NoorNavDestination
)
