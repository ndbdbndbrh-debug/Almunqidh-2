package com.example

import com.example.data.model.UserRole
import com.example.data.repository.PharmaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PharmaRepositoryTest {

    @Test
    fun repository_initializes_companies_and_drugs() {
        val repository = PharmaRepository()
        val companies = repository.companies.value
        val drugs = repository.drugs.value

        assertTrue(companies.isNotEmpty())
        assertTrue(drugs.isNotEmpty())
        assertTrue(companies.any { it.id == "sanofi" })
        assertTrue(companies.any { it.id == "novartis" })
        assertTrue(companies.any { it.id == "shaphaco" })
    }

    @Test
    fun repository_preset_prescription_contains_items() {
        val repository = PharmaRepository()
        val preset = repository.getPresetPrescription(0)

        assertNotNull(preset)
        assertTrue(preset.items.isNotEmpty())
        assertEquals("روشتة باطنية وصدرية (خط يدوي مائل)", preset.title)
    }

    @Test
    fun repository_update_supervisor_doctor_notes() {
        val repository = PharmaRepository()
        val initialScan = repository.getPresetPrescription(0)
        repository.setCurrentScan(initialScan)

        val firstItem = initialScan.items.first()
        val updatedNote = "ملاحظات المشرف الدكتور الصيدلاني د.ص أنس باقاضي"
        repository.updateSupervisorDoctorNotes(firstItem.id, updatedNote, true)

        val updatedScan = repository.currentScan.value
        assertNotNull(updatedScan)
        val modifiedItem = updatedScan?.items?.find { it.id == firstItem.id }
        assertEquals(updatedNote, modifiedItem?.supervisorDoctorNotes)
        assertTrue(modifiedItem?.isApprovedBySupervisor == true)
    }

    @Test
    fun user_role_permissions_verification() {
        val repository = PharmaRepository()
        assertEquals(UserRole.SUPERVISOR, repository.currentUserRole.value)
        assertTrue(repository.currentUserRole.value.canEdit)
        assertTrue(repository.currentUserRole.value.canDelete)

        repository.setUserRole(UserRole.COLLEAGUE)
        assertEquals(UserRole.COLLEAGUE, repository.currentUserRole.value)
        assertFalse(repository.currentUserRole.value.canEdit)
        assertFalse(repository.currentUserRole.value.canDelete)
    }

    @Test
    fun subscription_details_verification() {
        val repository = PharmaRepository()
        val subscription = repository.subscriptionDetails
        assertEquals("776028506", subscription.supervisorPhone)
        assertEquals("ndbdbndbrh@gmail.com", subscription.supervisorEmail)
        assertEquals("25 ريال سعودي", subscription.plan4MonthsPrice)
        assertEquals("50 ريال سعودي", subscription.planAnnualPrice)
        assertTrue(subscription.networks.any { it.name.contains("القطيبي") })
        assertTrue(subscription.networks.any { it.name.contains("الكريمي") })
    }
}
