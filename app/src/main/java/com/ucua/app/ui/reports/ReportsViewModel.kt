package com.ucua.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucua.app.core.model.FormType
import com.ucua.app.core.model.Report
import com.ucua.app.core.model.ReportStatus
import com.ucua.app.core.repository.LookupsRepository
import com.ucua.app.core.repository.ReportRepository
import com.ucua.app.core.util.ReportEvents
import com.ucua.app.core.util.ReportsFilterRequest
import com.ucua.app.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TypeFilter(val label: String, val value: String?) {
    ALL("All types", null),
    UC("Unsafe condition", FormType.UC),
    UA("Unsafe action", FormType.UA),
}

enum class StatusFilter(val label: String, val value: String?) {
    ALL("All", null),
    DRAFT("Drafts", ReportStatus.DRAFT),
    PENDING("Pending", null),
    CLOSED("Closed", ReportStatus.CLOSED),
    REJECTED("Rejected", ReportStatus.REJECTED),
}

data class ReportItem(
    val report: Report,
    val locationName: String,
    val offenceLabel: String,
)

data class ReportsUiState(
    val all: List<ReportItem> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val query: String = "",
) {
    val visible: List<ReportItem>
        get() {
            val needle = query.trim().lowercase()
            return all.filter { item ->
                val matchesType = typeFilter.value == null || item.report.formType == typeFilter.value
                val matchesStatus = when (statusFilter) {
                    StatusFilter.ALL -> true
                    StatusFilter.PENDING -> item.report.isPending
                    StatusFilter.DRAFT -> item.report.isDraft
                    StatusFilter.CLOSED -> item.report.isClosed
                    StatusFilter.REJECTED -> item.report.isRejected
                }
                val matchesQuery = needle.isEmpty() ||
                    item.report.conditionDetails?.lowercase()?.contains(needle) == true ||
                    item.report.violatorName?.lowercase()?.contains(needle) == true ||
                    item.report.offenceCode?.lowercase()?.contains(needle) == true ||
                    item.report.reference.lowercase().contains(needle) ||
                    item.locationName.lowercase().contains(needle)
                matchesType && matchesStatus && matchesQuery
            }
        }

    val isEmpty: Boolean get() = !loading && visible.isEmpty()
}

class ReportsViewModel(
    private val reportRepository: ReportRepository,
    private val lookupsRepository: LookupsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(statusFilter = initialStatusFilter()) }
        load()
        viewModelScope.launch {
            ReportEvents.changes.collect { load(isRefresh = true) }
        }
    }

    private fun initialStatusFilter(): StatusFilter =
        when (ReportsFilterRequest.consume()) {
            ReportStatus.PENDING_HSE,
            ReportStatus.PENDING_HOS,
            ReportStatus.PENDING_HOD,
            -> StatusFilter.PENDING
            ReportStatus.CLOSED -> StatusFilter.CLOSED
            ReportStatus.REJECTED -> StatusFilter.REJECTED
            ReportStatus.DRAFT -> StatusFilter.DRAFT
            else -> StatusFilter.ALL
        }

    fun load(isRefresh: Boolean = false) {
        if (_uiState.value.refreshing) return
        _uiState.update {
            it.copy(loading = !isRefresh && it.all.isEmpty(), refreshing = isRefresh, error = null)
        }
        viewModelScope.launch {
            lookupsRepository.load()
            when (val result = reportRepository.refreshReports()) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        all = result.data.map { report -> report.toItem() },
                        loading = false,
                        refreshing = false,
                        error = null,
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(loading = false, refreshing = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun setTypeFilter(filter: TypeFilter) {
        _uiState.update { it.copy(typeFilter = filter) }
    }

    fun setStatusFilter(filter: StatusFilter) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(typeFilter = TypeFilter.ALL, statusFilter = StatusFilter.ALL, query = "")
        }
    }

    private fun Report.toItem(): ReportItem = ReportItem(
        report = this,
        locationName = lookupsRepository.locationName(locationId),
        offenceLabel = lookupsRepository.offenceLabel(offenceCode),
    )
}
