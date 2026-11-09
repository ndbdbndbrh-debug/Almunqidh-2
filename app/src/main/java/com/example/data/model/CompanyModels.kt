package com.example.data.model

data class PharmaCompany(
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

data class CompanyDrug(
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
    val sourceOrigin: String = "SYSTEM" // "SUPERVISOR_UPLOAD", "FILE_ANALYSIS", "IMAGE_OCR", "LINK_EXTRACT", "FOLDER_IMPORT", "SYSTEM"
)

data class CompanyArchiveItem(
    val id: String,
    val companyId: String,
    val title: String,
    val fileType: ArchiveType,
    val summary: String,
    val dateAdded: String,
    val sourceUrlOrPath: String = "",
    val isVerifiedBySupervisor: Boolean = true,
    val isSupervisorPriority: Boolean = true,
    val sourceOrigin: String = "SUPERVISOR_UPLOAD"
)

enum class ArchiveType(val labelAr: String, val iconName: String) {
    LEAFLET("نشرة دوائية", "description"),
    PDF_FILE("ملف طبي PDF", "picture_as_pdf"),
    IMAGE("صورة أو عبوة دواء", "image"),
    WEB_LINK("رابط رسمي للشركة", "link"),
    RESEARCH("بحث أو دراسة سريرية", "biotech")
}
