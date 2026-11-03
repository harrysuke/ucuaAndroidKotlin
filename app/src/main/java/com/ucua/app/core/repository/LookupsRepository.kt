package com.ucua.app.core.repository

import com.ucua.app.core.model.Lookups
import com.ucua.app.core.network.ApiService
import com.ucua.app.core.util.Resource
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Reference data (locations, offences, companies, departments) cached in memory
 * so forms and report cards can resolve human-readable labels offline.
 */
class LookupsRepository(
    private val api: ApiService,
) {
    private val _lookups = MutableStateFlow(Lookups())
    val lookups: StateFlow<Lookups> = _lookups.asStateFlow()

    @Volatile
    private var loaded = false

    suspend fun load(force: Boolean = false): Resource<Lookups> {
        if (!force && loaded) return Resource.Success(_lookups.value)
        return withContext(Dispatchers.IO) {
            try {
                val response = api.lookups()
                if (response.isSuccessful && response.body() != null) {
                    _lookups.value = response.body()!!
                    loaded = true
                    Resource.Success(_lookups.value)
                } else {
                    Resource.Error("Failed to load reference data (${response.code()})", response.code())
                }
            } catch (e: IOException) {
                Resource.Error("Network error while loading reference data.")
            } catch (e: Exception) {
                Resource.Error("Unexpected response from the server. Please try again.")
            }
        }
    }

    fun locationName(id: String?): String {
        if (id.isNullOrBlank()) return "—"
        return _lookups.value.locations.firstOrNull { it.LocationId == id }?.LocationName ?: id
    }

    fun offenceLabel(code: String?): String {
        if (code.isNullOrBlank()) return "—"
        val offence = _lookups.value.offences.firstOrNull { it.OffenceCode == code }
        return offence?.Offence?.takeIf { it.isNotBlank() } ?: code
    }

    fun companyName(id: String?): String {
        if (id.isNullOrBlank()) return "—"
        return _lookups.value.companies
            .firstOrNull { it.id.toString() == id || it.companyName == id }
            ?.companyName ?: id
    }
}
