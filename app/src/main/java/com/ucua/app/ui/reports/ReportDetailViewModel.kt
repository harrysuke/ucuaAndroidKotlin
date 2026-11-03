package com.ucua.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucua.app.core.model.Report
import com.ucua.app.core.model.ReportStatus
import com.ucua.app.core.repository.LookupsRepository
import com.ucua.app.core.repository.ReportRepository
import com.ucua.app.core.util.ReportEvents
import com.ucua.app.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportDetailUiState(
    val report: Report? = null,
    val locationName: String = "",
    val offenceName: String = "",
    val loading: Boolean = true,
    val error: String? = null,
    val acting: Boolean = false,
    val actionError: String? = null,
    val actionMessage: String? = null,
    val deleted: Boolean = false,
) {
    val canApprove: Boolean get() = report?.canApprove == true
    val canEdit: Boolean
        get() = report?.isOwner == true && (report?.isDraft == true || report?.isRejected == true)
    val canDelete: Boolean get() = canEdit
    val canResubmit: Boolean get() = report?.isOwner == true && report?.isRejected == true
}

class ReportDetailViewModel(
    private val reportRepository: ReportRepository,
    private val lookupsRepository: LookupsRepository,
    private val reportId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = it.report == null, error = null) }
        viewModelScope.launch {
            lookupsRepository.load()
            when (val result = reportRepository.getReportDetail(reportId)) {
                is Resource.Success -> {
                    val report = result.data
                    _uiState.update {
                        it.copy(
                            report = report,
                            locationName = lookupsRepository.locationName(report?.locationId),
                            offenceName = lookupsRepository.offenceLabel(report?.offenceCode),
                            loading = false,
                            error = if (report == null) "Report not found" else null,
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(loading = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun approve() = act("approve", null, "Report approved and moved to the next stage.")

    fun close() = act("close", null, "Report closed.")

    fun reject(reason: String) {
        if (reason.isBlank()) {
            _uiState.update { it.copy(actionError = "A rejection reason is required") }
            return
        }
        act("reject", reason, "Report rejected and returned to the submitter.")
    }

    fun resubmit() = act("resubmit", null, "Report resubmitted for review.")

    private fun act(action: String, remarks: String?, successMessage: String) {
        if (_uiState.value.acting) return
        _uiState.update { it.copy(acting = true, actionError = null, actionMessage = null) }
        viewModelScope.launch {
            when (val result = reportRepository.workflowAction(reportId, action, remarks)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(acting = false, actionMessage = successMessage, actionError = null)
                    }
                    ReportEvents.notifyChanged()
                    load()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(acting = false, actionError = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun delete() {
        if (_uiState.value.acting) return
        _uiState.update { it.copy(acting = true, actionError = null) }
        viewModelScope.launch {
            when (val result = reportRepository.deleteReport(reportId)) {
                is Resource.Success -> {
                    ReportEvents.notifyChanged()
                    _uiState.update { it.copy(acting = false, deleted = true) }
                }                is Resource.Error -> _uiState.update {
                    it.copy(acting = false, actionError = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(actionError = null, actionMessage = null) }
    }
}
