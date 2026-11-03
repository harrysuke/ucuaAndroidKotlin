package com.ucua.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ucua.app.core.model.TrendPoint
import com.ucua.app.ui.theme.UcuaAccent
import com.ucua.app.ui.theme.UcuaPrimary

/**
 * Grouped bar chart for the last six months of UC / UA submissions.
 * Drawn with Canvas so no charting dependency is required.
 */
@Composable
fun TrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Text(
            text = "No submission history yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val maxValue = (points.maxOfOrNull { maxOf(it.uc, it.ua) } ?: 0).coerceAtLeast(1)

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(color = UcuaPrimary, label = "Unsafe Condition")
            LegendDot(color = UcuaAccent, label = "Unsafe Action")
        }

        Spacer(Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            val labelGutter = 0f
            val chartHeight = size.height - 8f
            val groupWidth = (size.width - labelGutter) / points.size

            // Horizontal grid lines at 0%, 50% and 100%.
            listOf(0f, 0.5f, 1f).forEach { fraction ->
                val y = chartHeight - (chartHeight * fraction)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
            }

            val barWidth = groupWidth * 0.24f
            val gap = groupWidth * 0.06f

            points.forEachIndexed { index, point ->
                val groupStart = index * groupWidth + groupWidth * 0.18f

                val ucHeight = (point.uc.toFloat() / maxValue) * chartHeight
                val uaHeight = (point.ua.toFloat() / maxValue) * chartHeight

                if (point.uc > 0) {
                    drawRoundRect(
                        color = UcuaPrimary,
                        topLeft = Offset(groupStart, chartHeight - ucHeight),
                        size = Size(barWidth, ucHeight.coerceAtLeast(3f)),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                } else {
                    drawRoundRect(
                        color = UcuaPrimary.copy(alpha = 0.15f),
                        topLeft = Offset(groupStart, chartHeight - 3f),
                        size = Size(barWidth, 3f),
                        cornerRadius = CornerRadius(3f, 3f),
                    )
                }

                if (point.ua > 0) {
                    drawRoundRect(
                        color = UcuaAccent,
                        topLeft = Offset(groupStart + barWidth + gap, chartHeight - uaHeight),
                        size = Size(barWidth, uaHeight.coerceAtLeast(3f)),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                } else {
                    drawRoundRect(
                        color = UcuaAccent.copy(alpha = 0.15f),
                        topLeft = Offset(groupStart + barWidth + gap, chartHeight - 3f),
                        size = Size(barWidth, 3f),
                        cornerRadius = CornerRadius(3f, 3f),
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEach { point ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
