package com.ucua.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val LocationId: String = "",
    val LocationName: String = "",
)

@Serializable
data class Offence(
    val OffenceCode: String = "",
    val offKeyword: String? = null,
    val Offence: String? = null,
)

@Serializable
data class Company(
    val id: Long = 0,
    val companyName: String = "",
)

@Serializable
data class Department(
    val deptId: Long = 0,
    val dept: String = "",
    val deptName: String = "",
)

@Serializable
data class Lookups(
    val locations: List<Location> = emptyList(),
    val offences: List<Offence> = emptyList(),
    val companies: List<Company> = emptyList(),
    val departments: List<Department> = emptyList(),
)
