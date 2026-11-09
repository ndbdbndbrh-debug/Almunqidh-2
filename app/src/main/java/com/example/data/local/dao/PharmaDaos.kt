package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ArchiveItemEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.DrugEntity
import com.example.data.local.entity.PharmacistNoteEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.PrescriptionItemEntity
import com.example.data.local.entity.PrescriptionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {

    @Transaction
    @Query("SELECT * FROM prescriptions ORDER BY timestamp DESC")
    fun getAllPrescriptionsWithItems(): Flow<List<PrescriptionWithItems>>

    @Transaction
    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    fun getPrescriptionWithItemsById(id: Long): Flow<PrescriptionWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptionItems(items: List<PrescriptionItemEntity>)

    @Update
    suspend fun updatePrescription(prescription: PrescriptionEntity)

    @Query("UPDATE prescription_items SET supervisorDoctorNotes = :notes, supervisorNotes = :notes, isApprovedBySupervisor = :isApproved WHERE id = :itemId")
    suspend fun updateItemSupervisorNotes(itemId: String, notes: String, isApproved: Boolean)

    @Query("UPDATE prescription_items SET clinicalNotes = :clinicalNotes WHERE id = :itemId")
    suspend fun updateItemClinicalNotes(itemId: String, clinicalNotes: String)

    @Query("UPDATE prescriptions SET supervisorOverallComment = :notes WHERE id = :prescriptionId")
    suspend fun updatePrescriptionOverallComment(prescriptionId: Long, notes: String)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescriptionById(id: Long)

    @Query("DELETE FROM prescription_items WHERE prescriptionId = :prescriptionId")
    suspend fun deleteItemsByPrescriptionId(prescriptionId: Long)

    @Transaction
    suspend fun saveFullPrescription(prescription: PrescriptionEntity, items: List<PrescriptionItemEntity>): Long {
        val insertedId = insertPrescription(prescription)
        val linkedItems = items.map { it.copy(prescriptionId = insertedId) }
        insertPrescriptionItems(linkedItems)
        return insertedId
    }

    @Query("SELECT COUNT(*) FROM prescriptions")
    suspend fun getPrescriptionCount(): Int
}

@Dao
interface CompanyDao {

    @Query("SELECT * FROM companies ORDER BY nameAr ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE id = :id LIMIT 1")
    fun getCompanyById(id: String): Flow<CompanyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(companies: List<CompanyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity)

    @Query("SELECT COUNT(*) FROM companies")
    suspend fun getCompanyCount(): Int
}

@Dao
interface DrugDao {

    @Query("SELECT * FROM company_drugs ORDER BY isSupervisorPriority DESC, tradeName ASC")
    fun getAllDrugs(): Flow<List<DrugEntity>>

    @Query("SELECT * FROM company_drugs WHERE companyId = :companyId ORDER BY isSupervisorPriority DESC, tradeName ASC")
    fun getDrugsByCompany(companyId: String): Flow<List<DrugEntity>>

    @Query("SELECT * FROM company_drugs WHERE tradeName LIKE '%' || :query || '%' OR genericName LIKE '%' || :query || '%' OR agent LIKE '%' || :query || '%' ORDER BY isSupervisorPriority DESC, tradeName ASC")
    fun searchDrugs(query: String): Flow<List<DrugEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugs(drugs: List<DrugEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: DrugEntity)

    @Query("UPDATE company_drugs SET supervisorNotes = :notes WHERE id = :drugId")
    suspend fun updateDrugSupervisorNotes(drugId: String, notes: String)

    @Query("SELECT COUNT(*) FROM company_drugs")
    suspend fun getDrugCount(): Int
}

@Dao
interface ArchiveDao {

    @Query("SELECT * FROM company_archive_items ORDER BY isSupervisorPriority DESC, dateAdded DESC")
    fun getAllArchiveItems(): Flow<List<ArchiveItemEntity>>

    @Query("SELECT * FROM company_archive_items WHERE companyId = :companyId ORDER BY isSupervisorPriority DESC, dateAdded DESC")
    fun getArchiveByCompany(companyId: String): Flow<List<ArchiveItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchiveItems(items: List<ArchiveItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchiveItem(item: ArchiveItemEntity)

    @Query("SELECT COUNT(*) FROM company_archive_items")
    suspend fun getArchiveCount(): Int
}

@Dao
interface PharmacistNoteDao {

    @Query("SELECT * FROM pharmacist_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<PharmacistNoteEntity>>

    @Query("SELECT * FROM pharmacist_notes WHERE targetType = :targetType AND targetId = :targetId ORDER BY timestamp DESC")
    fun getNotesForTarget(targetType: String, targetId: String): Flow<List<PharmacistNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PharmacistNoteEntity): Long

    @Query("SELECT COUNT(*) FROM pharmacist_notes")
    suspend fun getNotesCount(): Int
}
