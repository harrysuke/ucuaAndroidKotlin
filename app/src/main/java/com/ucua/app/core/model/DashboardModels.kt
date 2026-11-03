package com.ucua.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardCounts(
    val uc: Int = 0,
    val ua: Int = 0,
    val drafts: Int = 0,
    val pending: Int = 0,
    val closed: Int = 0,
    val rejected: Int = 0,
)

@Serializable
data class QueueCounts(
    val hse: Int = 0,
    val hos: Int = 0,
    val hod: Int = 0,
) {
    val total: Int get() = hse + hos + hod
    val hasQueues: Boolean get() = total > 0
}

@Serializable
data class TrendPoint(
    val month: String = "",
    val label: String = "",
    val uc: Int = 0,
    val ua: Int = 0,
) {
    val total: Int get() = uc + ua
}

@Serializable
data class ActivityItem(
    val id: Long = 0,
    val action: String = "",
    val activity: String = "",
    val date: String? = null,
)

@Serializable
data class LeaderboardEntry(
    val staffId: String = "",
    val name: String = "",
    val total: Int = 0,
)

@Serializable
data class DashboardResponse(
    val counts: DashboardCounts = DashboardCounts(),
    val queues: QueueCounts = QueueCounts(),
    val trend: List<TrendPoint> = emptyList(),
    val activity: List<ActivityItem> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    @SerialName("accessLevel") val accessLevel: Int = 1,
)
