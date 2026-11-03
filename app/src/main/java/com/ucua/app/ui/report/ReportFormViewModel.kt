package com.ucua.app.ui.report

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucua.app.core.model.CreateReportRequest
import com.ucua.app.core.model.FormType
import com.ucua.app.core.model.Lookups
import com.ucua.app.core.model.Report
import com.ucua.app.core.model.ReportPhoto
import com.ucua.app.core.model.ReportStatus
import com.ucua.app.core.model.UpdateReportRequest
import com.ucua.app.core.repository.LookupsRepository
import com.ucua.app.core.repository.ReportRepository
import com.ucua.app.core.util.ReportEvents
import com.ucua.app.core.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A photo selected on the device, held in memory until the report is created. */
data class PendingPhoto(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingPhoto) return false
        return fileName == other.fileName && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = 31 * fileName.hashCode() + bytes.contentHashCode()
}

sealed interface FormResult {
    data class Submitted(
        val id: Long,
        val reference: String,
        val photoFailures: Int,
    ) : FormResult

    data class DraftSaved(val id: Long) : FormResult
}

data class ReportFormUiState(
    val formType: String,
    val locationId: String = "",
    val conditionDetails: String = "",
    val offenceCode: String = "",
    val violatorName: String = "",
    val violatorCompany: String = "",
    val violatorDept: String = "",
    val actionTaken: String = "",
    val photos: List<PendingPhoto> = emptyList(),
    val existingPhotos: List<ReportPhoto> = emptyList(),
    val lookups: Lookups = Lookups(),
    val loadingLookups: Boolean = true,
    val lookupsError: String? = null,
    val submitting: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val submitError: String? = null,
    val result: FormResult? = null,
    val editingId: Long? = null,
    val editingStatus: String? = null,
) {
    val isEditing: Boolean get() = editingId != null
    val isUnsafeCondition: Boolean get() = formType == FormType.UC
    val photoCount: Int get() = photos.size + existingPhotos.size
    val canSubmit: Boolean get() = !submitting
}

