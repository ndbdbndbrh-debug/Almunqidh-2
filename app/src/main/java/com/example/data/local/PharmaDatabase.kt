package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converters.RoomConverters
import com.example.data.local.dao.ArchiveDao
import com.example.data.local.dao.CompanyDao
import com.example.data.local.dao.DrugDao
import com.example.data.local.dao.PharmacistNoteDao
import com.example.data.local.dao.PrescriptionDao
import com.example.data.local.entity.ArchiveItemEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.DrugEntity
import com.example.data.local.entity.PharmacistNoteEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.PrescriptionItemEntity

@Database(
    entities = [
        PrescriptionEntity::class,
        PrescriptionItemEntity::class,
        CompanyEntity::class,
        DrugEntity::class,
        ArchiveItemEntity::class,
        PharmacistNoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class PharmaDatabase : RoomDatabase() {

    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun companyDao(): CompanyDao
    abstract fun drugDao(): DrugDao
    abstract fun archiveDao(): ArchiveDao
    abstract fun pharmacistNoteDao(): PharmacistNoteDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaDatabase? = null

        fun getInstance(context: Context): PharmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaDatabase::class.java,
                    "pharma_savior.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
