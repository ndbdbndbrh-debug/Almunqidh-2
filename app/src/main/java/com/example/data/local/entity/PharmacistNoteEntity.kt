package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pharmacist_notes",
    indices = [
        Index(value = ["targetType", "targetId"]),
        Index(value = ["timestamp"])
    ]
)
data class PharmacistNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetType: String, // PRESCRIPTION, PRESCRIPTION_ITEM, DRUG, GENERAL
    val targetId: String,
    val supervisorName: String = "د.ص أنس يحيى باقاضي",
    val noteText: String,
    val isApproved: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
