package com.ucua.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val staffId: String = "",
    val name: String = "",
    val phoneNo: String? = null,
    val department: String? = null,
    val designation: String? = null,
    val email: String? = null,
    val accessLevel: Int = 1,
    val roleName: String? = null,
    val companyId: Int? = null,
) {
    /** PSSD Admin (3) and above can see every report. */
    val canSeeAllReports: Boolean get() = accessLevel >= 3

    val canApproveHse: Boolean get() = accessLevel in listOf(3, 4, 9)
    val canApproveHos: Boolean get() = accessLevel in listOf(3, 6, 9)
    val canApproveHod: Boolean get() = accessLevel in listOf(3, 7, 9)
    val canClose: Boolean get() = accessLevel in listOf(3, 7, 9)
    val isApprover: Boolean get() = canApproveHse || canApproveHos || canApproveHod

    /** True when this user may act on the given report status. */
    fun canActOn(status: String?): Boolean = when {
        status in ReportStatus.HSE_QUEUE -> canApproveHse
        status in ReportStatus.HOS_QUEUE -> canApproveHos
        status in ReportStatus.HOD_QUEUE -> canApproveHod
        else -> false
    }

    val displayRole: String
        get() = roleName?.takeIf { it.isNotBlank() }
            ?: when (accessLevel) {
                1 -> "Employee"
                2 -> "Selected employee"
                3 -> "PSSD Admin"
                4 -> "PSSD HSE Officer"
                5 -> "PSSD Personnel"
                6 -> "PSSD HOS"
                7 -> "PSSD HOD"
                8 -> "HSE Representative"
                9 -> "ICT Admin"
                10 -> "HSE PCU"
                else -> "Tester"
            }

    val initials: String
        get() {
            val source = name.takeIf { it.isNotBlank() } ?: staffId
            return source.split(" ", ".")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifBlank { "U" }
        }
}

@Serializable
data class LoginRequest(
    val staffId: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val valid: Boolean = false,
    val token: String? = null,
    val user: User? = null,
    val error: String? = null,
)

@Serializable
data class RegisterRequest(
    val staffId: String,
    val name: String,
    val password: String,
    val email: String? = null,
    val phoneNo: String? = null,
    val department: String? = null,
    val designation: String? = null,
)

@Serializable
data class RegisterResponse(
    val registered: Boolean = false,
    val staffId: String? = null,
    val status: String? = null,
    val message: String? = null,
    val error: String? = null,
)

@Serializable
data class ForgotRequest(
    val staffId: String,
)

@Serializable
data class ForgotResponse(
    val requested: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@Serializable
data class ApiError(
    val error: String? = null,
    val detail: String? = null,
)
