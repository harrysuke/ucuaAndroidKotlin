package com.ucua.app.core.network

import com.ucua.app.core.model.CreateReportRequest
import com.ucua.app.core.model.CreateReportResponse
import com.ucua.app.core.model.DashboardResponse
import com.ucua.app.core.model.DeleteReportResponse
import com.ucua.app.core.model.ForgotRequest
import com.ucua.app.core.model.ForgotResponse
import com.ucua.app.core.model.LoginRequest
import com.ucua.app.core.model.LoginResponse
import com.ucua.app.core.model.Lookups
import com.ucua.app.core.model.PhotoUploadResponse
import com.ucua.app.core.model.RegisterRequest
import com.ucua.app.core.model.RegisterResponse
import com.ucua.app.core.model.ReportDetailResponse
import com.ucua.app.core.model.ReportListResponse
import com.ucua.app.core.model.UpdateReportRequest
import com.ucua.app.core.model.UpdateReportResponse
import com.ucua.app.core.model.WorkflowRequest
import com.ucua.app.core.model.WorkflowResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("login.php")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("register.php")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("forgot.php")
    suspend fun forgotPassword(@Body body: ForgotRequest): Response<ForgotResponse>

    @GET("dashboard.php")
    suspend fun dashboard(): Response<DashboardResponse>

    @GET("lookups.php")
    suspend fun lookups(): Response<Lookups>

    // --- Read: list + single (CRUD-R) ---
    @GET("reports.php")
    suspend fun listReports(
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("q") query: String? = null,
        @Query("mine") mine: Int? = null,
    ): Response<ReportListResponse>

    @GET("reports.php")
    suspend fun getReport(@Query("id") id: Long): Response<ReportDetailResponse>

    // --- Create (CRUD-C) ---
    @POST("reports.php")
    suspend fun createReport(@Body body: CreateReportRequest): Response<CreateReportResponse>

    // --- Update: full (PUT) + partial (PATCH), owner or admin (CRUD-U) ---
    @PUT("reports.php")
    suspend fun updateReport(
        @Query("id") id: Long,
        @Body body: UpdateReportRequest,
    ): Response<UpdateReportResponse>

    @PATCH("reports.php")
    suspend fun patchReport(
        @Query("id") id: Long,
        @Body body: UpdateReportRequest,
    ): Response<UpdateReportResponse>

    // --- Delete: owner or admin (CRUD-D) ---
    @DELETE("reports.php")
    suspend fun deleteReport(@Query("id") id: Long): Response<DeleteReportResponse>

    // --- Approval workflow ---
    @POST("workflow.php")
    suspend fun workflow(@Body body: WorkflowRequest): Response<WorkflowResponse>

    // --- Photo evidence ---
    @Multipart
    @POST("photos.php")
    suspend fun uploadPhoto(
        @Part("reportId") reportId: RequestBody,
        @Part("photoType") photoType: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<PhotoUploadResponse>
}
