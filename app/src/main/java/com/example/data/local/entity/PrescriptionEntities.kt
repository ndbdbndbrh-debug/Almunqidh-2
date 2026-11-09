package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val clinicOrDoctor: String,
    val date: String,
    val isHandwritten: Boolean = true,
    val confidence: String = "98.5%",
    val imagePath: String? = null,
    val supervisorOverallComment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "prescription_items",
    indices = [Index(value = ["prescriptionId"])]
)
data class PrescriptionItemEntity(
    @PrimaryKey
    val id: String,
    val prescriptionId: Long,
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
    val supervisorNotes: String = "",
    val isApprovedBySupervisor: Boolean = true
)

data class PrescriptionWithItems(
    @Embedded val prescription: PrescriptionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "prescriptionId"
    )
    val items: List<PrescriptionItemEntity>
)
