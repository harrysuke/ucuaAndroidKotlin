package com.ucua.app.core.util

/**
 * One-shot hand-off for opening the reports list pre-filtered from a dashboard
 * counter (e.g. tapping "Awaiting approval").
 */
object ReportsFilterRequest {
    var pendingStatus: String? = null

    fun consume(): String? {
        val value = pendingStatus
        pendingStatus = null
        return value
    }
}
