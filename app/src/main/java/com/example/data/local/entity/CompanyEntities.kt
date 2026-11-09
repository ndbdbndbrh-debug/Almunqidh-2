package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val country: String,
    val agentName: String,
    val agentPhone: String = "",
    val website: String = "",
    val badgeColor: Long = 0xFF8E2DE2,
    val description: String = ""
)

@Entity(
    tableName = "company_drugs",
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["tradeName"]),
        Index(value = ["genericName"])
    ]
)
data class DrugEntity(
    @PrimaryKey
    val id: String,
    val companyId: String,
    val tradeName: String,
    val genericName: String,
    val strength: String,
    val form: String,
    val agent: String,
    val standardDosage: String,
    val commonInteractions: String,
    val storageAndTips: String,
    val priceEstimate: String = "",
    val leafletSummary: String = "",
    val supervisorNotes: String = "",
    val isSupervisorPriority: Boolean = false,
    val sourceOrigin: String = "SYSTEM"
)

@Entity(
    tableName = "company_archive_items",
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["isSupervisorPriority"])
    ]
)
data class ArchiveItemEntity(
    @PrimaryKey
    val id: String,
    val companyId: String,
    val title: String,
    val fileType: String,
    val summary: String,
    val dateAdded: String,
    val sourceUrlOrPath: String = "",
    val isVerifiedBySupervisor: Boolean = true,
    val isSupervisorPriority: Boolean = true,
    val sourceOrigin: String = "SUPERVISOR_UPLOAD"
)
