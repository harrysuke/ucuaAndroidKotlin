package com.ucua.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Workflow statuses stored in `ucuaform.status` (legacy vocabulary). */
object ReportStatus {
    const val DRAFT = "Draft"
    const val PENDING_HSE = "Pending PSSD HSE Officer"
    const val PENDING_HOS = "Approved by PSSD HSE Officer"
    const val PENDING_HOD = "Approved by HOS"
    const val CLOSED = "Approved by HOD"
    const val REJECTED = "Rejected by PSSD HSE Officer"

    fun isDraft(status: String?): Boolean = status == DRAFT
    fun isRejected(status: String?): Boolean = status?.startsWith("Rejected") == true
    fun isClosed(status: String?): Boolean = status?.startsWith("Approved by HOD") == true
    fun isPending(status: String?): Boolean =
        !status.isNullOrBlank() && !isDraft(status) && !isClosed(status) && !isRejected(status)

    /** Statuses that count as the PSSD HSE review queue. */
    val HSE_QUEUE = listOf(PENDING_HSE, "Pending HSE Reps", "Updated by HSE Reps")
    val HOS_QUEUE = listOf(PENDING_HOS, "Approved by PSSD Officer")
    val HOD_QUEUE = listOf(PENDING_HOD)
}

object FormType {
    const val UC = "Unsafe Condition"
    const val UA = "Unsafe Action"
}

@Serializable
data class ReportPhoto(
    val id: Long = 0,
    val filename: String? = null,
    @SerialName("photo_type") val photoType: String? = null,
    val url: String? = null,
)

@Serializable
data class HistoryEntry(
    val id: Long = 0,
    val action: String = "",
    val remarks: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val staffId: String? = null,
    val staffName: String? = null,
)

@Serializable
data class Report(
    val id: Long = 0,
    val staffId: String? = null,
    @SerialName("form_type") val formType: String? = null,
    @SerialName("LocationId") val locationId: String? = null,
    val conditionDetails: String? = null,
    @SerialName("OffenceCode") val offenceCode: String? = null,
    @SerialName("ViolatorName") val violatorName: String? = null,
    val violatorCompany: String? = null,
    @SerialName("ViolatorDept") val violatorDept: String? = null,
    @SerialName("ActionTaken") val actionTaken: String? = null,
    val submitdate: String? = null,
    val status: String? = null,
    val observerName: String? = null,
    val observerDepartment: String? = null,
    val photos: List<ReportPhoto> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("can_approve") val canApprove: Boolean = false,
    @SerialName("is_owner") val isOwner: Boolean = false,
) {
    val isUnsafeCondition: Boolean get() = formType == FormType.UC
    val isUnsafeAction: Boolean get() = formType == FormType.UA
    val isDraft: Boolean get() = ReportStatus.isDraft(status)
    val isRejected: Boolean get() = ReportStatus.isRejected(status)
    val isClosed: Boolean get() = ReportStatus.isClosed(status)
    val isPending: Boolean get() = ReportStatus.isPending(status)

    val typeShort: String get() = if (isUnsafeAction) "UA" else "UC"

    val reference: String get() = "UCUA-%05d".format(id)

    val title: String
        get() = when {
            isUnsafeCondition -> "Unsafe Condition"
            isUnsafeAction -> "Unsafe Action"
            else -> "Hazard report"
        }

    val summary: String
        get() = when {
            !conditionDetails.isNullOrBlank() -> conditionDetails
            !violatorName.isNullOrBlank() -> "Violator: $violatorName"
            !offenceCode.isNullOrBlank() -> "Offence $offenceCode"
            else -> title
        }
}

@Serializable
data class ReportListResponse(
    val reports: List<Report> = emptyList(),
)

@Serializable
data class ReportDetailResponse(
    val report: Report? = null,
)

@Serializable
data class CreateReportRequest(
    val formType: String,
    val locationId: String = "",
    val conditionDetails: String? = null,
    val offenceCode: String? = null,
    val violatorName: String? = null,
    val violatorCompany: String? = null,
    val violatorDept: String? = null,
    val actionTaken: String? = null,
    val draft: Boolean = false,
)

@Serializable
data class CreateReportResponse(
    val created: Boolean = false,
    val id: Long = 0,
    val status: String? = null,
    val error: String? = null,
)

// null = keep existing value on the server (PATCH semantics).
@Serializable
data class UpdateReportRequest(
    val formType: String? = null,
    val locationId: String? = null,
    val conditionDetails: String? = null,
    val offenceCode: String? = null,
    val violatorName: String? = null,
    val violatorCompany: String? = null,
    val violatorDept: String? = null,
    val actionTaken: String? = null,
    val submit: Boolean = false,
)

@Serializable
data class UpdateReportResponse(
    val updated: Boolean = false,
    val id: Long = 0,
    val status: String? = null,
    val error: String? = null,
)

@Serializable
data class DeleteReportResponse(
    val deleted: Boolean = false,
    val id: Long = 0,
    val error: String? = null,
)

@Serializable
data class WorkflowRequest(
    val id: Long,
    val action: String,
    val remarks: String? = null,
)

@Serializable
data class WorkflowResponse(
    val updated: Boolean = false,
    val id: Long = 0,
    val status: String? = null,
    val error: String? = null,
)

@Serializable
data class PhotoUploadResponse(
    val uploaded: Boolean = false,
    val photo: ReportPhoto? = null,
    val error: String? = null,
)
