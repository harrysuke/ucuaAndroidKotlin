package com.ucua.app.core.repository

import com.ucua.app.core.database.ReportDao
import com.ucua.app.core.database.ReportEntity
import com.ucua.app.core.model.ApiError
import com.ucua.app.core.model.CreateReportRequest
import com.ucua.app.core.model.Report
import com.ucua.app.core.model.ReportPhoto
import com.ucua.app.core.model.UpdateReportRequest
import com.ucua.app.core.model.WorkflowRequest
import com.ucua.app.core.network.ApiService
import com.ucua.app.core.util.Resource
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ReportRepository(
    private val api: ApiService,
    private val reportDao: ReportDao,
) {

    /** Local offline cache, observed by the UI when the network is unavailable. */
    fun observeCached(): Flow<List<ReportEntity>> = reportDao.observeAll()

    suspend fun refreshReports(
        type: String? = null,
        status: String? = null,
        query: String? = null,
        mine: Boolean = false,
    ): Resource<List<Report>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listReports(
                type = type,
                status = status,
                query = query?.takeIf { it.isNotBlank() },
                mine = if (mine) 1 else null,
            )
            if (response.isSuccessful) {
                val reports = response.body()?.reports ?: emptyList()
                if (type == null && status == null && query.isNullOrBlank() && !mine) {
                    reportDao.upsertAll(reports.map { it.toEntity() })
                }
                Resource.Success(reports)
            } else {
                Resource.Error(
                    parseApiError(response.errorBody()?.string())
                        ?: "Failed to load reports (${response.code()})",
                    response.code(),
                )
            }
        } catch (e: IOException) {
            Resource.Error("Network error while loading reports.")
        } catch (e: Exception) {
            Resource.Error("Unexpected response from the server. Please try again.")
        }
    }

    suspend fun getReportDetail(id: Long): Resource<Report?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getReport(id)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.report)
            } else {
                Resource.Error(
                    parseApiError(response.errorBody()?.string())
                        ?: "Failed to load report (${response.code()})",
                    response.code(),
                )
            }
        } catch (e: IOException) {
            Resource.Error("Network error while loading report.")
        } catch (e: Exception) {
            Resource.Error("Unexpected response from the server. Please try again.")
        }
    }

    suspend fun createReport(request: CreateReportRequest): Resource<Long> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createReport(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.created == true) {
                        Resource.Success(body.id)
                    } else {
                        Resource.Error(body?.error ?: "Report was not created")
                    }
                } else {
                    Resource.Error(
                        parseApiError(response.errorBody()?.string())
                            ?: "Failed to create report (${response.code()})",
                        response.code(),
                    )
                }
            } catch (e: IOException) {
                Resource.Error("Network error while submitting report.")
            } catch (e: Exception) {
                Resource.Error("Unexpected response from the server. Please try again.")
            }
        }

    suspend fun updateReport(
        id: Long,
        request: UpdateReportRequest,
    ): Resource<Long> = withContext(Dispatchers.IO) {
        try {
            var response = api.updateReport(id, request)
            if (response.code() == 405) response = api.patchReport(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.updated == true) {
                    reportDao.getById(id)?.let { cached ->
                        reportDao.upsert(
                            cached.copy(
                                formType = request.formType ?: cached.formType,
                                locationId = request.locationId ?: cached.locationId,
                                conditionDetails = request.conditionDetails ?: cached.conditionDetails,
                                offenceCode = request.offenceCode ?: cached.offenceCode,
                                violatorName = request.violatorName ?: cached.violatorName,
                                violatorCompany = request.violatorCompany ?: cached.violatorCompany,
                                status = body.status ?: cached.status,
                            ),
                        )
                    }
                    Resource.Success(if (body.id == 0L) id else body.id)
                } else {
                    Resource.Error(body?.error ?: "Report was not updated")
                }
            } else {
                Resource.Error(
                    parseApiError(response.errorBody()?.string())
                        ?: "Failed to update report (${response.code()})",
                    response.code(),
                )
            }
        } catch (e: IOException) {
            Resource.Error("Network error while updating report.")
        } catch (e: Exception) {
            Resource.Error("Unexpected response from the server. Please try again.")
        }
    }

    suspend fun deleteReport(id: Long): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteReport(id)
            if (response.isSuccessful && (response.body()?.deleted == true)) {
                reportDao.deleteById(id)
                Resource.Success(Unit)
            } else {
                Resource.Error(
                    parseApiError(response.errorBody()?.string())
                        ?: response.body()?.error
                        ?: "Failed to delete report (${response.code()})",
                    response.code(),
                )
            }
        } catch (e: IOException) {
            Resource.Error("Network error while deleting report.")
        } catch (e: Exception) {
            Resource.Error("Unexpected response from the server. Please try again.")
        }
    }

    /** approve | reject | close | resubmit */
    suspend fun workflowAction(
        id: Long,
        action: String,
        remarks: String? = null,
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.workflow(WorkflowRequest(id = id, action = action, remarks = remarks))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.updated == true) {
                    Resource.Success(body.status ?: "")
                } else {
                    Resource.Error(body?.error ?: "Workflow action failed")
                }
            } else {
                Resource.Error(
                    parseApiError(response.errorBody()?.string())
                        ?: "Workflow action failed (${response.code()})",
                    response.code(),
                )
            }
        } catch (e: IOException) {
            Resource.Error("Network error. Check your connection and try again.")
        } catch (e: Exception) {
            Resource.Error("Unexpected response from the server. Please try again.")
        }
    }

    suspend fun uploadPhoto(
        reportId: Long,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        photoType: String = "evidence",
    ): Resource<ReportPhoto> = withContext(Dispatchers.IO) {
        try {
            val fileBody = bytes.toRequestBody(mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData("file", fileName, fileBody)
            val response = api.uploadPhoto(
                reportId = reportId.toString().toRequestBody("text/plain".toMediaType()),
                photoType = photoType.toRequestBody("text/plain".toMediaType()),
                file = part,
            )
            if (response.isSuccessful && response.body()?.uploaded == true) {
                Resource.Success(response.body()?.photo ?: ReportPhoto())
            } else {
                Resource.Error(
                    parseApiError(response.errorBody()?.string())
                        ?: response.body()?.error
                        ?: "Photo upload failed (${response.code()})",
                    response.code(),
                )
            }
        } catch (e: IOException) {
            Resource.Error("Network error while uploading photo.")
        } catch (e: Exception) {
            Resource.Error("Unexpected response from the server. Please try again.")
        }
    }

    private fun parseApiError(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            Json { ignoreUnknownKeys = true }.decodeFromString<ApiError>(raw).error
        } catch (e: Exception) {
            null
        }
    }

    private fun Report.toEntity(): ReportEntity = ReportEntity(
        id = id,
        staffId = staffId,
        formType = formType,
        locationId = locationId,
        conditionDetails = conditionDetails,
        offenceCode = offenceCode,
        violatorName = violatorName,
        violatorCompany = violatorCompany,
        submitDate = submitdate,
        status = status,
    )
}
