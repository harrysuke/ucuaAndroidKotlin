package com.ucua.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_reports")
data class ReportEntity(
    @PrimaryKey val id: Long,
    val staffId: String? = null,
    val formType: String? = null,
    val locationId: String? = null,
    val conditionDetails: String? = null,
    val offenceCode: String? = null,
    val violatorName: String? = null,
    val violatorCompany: String? = null,
    val submitDate: String? = null,
    val status: String? = null,
)
