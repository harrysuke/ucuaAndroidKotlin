package com.ucua.app.ui.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ucua.app.core.model.DashboardResponse
import com.ucua.app.core.model.ReportStatus
import com.ucua.app.core.model.User
import com.ucua.app.ui.components.ErrorState
import com.ucua.app.ui.components.KpiCard
import com.ucua.app.ui.components.LoadingState
import com.ucua.app.ui.components.SectionHeader
import com.ucua.app.ui.components.TrendChart
import com.ucua.app.ui.components.formatTimestamp
import com.ucua.app.ui.theme.UcuaAccent
import com.ucua.app.ui.theme.UcuaInfo
import com.ucua.app.ui.theme.UcuaPrimary
import com.ucua.app.ui.theme.UcuaPrimaryDark
import com.ucua.app.ui.theme.UcuaSuccess
import com.ucua.app.ui.theme.UcuaWarning
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onReportUc: () -> Unit,
    onReportUa: () -> Unit,
    onViewReports: (String?) -> Unit,
    onProfile: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.loading && state.data == null -> LoadingState(message = "Loading your dashboard…")
        state.error != null && state.data == null -> ErrorState(
            message = state.error!!,
            onRetry = { viewModel.load() },
        )
        else -> DashboardContent(
            user = state.user,
            data = state.data,
            refreshing = state.refreshing,
            onRefresh = { viewModel.load(isRefresh = true) },
            onReportUc = onReportUc,
            onReportUa = onReportUa,
            onViewReports = onViewReports,
            onProfile = onProfile,
        )
    }
}

@Composable
private fun DashboardContent(
    user: User?,
    data: DashboardResponse?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onReportUc: () -> Unit,
    onReportUa: () -> Unit,
    onViewReports: (String?) -> Unit,
    onProfile: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GreetingRow(
                user = user,
                refreshing = refreshing,
                onRefresh = onRefresh,
                onProfile = onProfile,
            )
        }

        item { HeroCard(onReportUc = onReportUc, onReportUa = onReportUa) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    label = "My unsafe conditions",
                    value = data?.counts?.uc ?: 0,
                    icon = Icons.Default.Warning,
                    accent = UcuaPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onViewReports(null) },
                )
                KpiCard(
                    label = "My unsafe actions",
                    value = data?.counts?.ua ?: 0,
                    icon = Icons.Default.Person,
                    accent = UcuaAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { onViewReports(null) },
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    label = "Awaiting approval",
                    value = data?.counts?.pending ?: 0,
                    icon = Icons.Default.Schedule,
                    accent = UcuaWarning,
                    modifier = Modifier.weight(1f),
                    onClick = { onViewReports(ReportStatus.PENDING_HSE) },
                )
                KpiCard(
                    label = "Closed reports",
                    value = data?.counts?.closed ?: 0,
                    icon = Icons.Default.CheckCircle,
                    accent = UcuaSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { onViewReports(ReportStatus.CLOSED) },
                )
            }
        }

        val queues = data?.queues
        if (queues != null && queues.hasQueues) {
            item {
                ApprovalQueueCard(
                    hse = queues.hse,
                    hos = queues.hos,
                    hod = queues.hod,
                    onClick = { status -> onViewReports(status) },
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Submission trend",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Your UC and UA reports over the last six months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    TrendChart(points = data?.trend ?: emptyList())
                }
            }
        }

        item {
            SectionHeader(
                title = "Recent activity",
                actionLabel = "My reports",
                onAction = { onViewReports(null) },
            )
        }

        val activity = data?.activity.orEmpty()
        if (activity.isEmpty()) {
            item {
                Text(
                    text = "No activity yet. Your submissions and approvals will appear here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(activity.size) { index ->
                val item = activity[index]
                ActivityRow(
                    title = item.activity.ifBlank { item.action },
                    action = item.action,
                    date = item.date,
                )
            }
        }

        val leaderboard = data?.leaderboard.orEmpty()
        if (leaderboard.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = UcuaAccent,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Participation leaderboard",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Text(
                            text = "Top reporters across the port",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                        )
                        leaderboard.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (index == 0) UcuaAccent else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp),
                                )
                                Text(
                                    text = "${entry.total}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingRow(
    user: User?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(UcuaPrimary, CircleShape)
                .clickable(onClick = onProfile),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user?.initials ?: "U",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = greeting(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = user?.name?.takeIf { it.isNotBlank() } ?: "Welcome",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = user?.displayRole ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(onClick = onRefresh, enabled = !refreshing) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh dashboard",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun HeroCard(onReportUc: () -> Unit, onReportUa: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(UcuaPrimaryDark, UcuaPrimary)),
                RoundedCornerShape(20.dp),
            )
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = "Safety starts with you",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Text(
                text = "Spotted an unsafe condition or action? Report it with photo evidence in under a minute.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroAction(
                    label = "Report UC",
                    modifier = Modifier.weight(1f),
                    onClick = onReportUc,
                    filled = true,
                )
                HeroAction(
                    label = "Report UA",
                    modifier = Modifier.weight(1f),
                    onClick = onReportUa,
                    filled = false,
                )
            }
        }
    }
}

@Composable
private fun HeroAction(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    filled: Boolean,
) {
    Box(
        modifier = modifier
            .background(
                if (filled) Color.White else Color.White.copy(alpha = 0.14f),
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (filled) UcuaPrimaryDark else Color.White,
        )
    }
}

@Composable
private fun ApprovalQueueCard(
    hse: Int,
    hos: Int,
    hod: Int,
    onClick: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your approval queue",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Reports waiting for your review",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            QueueRow(
                label = "PSSD HSE Officer",
                count = hse,
                color = UcuaInfo,
                onClick = { onClick(ReportStatus.PENDING_HSE) },
            )
            QueueRow(
                label = "Head of Section",
                count = hos,
                color = UcuaWarning,
                onClick = { onClick(ReportStatus.PENDING_HOS) },
            )
            QueueRow(
                label = "Head of Department",
                count = hod,
                color = UcuaSuccess,
                onClick = { onClick(ReportStatus.PENDING_HOD) },
            )
        }
    }
}

@Composable
private fun QueueRow(
    label: String,
    count: Int,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = color,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(11.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivityRow(title: String, action: String, date: String?) {
    val color = when (action.uppercase()) {
        "APPROVE" -> UcuaSuccess
        "REJECT" -> MaterialTheme.colorScheme.error
        "SUBMIT", "RESUBMIT" -> UcuaAccent
        else -> UcuaInfo
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(color, CircleShape),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatTimestamp(date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning,"
        hour < 15 -> "Good afternoon,"
        hour < 19 -> "Good evening,"
        else -> "Good night,"
    }
}
