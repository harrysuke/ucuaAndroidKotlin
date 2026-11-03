package com.ucua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ucua.app.core.model.ReportStatus
import com.ucua.app.ui.theme.StatusClosed
import com.ucua.app.ui.theme.StatusDraft
import com.ucua.app.ui.theme.StatusPending
import com.ucua.app.ui.theme.StatusRejected

/** Short, human label for a stored workflow status. */
fun statusLabel(status: String?): String = when {
    status.isNullOrBlank() -> "Unknown"
    status == ReportStatus.DRAFT -> "Draft"
    status == "Pending PSSD HSE Officer" -> "Pending HSE"
    status == "Pending HSE Reps" || status == "Updated by HSE Reps" -> "Pending HSE Reps"
    status == "Approved by PSSD HSE Officer" || status == "Approved by PSSD Officer" -> "Pending HOS"
    status == "Approved by HOS" -> "Pending HOD"
    ReportStatus.isClosed(status) -> "Closed"
    ReportStatus.isRejected(status) -> "Rejected"
    else -> status
}

/** Accent colour for a workflow status. */
fun statusColor(status: String?): Color = when {
    ReportStatus.isDraft(status) -> StatusDraft
    ReportStatus.isClosed(status) -> StatusClosed
    ReportStatus.isRejected(status) -> StatusRejected
    else -> StatusPending
}

/** Full-screen loading indicator. */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading…",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Full-screen empty state. */
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    title: String = "Nothing here yet",
    message: String = "New items will appear here.",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                modifier = Modifier.padding(top = 20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Text(actionLabel)
            }
        }
    }
}

/** Full-screen error state with a retry action. */
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Text("Try again")
            }
        }
    }
}

/** Inline loading spinner sized for refresh states. */
@Composable
fun SmallProgressIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(20.dp),
        strokeWidth = 2.dp,
    )
}

/** Compact status pill with a leading dot. */
@Composable
fun StatusChip(status: String?, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape),
            )
            Text(
                text = statusLabel(status),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/** Inline banner used for validation, rejection and success feedback. */
@Composable
fun InlineBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Error,
) {
    val color = when (tone) {
        BannerTone.Error -> MaterialTheme.colorScheme.error
        BannerTone.Warning -> MaterialTheme.colorScheme.secondary
        BannerTone.Success -> MaterialTheme.colorScheme.tertiary
        BannerTone.Info -> MaterialTheme.colorScheme.primary
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

enum class BannerTone { Error, Warning, Success, Info }
