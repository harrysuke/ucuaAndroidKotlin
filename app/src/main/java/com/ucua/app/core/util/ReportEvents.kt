package com.ucua.app.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Process-wide signal emitted whenever a report is created, updated or moved
 * through the approval workflow, so list/dashboard screens can refresh.
 */
object ReportEvents {

    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val changes: SharedFlow<Unit> = _changes

    fun notifyChanged() {
        _changes.tryEmit(Unit)
    }
}
