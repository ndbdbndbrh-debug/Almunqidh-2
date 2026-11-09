package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PharmaDatabase
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.DrugEntity
import com.example.data.local.entity.PharmacistNoteEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.PrescriptionItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PharmaDatabaseRobolectricTest {

    private lateinit var db: PharmaDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PharmaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_read_company_and_drugs() = runBlocking {
        val company = CompanyEntity(
            id = "test_spimaco",
            nameAr = "سبيماكو السعودية",
            nameEn = "SPIMACO",
            country = "السعودية",
            agentName = "وكيل الخليج",
            badgeColor = 0xFF10B981
        )
        db.companyDao().insertCompany(company)

        val companies = db.companyDao().getAllCompanies().first()
        assertEquals(1, companies.size)
        assertEquals("سبيماكو السعودية", companies[0].nameAr)

        val drug = DrugEntity(
            id = "drug_101",
            companyId = "test_spimaco",
            tradeName = "سابوفين (Sapofen)",
            genericName = "Ibuprofen",
            strength = "400 mg",
            form = "أقراص",
            agent = "وكيل الخليج",
            standardDosage = "قرص كل 8 ساعات",
            commonInteractions = "تجنب مع الأسبرين",
            storageAndTips = "مع الطعام",
            supervisorNotes = "معتمد من د.ص أنس باقاضي"
        )
        db.drugDao().insertDrug(drug)

        val drugs = db.drugDao().getDrugsByCompany("test_spimaco").first()
        assertEquals(1, drugs.size)
        assertEquals("سابوفين (Sapofen)", drugs[0].tradeName)
    }

    @Test
    fun insert_full_prescription_and_query_relation() = runBlocking {
        val prescription = PrescriptionEntity(
            id = 55L,
            title = "روشتة عظام تجريبية",
            clinicOrDoctor = "عيادة المفاصل",
            date = "2026-11-08",
            isHandwritten = true,
            supervisorOverallComment = "روشتة ممتازة ومعتمدة"
        )
        val item = PrescriptionItemEntity(
            id = "item_1",
            prescriptionId = 55L,
            tradeName = "فولتارين",
            genericName = "Diclofenac Sodium",
            dosageForm = "حقن",
            strength = "75 mg",
            manufacturer = "نوفارتس",
            localAgent = "وكالة النور",
            writtenDosage = "أمبولة عضلية عند اللزوم",
            drugInteractions = listOf("تجنب مع المسكنات الأخرى"),
            medicalTips = listOf("بعد الأكل"),
            clinicalNotes = "ملاحظات صيدلانية دوائية معتمدة",
            supervisorDoctorNotes = "ملاحظات المشرف الدكتور الصيدلاني",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: تم الاعتماد",
            isApprovedBySupervisor = true
        )

        db.prescriptionDao().saveFullPrescription(prescription, listOf(item))

        val prescriptions = db.prescriptionDao().getAllPrescriptionsWithItems().first()
        assertEquals(1, prescriptions.size)
        assertEquals("روشتة عظام تجريبية", prescriptions[0].prescription.title)
        assertEquals(1, prescriptions[0].items.size)
        assertEquals("فولتارين", prescriptions[0].items[0].tradeName)
        assertEquals(1, prescriptions[0].items[0].drugInteractions.size)
    }

    @Test
    fun insert_and_read_pharmacist_supervisor_notes() = runBlocking {
        val note = PharmacistNoteEntity(
            targetType = "PRESCRIPTION",
            targetId = "55",
            supervisorName = "د.ص أنس يحيى باقاضي",
            noteText = "تم التأكد من سلامة الجرعة للمريض المسن",
            isApproved = true
        )
        val noteId = db.pharmacistNoteDao().insertNote(note)
        assertTrue(noteId > 0)

        val notes = db.pharmacistNoteDao().getNotesForTarget("PRESCRIPTION", "55").first()
        assertEquals(1, notes.size)
        assertEquals("د.ص أنس يحيى باقاضي", notes[0].supervisorName)
        assertEquals("تم التأكد من سلامة الجرعة للمريض المسن", notes[0].noteText)
    }
}
