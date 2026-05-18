package com.babyvault.android.presentation.screens.history
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babyvault.android.presentation.theme.*
internal data class ItemDecor(val icon: ImageVector, val iconBg: Color, val iconTint: Color)
internal fun itemDecorFor(id: String): ItemDecor = when {
    id.startsWith("feed_")      -> ItemDecor(Icons.Filled.LocalDrink, Rose100,     Rose400)
    id.startsWith("sleep_")     -> ItemDecor(Icons.Filled.Bedtime,    SkyBlue100,  SkyBlue400)
    id.startsWith("diaper_")    -> ItemDecor(Icons.Filled.ChildCare,  Sage100,     Sage400)
    id.startsWith("milestone_") -> ItemDecor(Icons.Filled.Star,       Teal100,     Teal500)
    id.startsWith("growth_")    -> ItemDecor(Icons.AutoMirrored.Filled.ShowChart,  Lavender100, Lavender400)
    else                        -> ItemDecor(Icons.Filled.Circle,     Color(0xFFE8E8E8), Color(0xFFAAAAAA))
}
@Composable
internal fun HistoryItemCard(item: HistoryItem, onDelete: () -> Unit) {
    val decor = itemDecorFor(item.id)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = decor.iconBg, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(decor.icon, contentDescription = null, tint = decor.iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                )
                if (item.subtitle.isNotBlank()) {
                    Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                }
            }
            Text(item.timeLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFB0B0B0), modifier = Modifier.size(18.dp))
            }
        }
    }
}
