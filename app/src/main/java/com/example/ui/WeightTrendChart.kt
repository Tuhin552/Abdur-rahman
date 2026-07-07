package com.example.ui

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BmiEntry
import java.util.Date

@Composable
fun WeightTrendChart(
    entries: List<BmiEntry>,
    targetWeight: Double?,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log weights to view your trend chart.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return;
    }

    // Sort chronologically (oldest to newest)
    val sortedEntries = entries.sortedBy { it.timestamp }
    val weights = sortedEntries.map { it.weightKg }
    
    val baseMin = weights.minOrNull() ?: 50.0
    val baseMax = weights.maxOrNull() ?: 80.0
    
    // Auto-scale including target goal line
    val minVal = if (targetWeight != null && targetWeight > 0) {
        minOf(baseMin, targetWeight) - 3.0
    } else {
        baseMin - 3.0
    }
    
    val maxVal = if (targetWeight != null && targetWeight > 0) {
        maxOf(baseMax, targetWeight) + 3.0
    } else {
        baseMax + 3.0
    }
    
    val range = if (maxVal - minVal > 0) maxVal - minVal else 1.0

    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val tertiaryColor = MaterialTheme.colorScheme.tertiary // goal line
    
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Weight History Trend (kg)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height
            
            val paddingLeft = 45.dp.toPx()
            val paddingRight = 15.dp.toPx()
            val paddingTop = 15.dp.toPx()
            val paddingBottom = 25.dp.toPx()
            
            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            // Helper to get X and Y positions
            fun getX(index: Int, total: Int): Float {
                if (total <= 1) return paddingLeft + chartWidth / 2f
                return paddingLeft + (index.toFloat() / (total - 1)) * chartWidth
            }

            fun getY(value: Double): Float {
                val ratio = (value - minVal) / range
                return paddingTop + (chartHeight - (ratio * chartHeight).toFloat())
            }

            // 1. Draw Y-axis gridlines & labels
            val gridCount = 4
            for (i in 0 until gridCount) {
                val ratio = i.toFloat() / (gridCount - 1)
                val gridVal = minVal + ratio * range
                val y = paddingTop + (chartHeight - (ratio * chartHeight).toFloat())
                
                // Draw line
                drawLine(
                    color = outlineColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw label
                val labelText = String.format("%.1f", gridVal)
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    style = TextStyle(
                        color = onSurfaceColor.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    ),
                    topLeft = Offset(4.dp.toPx(), y - 8.dp.toPx())
                )
            }

            // 2. Draw Target Weight Goal Line (if present)
            if (targetWeight != null && targetWeight > 0.0) {
                val goalY = getY(targetWeight)
                
                // Draw dashed line for goal
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = tertiaryColor,
                    start = Offset(paddingLeft, goalY),
                    end = Offset(width - paddingRight, goalY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = pathEffect
                )
                
                // Draw "Goal" text badge
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Goal (${targetWeight} kg)",
                    style = TextStyle(
                        color = tertiaryColor,
                        fontSize = 10.sp,
                    ),
                    topLeft = Offset(width - paddingRight - 80.dp.toPx(), goalY - 14.dp.toPx())
                )
            }

            // 3. Draw Plot Line and Gradient Under Area
            if (sortedEntries.isNotEmpty()) {
                val points = sortedEntries.mapIndexed { idx, entry ->
                    Offset(getX(idx, sortedEntries.size), getY(entry.weightKg))
                }

                // Path for the curve/line
                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            // Can use simple lineTo or cubic Bézier
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                }

                // Path for the gradient under the line
                val fillPath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, paddingTop + chartHeight)
                        lineTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                        lineTo(points.last().x, paddingTop + chartHeight)
                        close()
                    }
                }

                // Draw gradient under line
                if (points.size > 1) {
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startY = paddingTop,
                            endY = paddingTop + chartHeight
                        )
                    )
                }

                // Draw the line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw Dots at point locations & labels
                points.forEachIndexed { index, point ->
                    // Draw outer aura
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.2f),
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    // Draw inner dot
                    drawCircle(
                        color = primaryColor,
                        radius = 3.5.dp.toPx(),
                        center = point
                    )

                    // Draw Date label below X-axis occasionally to avoid clutter
                    if (sortedEntries.size <= 5 || index == 0 || index == sortedEntries.lastIndex || index == sortedEntries.size / 2) {
                        val entryDate = Date(sortedEntries[index].timestamp)
                        val dateString = DateFormat.format("MM/dd", entryDate).toString()
                        
                        drawText(
                            textMeasurer = textMeasurer,
                            text = dateString,
                            style = TextStyle(
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                fontSize = 9.sp
                            ),
                            topLeft = Offset(point.x - 12.dp.toPx(), paddingTop + chartHeight + 4.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