class ReportFormViewModel(
    private val reportRepository: ReportRepository,
    private val lookupsRepository: LookupsRepository,
    formType: String,
    private val editReportId: Long? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportFormUiState(formType = formType))
    val uiState: StateFlow<ReportFormUiState> = _uiState.asStateFlow()

    init {
        loadLookups()
        if (editReportId != null) loadExistingReport(editReportId)
    }

    private fun loadLookups(force: Boolean = false) {
        viewModelScope.launch {
            when (val result = lookupsRepository.load(force)) {
                is Resource.Success -> _uiState.update {
                    it.copy(lookups = result.data, loadingLookups = false, lookupsError = null)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(loadingLookups = false, lookupsError = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadExistingReport(id: Long) {
        viewModelScope.launch {
            when (val result = reportRepository.getReportDetail(id)) {
                is Resource.Success -> {
                    val report = result.data
                    if (report == null) {
                        _uiState.update { it.copy(submitError = "Report not found") }
                        return@launch
                    }
                    _uiState.update {
                        it.copy(
                            formType = report.formType ?: it.formType,
                            locationId = report.locationId.orEmpty(),
                            conditionDetails = report.conditionDetails.orEmpty(),
                            offenceCode = report.offenceCode.orEmpty(),
                            violatorName = report.violatorName.orEmpty(),
                            violatorCompany = report.violatorCompany.orEmpty(),
                            violatorDept = report.violatorDept.orEmpty(),
                            actionTaken = report.actionTaken.orEmpty(),
                            existingPhotos = report.photos,
                            editingId = report.id,
                            editingStatus = report.status,
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(submitError = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun setField(field: String, value: String) {
        _uiState.update { current ->
            val next = when (field) {
                "locationId" -> current.copy(locationId = value)
                "conditionDetails" -> current.copy(conditionDetails = value)
                "offenceCode" -> current.copy(offenceCode = value)
                "violatorName" -> current.copy(violatorName = value)
                "violatorCompany" -> current.copy(violatorCompany = value)
                "violatorDept" -> current.copy(violatorDept = value)
                "actionTaken" -> current.copy(actionTaken = value)
                else -> current
            }
            next.copy(fieldErrors = next.fieldErrors - field, submitError = null)
        }
    }

    fun addPhotos(resolver: ContentResolver, uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> readPhoto(resolver, uri) }
            }
            if (loaded.isEmpty()) return@launch
            _uiState.update { current ->
                val remaining = (5 - current.photoCount).coerceAtLeast(0)
                current.copy(photos = current.photos + loaded.take(remaining))
            }
        }
    }

    private fun readPhoto(resolver: ContentResolver, uri: Uri): PendingPhoto? = try {
        val mime = resolver.getType(uri) ?: "image/jpeg"
        var name = "evidence_${System.currentTimeMillis()}.jpg"
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(index) ?: name
            }
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        when {
            bytes == null -> null
            bytes.size > MAX_PHOTO_BYTES -> null
            else -> PendingPhoto(bytes = bytes, mimeType = mime, fileName = name)
        }
    } catch (e: Exception) {
        null
    }

    fun removePhoto(index: Int) {
        _uiState.update { current ->
            if (index in current.photos.indices) {
                current.copy(photos = current.photos.filterIndexed { i, _ -> i != index })
            } else {
                current
            }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(result = null) }
    }

    fun saveDraft() {
        // LocationId is a required foreign key even for drafts.
        val state = _uiState.value
        if (state.locationId.isBlank()) {
            _uiState.update {
                it.copy(fieldErrors = mapOf("locationId" to "Select the location before saving"))
            }
            return
        }
        persist(draft = true)
    }

    fun submit() {
        val errors = validate(_uiState.value)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }
        persist(draft = false)
    }

    private fun validate(state: ReportFormUiState): Map<String, String> = buildMap {
        if (state.locationId.isBlank()) put("locationId", "Select the location")
        if (state.isUnsafeCondition) {
            if (state.conditionDetails.isBlank()) {
                put("conditionDetails", "Describe the unsafe condition")
            }
        } else {
            if (state.offenceCode.isBlank()) put("offenceCode", "Select the offence code")
            if (state.violatorCompany.isBlank()) put("violatorCompany", "Select the violator company")
        }
    }

    private fun persist(draft: Boolean) {
        val state = _uiState.value
        if (state.submitting) return
        _uiState.update { it.copy(submitting = true, submitError = null) }

        viewModelScope.launch {
            val reportId = if (state.editingId != null) {
                updateExisting(state, draft)
            } else {
                createNew(state, draft)
            }

            if (reportId == null) {
                _uiState.update { it.copy(submitting = false) }
                return@launch
            }

            val failures = uploadPendingPhotos(reportId, state.photos)
            ReportEvents.notifyChanged()

            if (draft) {
                _uiState.update {
                    it.copy(
                        submitting = false,
                        result = FormResult.DraftSaved(reportId),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        submitting = false,
                        result = FormResult.Submitted(
                            id = reportId,
                            reference = "UCUA-%05d".format(reportId),
                            photoFailures = failures,
                        ),
                    )
                }
            }
        }
    }

    private suspend fun createNew(state: ReportFormUiState, draft: Boolean): Long? {
        val request = CreateReportRequest(
            formType = state.formType,
            locationId = state.locationId,
            conditionDetails = state.conditionDetails.takeIf { !state.isUnsafeCondition || it.isNotBlank() },
            offenceCode = state.offenceCode.takeIf { !state.isUnsafeCondition },
            violatorName = state.violatorName.takeIf { !state.isUnsafeCondition && it.isNotBlank() },
            violatorCompany = state.violatorCompany.takeIf { !state.isUnsafeCondition },
            violatorDept = state.violatorDept.takeIf { !state.isUnsafeCondition && it.isNotBlank() },
            actionTaken = state.actionTaken.takeIf { !state.isUnsafeCondition && it.isNotBlank() },
            draft = draft,
        )
        return when (val result = reportRepository.createReport(request)) {
            is Resource.Success -> result.data
            is Resource.Error -> {
                _uiState.update { it.copy(submitError = result.message) }
                null
            }
            Resource.Loading -> null
        }
    }

    private suspend fun updateExisting(state: ReportFormUiState, draft: Boolean): Long? {
        val id = state.editingId ?: return null
        val request = UpdateReportRequest(
            formType = state.formType,
            locationId = state.locationId,
            conditionDetails = state.conditionDetails,
            offenceCode = state.offenceCode.takeIf { !state.isUnsafeCondition },
            violatorName = state.violatorName.takeIf { !state.isUnsafeCondition },
            violatorCompany = state.violatorCompany.takeIf { !state.isUnsafeCondition },
            violatorDept = state.violatorDept.takeIf { !state.isUnsafeCondition },
            actionTaken = state.actionTaken.takeIf { !state.isUnsafeCondition },
            submit = !draft,
        )
        return when (val result = reportRepository.updateReport(id, request)) {
            is Resource.Success -> result.data
            is Resource.Error -> {
                _uiState.update { it.copy(submitError = result.message) }
                null
            }
            Resource.Loading -> null
        }
    }

    private suspend fun uploadPendingPhotos(reportId: Long, photos: List<PendingPhoto>): Int {
        var failures = 0
        for (photo in photos) {
            val result = reportRepository.uploadPhoto(
                reportId = reportId,
                bytes = photo.bytes,
                fileName = photo.fileName,
                mimeType = photo.mimeType,
            )
            if (result is Resource.Error) failures++
        }
        if (failures > 0) {
            _uiState.update {
                it.copy(
                    submitError = "$failures photo(s) could not be uploaded. The report itself was saved.",
                )
            }
        }
        return failures
    }

    companion object {
        private const val MAX_PHOTO_BYTES = 8 * 1024 * 1024
    }
}
