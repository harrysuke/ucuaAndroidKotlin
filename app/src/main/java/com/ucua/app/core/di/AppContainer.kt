package com.ucua.app.core.di

import android.content.Context
import com.ucua.app.core.database.AppDatabase
import com.ucua.app.core.network.NetworkModule
import com.ucua.app.core.repository.AuthRepository
import com.ucua.app.core.repository.DashboardRepository
import com.ucua.app.core.repository.LookupsRepository
import com.ucua.app.core.repository.ReportRepository
import com.ucua.app.core.security.TokenStore

/**
 * Manual dependency container (no DI framework needed).
 */
class AppContainer(context: Context) {

    val tokenStore: TokenStore by lazy { TokenStore(context.applicationContext) }

    private val apiService by lazy { NetworkModule.createApiService(tokenStore) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, tokenStore)
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepository(apiService, AppDatabase.getInstance(context.applicationContext).reportDao())
    }

    val lookupsRepository: LookupsRepository by lazy {
        LookupsRepository(apiService)
    }

    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(apiService)
    }
}
