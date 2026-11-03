package com.ucua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ucua.app.core.model.HistoryEntry
import com.ucua.app.ui.theme.UcuaAccent
import com.ucua.app.ui.theme.UcuaPrimary

/** Section title with an optional trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/** Dashboard metric card. */
@Composable
fun KpiCard(
    label: String,
    value: Int,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accent.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** UC / UA square badge. */
@Composable
fun TypeBadge(type: String?, modifier: Modifier = Modifier) {
    val isUa = type == "UA"
    val color = if (isUa) UcuaAccent else UcuaPrimary
    Box(
        modifier = modifier
            .size(38.dp)
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = type ?: "UC",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

/** Label/value row used in detail cards. */
@Composable
fun InfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(112.dp),
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Read-only dropdown field backed by a simple menu. */
@Composable
fun <T> DropdownField(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select…",
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No options available") }, onClick = { expanded = false })
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

/** Primary action button that shows a spinner while busy. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.medium,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Workflow timeline rendered from per-report history entries. */
@Composable
fun WorkflowTimeline(
    entries: List<HistoryEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        Text(
            text = "No workflow activity recorded yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    Column(modifier = modifier) {
        entries.forEachIndexed { index, entry ->
            val color = when (entry.action.uppercase()) {
                "APPROVE" -> MaterialTheme.colorScheme.tertiary
                "REJECT" -> MaterialTheme.colorScheme.error
                "CLOSE" -> MaterialTheme.colorScheme.primary
                "SUBMIT", "RESUBMIT" -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            ) {
                Column(
                    modifier = Modifier.width(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(9.dp)
                            .background(color, CircleShape),
                    )
                    if (index != entries.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, bottom = 14.dp),
                ) {
                    Text(
                        text = actionLabel(entry.action),
                        style = MaterialTheme.typography.titleSmall,
                        color = color,
                    )
                    if (!entry.remarks.isNullOrBlank()) {
                        Text(
                            text = entry.remarks,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = listOfNotNull(
                            entry.staffName?.takeIf { it.isNotBlank() },
                            formatTimestamp(entry.createdAt),
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }
    }
}

private fun actionLabel(action: String): String = when (action.uppercase()) {
    "SUBMIT" -> "Report submitted"
    "DRAFT" -> "Saved as draft"
    "EDIT" -> "Details updated"
    "APPROVE" -> "Approved"
    "REJECT" -> "Rejected"
    "CLOSE" -> "Report closed"
    "RESUBMIT" -> "Resubmitted"
    "PHOTO" -> "Evidence attached"
    else -> action.lowercase().replaceFirstChar { it.uppercase() }
}

/** Formats "2026-10-30 11:04:36" as "30 Oct 2026 · 11:04". */
fun formatTimestamp(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val datePart = raw.substringBefore(' ')
    val timePart = raw.substringAfter(' ', "")
    val pieces = datePart.split("-")
    if (pieces.size != 3) return raw
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    val monthIndex = pieces[1].toIntOrNull()?.minus(1) ?: return raw
    val month = months.getOrNull(monthIndex) ?: return raw
    val day = pieces[2].toIntOrNull() ?: return raw
    val time = if (timePart.length >= 5) " · ${timePart.take(5)}" else ""
    return "$day $month ${pieces[0]}$time"
}

/** Formats a date for list rows. */
fun formatDate(raw: String?): String = formatTimestamp(raw)
