package com.example.data.model

data class PrescriptionItem(
    val id: String,
    val tradeName: String,
    val genericName: String,
    val dosageForm: String = "أقراص",
    val strength: String = "",
    val manufacturer: String = "غير محدد",
    val localAgent: String = "الوكيل المعتمد",
    val writtenDosage: String = "",
    val drugInteractions: List<String> = emptyList(),
    val medicalTips: List<String> = emptyList(),
    val clinicalNotes: String = "",
    val supervisorDoctorNotes: String = "",
    val isApprovedBySupervisor: Boolean = true,
    val supervisorNotes: String = supervisorDoctorNotes.ifBlank { clinicalNotes }
)

data class PrescriptionScan(
    val id: Long = System.currentTimeMillis(),
    val title: String = "وصفة طبية جديدة",
    val clinicOrDoctor: String = "عيادة الباطنية والقلب",
    val date: String = "2026-11-08",
    val isHandwritten: Boolean = true,
    val confidence: String = "98.5%",
    val imagePath: String? = null,
    val items: List<PrescriptionItem> = emptyList(),
    val supervisorOverallComment: String = "تمت المراجعة والتدقيق الصيدلاني بواسطة د.ص أنس يحيى باقاضي - الوصفة متوافقة وتتطلب مراقبة ضغط الدم.",
    val isApprovedBySupervisor: Boolean = true
)
