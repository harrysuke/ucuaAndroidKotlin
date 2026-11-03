package com.ucua.app.core.repository

import com.ucua.app.core.model.DashboardResponse
import com.ucua.app.core.network.ApiService
import com.ucua.app.core.util.Resource
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardRepository(
    private val api: ApiService,
) {
    suspend fun load(): Resource<DashboardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.dashboard()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load dashboard (${response.code()})", response.code())
            }
        } catch (e: IOException) {
            Resource.Error("Network error while loading the dashboard.")
        } catch (e: Exception) {
            Resource.Error("Unexpected response from the server. Please try again.")
        }
    }
}
