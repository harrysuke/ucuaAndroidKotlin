package com.ucua.app.ui.reports

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ucua.app.core.model.Report
import com.ucua.app.core.model.ReportStatus
import com.ucua.app.ui.components.BannerTone
import com.ucua.app.ui.components.ErrorState
import com.ucua.app.ui.components.InfoRow
import com.ucua.app.ui.components.InlineBanner
import com.ucua.app.ui.components.LoadingState
import com.ucua.app.ui.components.PrimaryButton
import com.ucua.app.ui.components.StatusChip
import com.ucua.app.ui.components.TypeBadge
import com.ucua.app.ui.components.WorkflowTimeline
import com.ucua.app.ui.components.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    viewModel: ReportDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showRejectDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullScreenPhoto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::load) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        val report = state.report
        when {
            state.loading && report == null -> LoadingState(message = "Loading report…")
            report == null -> ErrorState(
                message = state.error ?: "Report not found",
                onRetry = viewModel::load,
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { HeaderCard(report) }

                if (state.actionError != null) {
                    item {
                        InlineBanner(text = state.actionError!!, tone = BannerTone.Error)
                    }
                }
                if (state.actionMessage != null) {
                    item {
                        InlineBanner(text = state.actionMessage!!, tone = BannerTone.Success)
                    }
                }
                if (report.isRejected && !report.rejectionReason.isNullOrBlank()) {
                    item {
                        InlineBanner(
                            text = "Rejected: ${report.rejectionReason}",
                            tone = BannerTone.Error,
                        )
                    }
                }

                item { SummaryCard(report, state.locationName, state.offenceName) }

                item { PhotoCard(report, onPhotoClick = { fullScreenPhoto = it }) }

                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Workflow history",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "PSSD HSE Officer → HOS → HOD → Closed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                            )
                            WorkflowTimeline(entries = report.history)
                        }
                    }
                }

                if (state.canApprove || state.canEdit || state.canResubmit) {
                    item {
                        ActionCard(
                            state = state,
                            onApprove = viewModel::approve,
                            onReject = { showRejectDialog = true },
                            onClose = viewModel::close,
                            onEdit = { report.id.let(onEdit) },
                            onResubmit = viewModel::resubmit,
                            onDelete = { showDeleteDialog = true },
                        )
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        RejectDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                showRejectDialog = false
                viewModel.reject(reason)
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this report?") },
            text = { Text("This permanently removes the report and its evidence. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    fullScreenPhoto?.let { url ->
        Dialog(onDismissRequest = { fullScreenPhoto = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { fullScreenPhoto = null },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Evidence photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(report: Report) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeBadge(type = report.typeShort)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text(
                        text = report.reference,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                StatusChip(status = report.status)
            }
            Text(
                text = "Submitted ${formatTimestamp(report.submitdate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun SummaryCard(report: Report, locationName: String, offenceName: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (report.isUnsafeCondition) "Condition details" else "Violation details",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))

            if (report.isUnsafeCondition) {
                Text(
                    text = report.conditionDetails?.takeIf { it.isNotBlank() } ?: "No details provided.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                InfoRow("Offence", offenceName)
                InfoRow("Offence code", report.offenceCode)
                InfoRow("Violator", report.violatorName)
                InfoRow("Company", report.violatorCompany)
                InfoRow("Department", report.violatorDept)
                InfoRow("Action taken", report.actionTaken)
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )

            Spacer(Modifier.height(10.dp))

            InfoRow("Location", locationName)
            InfoRow("Reported by", report.staffId)
            InfoRow("Observer", report.observerName)
            InfoRow("Department", report.observerDepartment)
        }
    }
}

@Composable
private fun PhotoCard(report: Report, onPhotoClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Photo evidence",
                style = MaterialTheme.typography.titleMedium,
            )
            if (report.photos.isEmpty()) {
                Text(
                    text = "No photo evidence attached.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            } else {
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(report.photos) { photo ->
                        AsyncImage(
                            model = photo.url,
                            contentDescription = photo.filename,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { photo.url?.let(onPhotoClick) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    state: ReportDetailUiState,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onResubmit: () -> Unit,
    onDelete: () -> Unit,
) {
    val report = state.report ?: return
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (state.canApprove) "Review actions" else "Report actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            if (state.canApprove) {
                PrimaryButton(
                    text = approveLabel(report.status),
                    onClick = onApprove,
                    loading = state.acting,
                    icon = Icons.Default.Check,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onReject,
                    enabled = !state.acting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Reject with reason", color = MaterialTheme.colorScheme.error)
                }
                if (report.status == ReportStatus.PENDING_HOD) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onClose,
                        enabled = !state.acting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text("Close without further approval")
                    }
                }
            }

            if (state.canResubmit) {
                PrimaryButton(
                    text = "Resubmit for review",
                    onClick = onResubmit,
                    loading = state.acting,
                    icon = Icons.Default.Replay,
                )
            }

            if (state.canEdit) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !state.acting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Edit report", modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (state.canDelete) {
                Spacer(Modifier.height(6.dp))
                TextButton(
                    onClick = onDelete,
                    enabled = !state.acting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "Delete report",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RejectDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject report") },
        text = {
            Column {
                Text(
                    text = "A documented reason is required and will be sent back to the reporter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank(),
            ) {
                Text("Reject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun approveLabel(status: String?): String = when (status) {
    ReportStatus.PENDING_HSE -> "Approve & send to HOS"
    ReportStatus.PENDING_HOS -> "Approve & send to HOD"
    ReportStatus.PENDING_HOD -> "Approve & close report"
    else -> "Approve"
}
