package com.example.data.repository

import com.example.data.local.PharmaDatabase
import com.example.data.local.entity.ArchiveItemEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.DrugEntity
import com.example.data.local.entity.PharmacistNoteEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.PrescriptionItemEntity
import com.example.data.local.entity.PrescriptionWithItems
import com.example.data.model.ArchiveType
import com.example.data.model.CompanyArchiveItem
import com.example.data.model.CompanyDrug
import com.example.data.model.PharmaCompany
import com.example.data.model.PrescriptionItem
import com.example.data.model.PrescriptionScan
import com.example.data.model.SubscriptionDetails
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PharmaRepository(
    private val database: PharmaDatabase? = null,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    val subscriptionDetails = SubscriptionDetails()

    private val _currentUserRole = MutableStateFlow<UserRole>(UserRole.SUPERVISOR)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
    }

    private val _currentScan = MutableStateFlow<PrescriptionScan?>(null)
    val currentScan: StateFlow<PrescriptionScan?> = _currentScan.asStateFlow()

    // Fallback in-memory state flows if running in memory-only test mode
    private val _memCompanies = MutableStateFlow<List<PharmaCompany>>(emptyList())
    private val _memDrugs = MutableStateFlow<List<CompanyDrug>>(emptyList())
    private val _memArchiveItems = MutableStateFlow<List<CompanyArchiveItem>>(emptyList())
    private val _memPrescriptionsHistory = MutableStateFlow<List<PrescriptionScan>>(emptyList())

    // Database backed flows if database is present, otherwise fallback
    val companies: StateFlow<List<PharmaCompany>> = if (database != null) {
        database.companyDao().getAllCompanies()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())
    } else {
        _memCompanies.asStateFlow()
    }

    val drugs: StateFlow<List<CompanyDrug>> = if (database != null) {
        database.drugDao().getAllDrugs()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())
    } else {
        _memDrugs.asStateFlow()
    }

    val archiveItems: StateFlow<List<CompanyArchiveItem>> = if (database != null) {
        database.archiveDao().getAllArchiveItems()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())
    } else {
        _memArchiveItems.asStateFlow()
    }

    val prescriptionsHistory: StateFlow<List<PrescriptionScan>> = if (database != null) {
        database.prescriptionDao().getAllPrescriptionsWithItems()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(externalScope, SharingStarted.Eagerly, emptyList())
    } else {
        _memPrescriptionsHistory.asStateFlow()
    }

    init {
        val defaultPreset = getPresetPrescription(0)
        _currentScan.value = defaultPreset

        if (database != null) {
            externalScope.launch {
                seedDatabaseIfEmpty(defaultPreset)
            }
        } else {
            seedInMemory(defaultPreset)
        }
    }

    private suspend fun seedDatabaseIfEmpty(defaultPreset: PrescriptionScan) {
        val db = database ?: return
        if (db.companyDao().getCompanyCount() == 0) {
            val companiesToInsert = getInitialCompanies().map { it.toEntity() }
            db.companyDao().insertCompanies(companiesToInsert)
        }
        if (db.drugDao().getDrugCount() == 0) {
            val drugsToInsert = getInitialDrugs().map { it.toEntity() }
            db.drugDao().insertDrugs(drugsToInsert)
        }
        if (db.archiveDao().getArchiveCount() == 0) {
            val archiveToInsert = getInitialArchive().map { it.toEntity() }
            db.archiveDao().insertArchiveItems(archiveToInsert)
        }
        if (db.prescriptionDao().getPrescriptionCount() == 0) {
            savePrescriptionToDatabase(defaultPreset)
            savePrescriptionToDatabase(getPresetPrescription(1))
            savePrescriptionToDatabase(getPresetPrescription(2))
        }
    }

    private fun seedInMemory(defaultPreset: PrescriptionScan) {
        _memCompanies.value = getInitialCompanies()
        _memDrugs.value = getInitialDrugs()
        _memArchiveItems.value = getInitialArchive()
        _memPrescriptionsHistory.value = listOf(
            defaultPreset,
            getPresetPrescription(1),
            getPresetPrescription(2)
        )
    }

    fun setCurrentScan(scan: PrescriptionScan) {
        _currentScan.value = scan
        if (database != null) {
            externalScope.launch {
                savePrescriptionToDatabase(scan)
            }
        } else {
            _memPrescriptionsHistory.update { current ->
                if (current.any { it.id == scan.id }) current else listOf(scan) + current
            }
        }
    }

    private suspend fun savePrescriptionToDatabase(scan: PrescriptionScan) {
        val db = database ?: return
        val presEntity = PrescriptionEntity(
            id = if (scan.id == 0L) System.currentTimeMillis() else scan.id,
            title = scan.title,
            clinicOrDoctor = scan.clinicOrDoctor,
            date = scan.date,
            isHandwritten = scan.isHandwritten,
            confidence = scan.confidence,
            imagePath = scan.imagePath,
            supervisorOverallComment = scan.supervisorOverallComment
        )
        val itemEntities = scan.items.map { it.toEntity(presEntity.id) }
        db.prescriptionDao().saveFullPrescription(presEntity, itemEntities)
    }

    fun updateSupervisorNotesForItem(itemId: String, notes: String, isApproved: Boolean) {
        val current = _currentScan.value ?: return
        val updatedItems = current.items.map { item ->
            if (item.id == itemId) {
                item.copy(supervisorNotes = notes, isApprovedBySupervisor = isApproved)
            } else {
                item
            }
        }
        _currentScan.value = current.copy(items = updatedItems)

        if (database != null) {
            externalScope.launch {
                database.prescriptionDao().updateItemSupervisorNotes(itemId, notes, isApproved)
                database.pharmacistNoteDao().insertNote(
                    PharmacistNoteEntity(
                        targetType = "PRESCRIPTION_ITEM",
                        targetId = itemId,
                        noteText = notes,
                        isApproved = isApproved
                    )
                )
            }
        }
    }

    fun updateSupervisorDoctorNotes(itemId: String, doctorNotes: String, isApproved: Boolean = true) {
        val current = _currentScan.value ?: return
        val updatedItems = current.items.map { item ->
            if (item.id == itemId) {
                item.copy(supervisorDoctorNotes = doctorNotes, isApprovedBySupervisor = isApproved)
            } else {
                item
            }
        }
        _currentScan.value = current.copy(items = updatedItems)

        if (database != null) {
            externalScope.launch {
                database.prescriptionDao().updateItemSupervisorNotes(itemId, doctorNotes, isApproved)
                database.pharmacistNoteDao().insertNote(
                    PharmacistNoteEntity(
                        targetType = "PRESCRIPTION_ITEM_SUPERVISOR",
                        targetId = itemId,
                        supervisorName = "د.ص أنس يحيى باقاضي",
                        noteText = doctorNotes,
                        isApproved = isApproved
                    )
                )
            }
        }
    }

    fun updateOverallSupervisorNotes(notes: String) {
        val current = _currentScan.value ?: return
        _currentScan.value = current.copy(supervisorOverallComment = notes)

        if (database != null) {
            externalScope.launch {
                database.prescriptionDao().updatePrescriptionOverallComment(current.id, notes)
                database.pharmacistNoteDao().insertNote(
                    PharmacistNoteEntity(
                        targetType = "PRESCRIPTION",
                        targetId = current.id.toString(),
                        noteText = notes,
                        isApproved = true
                    )
                )
            }
        }
    }

    fun addCompany(company: PharmaCompany) {
        if (database != null) {
            externalScope.launch {
                database.companyDao().insertCompany(company.toEntity())
            }
        } else {
            _memCompanies.update { current ->
                if (current.any { it.id == company.id }) current else current + company
            }
        }
    }

    fun addManualDrug(drug: CompanyDrug) {
        if (database != null) {
            externalScope.launch {
                database.drugDao().insertDrug(drug.toEntity())
                if (drug.supervisorNotes.isNotBlank()) {
                    database.pharmacistNoteDao().insertNote(
                        PharmacistNoteEntity(
                            targetType = "DRUG",
                            targetId = drug.id,
                            noteText = drug.supervisorNotes,
                            isApproved = true
                        )
                    )
                }
            }
        } else {
            _memDrugs.update { listOf(drug) + it }
        }
    }

    fun addArchiveItem(item: CompanyArchiveItem) {
        if (database != null) {
            externalScope.launch {
                database.archiveDao().insertArchiveItem(item.toEntity())
            }
        } else {
            _memArchiveItems.update { listOf(item) + it }
        }
    }

    fun addSupervisorKnowledge(
        drug: CompanyDrug?,
        archiveItem: CompanyArchiveItem?,
        newCompany: PharmaCompany? = null
    ) {
        if (newCompany != null) {
            addCompany(newCompany)
        }
        if (drug != null) {
            addManualDrug(drug)
        }
        if (archiveItem != null) {
            addArchiveItem(archiveItem)
        }
    }

    fun getDrugsByCompany(companyId: String): List<CompanyDrug> {
        return drugs.value.filter { it.companyId.equals(companyId, ignoreCase = true) }
    }

    fun getArchiveByCompany(companyId: String): List<CompanyArchiveItem> {
        return archiveItems.value.filter { it.companyId.equals(companyId, ignoreCase = true) }
    }

    // --- Mapping Functions ---

    private fun CompanyEntity.toDomain() = PharmaCompany(
        id = id,
        nameAr = nameAr,
        nameEn = nameEn,
        country = country,
        agentName = agentName,
        agentPhone = agentPhone,
        website = website,
        badgeColor = badgeColor,
        description = description
    )

    private fun PharmaCompany.toEntity() = CompanyEntity(
        id = id,
        nameAr = nameAr,
        nameEn = nameEn,
        country = country,
        agentName = agentName,
        agentPhone = agentPhone,
        website = website,
        badgeColor = badgeColor,
        description = description
    )

    private fun DrugEntity.toDomain() = CompanyDrug(
        id = id,
        companyId = companyId,
        tradeName = tradeName,
        genericName = genericName,
        strength = strength,
        form = form,
        agent = agent,
        standardDosage = standardDosage,
        commonInteractions = commonInteractions,
        storageAndTips = storageAndTips,
        priceEstimate = priceEstimate,
        leafletSummary = leafletSummary,
        supervisorNotes = supervisorNotes,
        isSupervisorPriority = isSupervisorPriority,
        sourceOrigin = sourceOrigin
    )

    private fun CompanyDrug.toEntity() = DrugEntity(
        id = id,
        companyId = companyId,
        tradeName = tradeName,
        genericName = genericName,
        strength = strength,
        form = form,
        agent = agent,
        standardDosage = standardDosage,
        commonInteractions = commonInteractions,
        storageAndTips = storageAndTips,
        priceEstimate = priceEstimate,
        leafletSummary = leafletSummary,
        supervisorNotes = supervisorNotes,
        isSupervisorPriority = isSupervisorPriority,
        sourceOrigin = sourceOrigin
    )

    private fun ArchiveItemEntity.toDomain() = CompanyArchiveItem(
        id = id,
        companyId = companyId,
        title = title,
        fileType = try { ArchiveType.valueOf(fileType) } catch (e: Exception) { ArchiveType.LEAFLET },
        summary = summary,
        dateAdded = dateAdded,
        sourceUrlOrPath = sourceUrlOrPath,
        isVerifiedBySupervisor = isVerifiedBySupervisor,
        isSupervisorPriority = isSupervisorPriority,
        sourceOrigin = sourceOrigin
    )

    private fun CompanyArchiveItem.toEntity() = ArchiveItemEntity(
        id = id,
        companyId = companyId,
        title = title,
        fileType = fileType.name,
        summary = summary,
        dateAdded = dateAdded,
        sourceUrlOrPath = sourceUrlOrPath,
        isVerifiedBySupervisor = isVerifiedBySupervisor,
        isSupervisorPriority = isSupervisorPriority,
        sourceOrigin = sourceOrigin
    )

    private fun PrescriptionWithItems.toDomain() = PrescriptionScan(
        id = prescription.id,
        title = prescription.title,
        clinicOrDoctor = prescription.clinicOrDoctor,
        date = prescription.date,
        isHandwritten = prescription.isHandwritten,
        confidence = prescription.confidence,
        imagePath = prescription.imagePath,
        supervisorOverallComment = prescription.supervisorOverallComment,
        items = items.map { it.toDomain() }
    )

    private fun PrescriptionItemEntity.toDomain() = PrescriptionItem(
        id = id,
        tradeName = tradeName,
        genericName = genericName,
        dosageForm = dosageForm,
        strength = strength,
        manufacturer = manufacturer,
        localAgent = localAgent,
        writtenDosage = writtenDosage,
        drugInteractions = drugInteractions,
        medicalTips = medicalTips,
        clinicalNotes = clinicalNotes.ifBlank { supervisorNotes },
        supervisorDoctorNotes = supervisorDoctorNotes,
        supervisorNotes = supervisorDoctorNotes.ifBlank { clinicalNotes.ifBlank { supervisorNotes } },
        isApprovedBySupervisor = isApprovedBySupervisor
    )

    private fun PrescriptionItem.toEntity(prescriptionId: Long) = PrescriptionItemEntity(
        id = id,
        prescriptionId = prescriptionId,
        tradeName = tradeName,
        genericName = genericName,
        dosageForm = dosageForm,
        strength = strength,
        manufacturer = manufacturer,
        localAgent = localAgent,
        writtenDosage = writtenDosage,
        drugInteractions = drugInteractions,
        medicalTips = medicalTips,
        clinicalNotes = clinicalNotes,
        supervisorDoctorNotes = supervisorDoctorNotes,
        supervisorNotes = supervisorDoctorNotes.ifBlank { clinicalNotes },
        isApprovedBySupervisor = isApprovedBySupervisor
    )

    fun getPresetPrescription(index: Int): PrescriptionScan {
        return when (index) {
            0 -> PrescriptionScan(
                id = 101L,
                title = "روشتة باطنية وصدرية (خط يدوي مائل)",
                clinicOrDoctor = "عيادة د. أسامة المنصوري - استشاري أمراض الصدر والباطنية",
                date = "2026-11-08",
                isHandwritten = true,
                confidence = "98.7%",
                items = listOf(
                    PrescriptionItem(
                        id = "rx1_item1",
                        tradeName = "أوجمنتين (Augmentin)",
                        genericName = "Amoxicillin + Clavulanate",
                        dosageForm = "أقراص مغلفة",
                        strength = "1 g",
                        manufacturer = "GSK / جلاكسو سميث كلاين",
                        localAgent = "وكالة النصر الطبية والتوزيع",
                        writtenDosage = "قرص واحد كل 12 ساعة بعد الأكل مباشرة لمدة 7 أيام.",
                        drugInteractions = listOf(
                            "يقلل من تأثير موانع الحمل الفموية.",
                            "تجنب الاستخدام المتزامن مع ألوبيورينول لتجنب الطفح الجلدي."
                        ),
                        medicalTips = listOf(
                            "يجب إكمال الكورس العلاجي بالكامل حتى بعد الشعور بالتحسن لمنع نشوء سلالات بكتيرية مقاومة.",
                            "يؤخذ مع بداية وجبة الطعام لتقليل الشعور بالغثيان أو اضطراب المعدة."
                        ),
                        supervisorNotes = "مراجعة د.ص أنس باقاضي: الجرعة صحيحة للبالغين، في حال ظهور إسهال شديد يرجى تناول بروبيوتيك (خمائر نافعة) بفارق ساعتين.",
                        isApprovedBySupervisor = true
                    ),
                    PrescriptionItem(
                        id = "rx1_item2",
                        tradeName = "كونكور (Concor)",
                        genericName = "Bisoprolol Fumarate (بيسوبرولول)",
                        dosageForm = "أقراص مغلفة مجزأة",
                        strength = "5 mg",
                        manufacturer = "Merck / ميرك الألمانية",
                        localAgent = "وكالة ابن سينا الدوائية",
                        writtenDosage = "نصف قرص إلى قرص واحد صباحاً يومياً بعد الإفطار بانتظام.",
                        drugInteractions = listOf(
                            "يحذر التوقف المفاجئ عنه لأنه قد يسبب ارتداداً حاداً في ضغط الدم وسرعة ضربات القلب.",
                            "قد يخفي علامات هبوط السكر (مثل تسارع النبض) لدى مرضى السكري."
                        ),
                        medicalTips = listOf(
                            "قس ضغط الدم ونبض القلب بصورة دورية وسجلها في كراسة المتابعة.",
                            "تناوله يومياً في نفس الموعد الصباحي."
                        ),
                        supervisorNotes = "مراجعة د.ص أنس باقاضي: تم التحقق من سلامة المريض من الربو الشعبي الحاد حيث أن حاصرات بيتا تستدعي الحذر لديهم.",
                        isApprovedBySupervisor = true
                    ),
                    PrescriptionItem(
                        id = "rx1_item3",
                        tradeName = "نيكسيوم (Nexium)",
                        genericName = "Esomeprazole (إيزوميبرازول)",
                        dosageForm = "كبسولات مؤجلة التحرر",
                        strength = "40 mg",
                        manufacturer = "AstraZeneca / أسترازينيكا",
                        localAgent = "الشركة المتحدة للخدمات الصيدلانية",
                        writtenDosage = "كبسولة واحدة صباحاً على الريق قبل وجبة الإفطار بنصف ساعة.",
                        drugInteractions = listOf(
                            "يقلل امتصاص فيتامين B12 والحديد والمغنيسيوم عند الاستخدام لفترات طويلة.",
                            "خيار ممتاز وأكثر أماناً مع أدوية القلب مقارنة بالأوميبرازول."
                        ),
                        medicalTips = listOf(
                            "ابتلع الكبسولة كاملة مع كوب ماء ولا تمضغ أو تسحق الحبيبات الداخلية.",
                            "تجنب الأطعمة الحارة والمشروبات الغازية قبل النوم."
                        ),
                        supervisorNotes = "مراجعة د.ص أنس باقاضي: حماية ممتازة للمعدة، معتمد من قبلنا.",
                        isApprovedBySupervisor = true
                    )
                ),
                supervisorOverallComment = "روشتة علاجية متوازنة تم تدقيقها بالكامل بواسطة د.ص أنس يحيى باقاضي. تم التأكد من عدم وجود تعارض خطير بين المضاد الحيوي وعقار القلب."
            )
            1 -> PrescriptionScan(
                id = 102L,
                title = "روشتة عظام وروماتيزم (تداخل دوائي محتمل)",
                clinicOrDoctor = "المركز التخصصي لجراحة العظام والمفاصل",
                date = "2026-11-07",
                isHandwritten = true,
                confidence = "97.9%",
                items = listOf(
                    PrescriptionItem(
                        id = "rx2_item1",
                        tradeName = "سيليبريكس (Celebrex)",
                        genericName = "Celecoxib (سيليكوكسيب)",
                        dosageForm = "كبسولات",
                        strength = "200 mg",
                        manufacturer = "Pfizer / فايزر",
                        localAgent = "الشركة العامة للتوكيلات الدوائية",
                        writtenDosage = "كبسولة واحدة يومياً بعد وجبة الغداء.",
                        drugInteractions = listOf(
                            "تحذير هام: الوصفة تحتوي أيضاً على مسكن آخر (فولتارين). الجمع بين مسكنين من عائلة NSAIDs يضاعف خطر قرحة المعدة والنزيف ولا يضيف فائدة تسكين إضافية!"
                        ),
                        medicalTips = listOf(
                            "يؤخذ مع وجبة طعام كاملة وكوب ماء كبير.",
                            "اخبر طبيبك إذا كنت تعاني من حساسية أدوية السلفا."
                        ),
                        supervisorNotes = "تدخل المشرف د.ص أنس باقاضي: قمت بفصل المسكنين والتوصية باستخدام Celebrex فقط وإلغاء الفولتارين لحماية الكلى والمعدة.",
                        isApprovedBySupervisor = true
                    ),
                    PrescriptionItem(
                        id = "rx2_item2",
                        tradeName = "ماسكادول (Muscadol)",
                        genericName = "Paracetamol + Orphenadrine Citrate",
                        dosageForm = "أقراص باسطة للعضلات ومسكنة",
                        strength = "450/35 mg",
                        manufacturer = "SPIMACO / سبيماكو الدوائية",
                        localAgent = "شركة الخليج والجزيرة الصيدلانية",
                        writtenDosage = "قرص واحد مرتين إلى 3 مرات يومياً بعد الطعام.",
                        drugInteractions = listOf(
                            "قد يسبب النعاس وجفاف الفم، تجنب قيادة السيارة أثناء تناوله."
                        ),
                        medicalTips = listOf(
                            "اشرب سوائل كافية لمكافحة جفاف الحلق.",
                            "تجنب تناول أي مسكنات أخرى تحوي الباراسيتامول لتفادي الجرعة الزائدة."
                        ),
                        supervisorNotes = "ملاحظة د.ص أنس باقاضي: مناسب لفك التشنج العضلي المصاحب للفقرات وآلام الظهر.",
                        isApprovedBySupervisor = true
                    ),
                    PrescriptionItem(
                        id = "rx2_item3",
                        tradeName = "بانتوزول (Pantozol)",
                        genericName = "Pantoprazole (بانتوبرازول)",
                        dosageForm = "أقراص مقاومة لعصارة المعدة",
                        strength = "40 mg",
                        manufacturer = "Takeda / تاكيدا",
                        localAgent = "وكالة الشرق الأوسط الطبية",
                        writtenDosage = "قرص واحد قبل الإفطار يومياً كحماية معدية.",
                        drugInteractions = listOf(
                            "آمن جداً مع المسكنات ولا يتعارض مع الصفائح الدموية."
                        ),
                        medicalTips = listOf(
                            "يؤخذ بانتظام طوال فترة استخدام المسكنات العظمية."
                        ),
                        supervisorNotes = "ملاحظة د.ص أنس باقاضي: واقٍ معدي أساسي وضروري مع مسكنات العظام، معتمد.",
                        isApprovedBySupervisor = true
                    )
                ),
                supervisorOverallComment = "تنبيه د.ص أنس يحيى باقاضي: اكتشف محرك الذكاء الاصطناعي ازدواجية مسكنات في الوصفة الأصلية وتم تصحيحها صيدلانياً حفاظاً على سلامة المريض."
            )
            else -> PrescriptionScan(
                id = 103L,
                title = "روشتة سكري وغدد صماء وضغط",
                clinicOrDoctor = "عيادة السكري والغدد الصماء التخصصية",
                date = "2026-11-06",
                isHandwritten = false,
                confidence = "99.4%",
                items = listOf(
                    PrescriptionItem(
                        id = "rx3_item1",
                        tradeName = "جلوكوفاج إكس آر (Glucophage XR)",
                        genericName = "Metformin HCl Extended Release",
                        dosageForm = "أقراص ممتدة المفعول",
                        strength = "1000 mg",
                        manufacturer = "Merck / ميرك",
                        localAgent = "وكالة ابن سينا الدوائية",
                        writtenDosage = "قرص واحد بعد وجبة العشاء يومياً مع كوب ماء كبير.",
                        drugInteractions = listOf(
                            "قد يقلل من امتصاص فيتامين B12 مع السنوات، ينصح بفحص دوري لمستوى الفيتامين."
                        ),
                        medicalTips = listOf(
                            "تقنية XR الممتدة المفعول تمنع المغص والانتفاخ، لا تقسم أو تمضغ القرص أبداً.",
                            "المشي لمدة 20 دقيقة بعد الوجبة يرفع استجابة الخلايا للدواء بنسبة ملحوظة."
                        ),
                        supervisorNotes = "ملاحظة د.ص أنس باقاضي: فحص وظائف الكلى (eGFR) يجب أن يكون أعلى من 45 مل/دقيقة، الوصفة آمنة وممتازة.",
                        isApprovedBySupervisor = true
                    ),
                    PrescriptionItem(
                        id = "rx3_item2",
                        tradeName = "جانوفيا (Januvia)",
                        genericName = "Sitagliptin (سيتاجليبتين)",
                        dosageForm = "أقراص مغلفة",
                        strength = "100 mg",
                        manufacturer = "MSD / ميرك شارب آند دوم",
                        localAgent = "الشركة الدولية للتوكيلات الدوائية",
                        writtenDosage = "قرص واحد يومياً في أي وقت مع أو بدون طعام.",
                        drugInteractions = listOf(
                            "آمن جداً ولا يسبب نوبات هبوط سكر عند استخدامه بمفرده أو مع الجلوكوفاج."
                        ),
                        medicalTips = listOf(
                            "يساعد البنكرياس على إفراز الأنسولين فقط عند ارتفاع مستوى السكر في الدم."
                        ),
                        supervisorNotes = "ملاحظة د.ص أنس باقاضي: خيار حديث ومثالي للحفاظ على أمان القلب والوزن، معتمد.",
                        isApprovedBySupervisor = true
                    )
                ),
                supervisorOverallComment = "وصفة نموذجية معتمدة لعلاج السكري النوع الثاني تحت إشراف وتدقيق د.ص أنس يحيى باقاضي."
            )
        }
    }

    private fun getInitialCompanies() = listOf(
        PharmaCompany(
            id = "sanofi",
            nameAr = "سانوفي العالمية (Sanofi)",
            nameEn = "Sanofi Aventis",
            country = "فرنسا",
            agentName = "شركة الشرق للتوزيع والتوكيلات الطبية",
            agentPhone = "+967 777 123 456",
            website = "https://www.sanofi.com",
            badgeColor = 0xFF8E2DE2,
            description = "إحدى كبرى شركات الأدوية العالمية المتخصصة في أدوية القلب والأوعية الدموية والسكري والمضادات الحيوية."
        ),
        PharmaCompany(
            id = "novartis",
            nameAr = "نوفارتس (Novartis)",
            nameEn = "Novartis AG",
            country = "سويسرا",
            agentName = "وكالة النور والمستقبل الدوائية",
            agentPhone = "+967 777 234 567",
            website = "https://www.novartis.com",
            badgeColor = 0xFF2563EB,
            description = "شركة رائدة في أدوية العيون، أمراض القلب، مضادات الالتهاب، وأدوية الأورام المتطورة."
        ),
        PharmaCompany(
            id = "spimaco",
            nameAr = "سبيماكو الدوائية (SPIMACO)",
            nameEn = "Saudi Pharmaceutical Industries",
            country = "المملكة العربية السعودية",
            agentName = "شركة الخليج والجزيرة الصيدلانية",
            agentPhone = "+966 11 200 8899",
            website = "https://www.spimaco.com.sa",
            badgeColor = 0xFF10B981,
            description = "رائدة الصناعات الدوائية في الشرق الأوسط، تمتاز بجودة التصنيع وتغطية كافة المجموعات العلاجية الأساسية."
        ),
        PharmaCompany(
            id = "shaphaco",
            nameAr = "الشركة العربية لصناعة الأدوية (شفا فارما)",
            nameEn = "Shaphaco Pharmaceutical Industry",
            country = "اليمن",
            agentName = "مؤسسة شفا المركزية للتسويق الدوائي",
            agentPhone = "+967 1 445 667",
            website = "https://shaphaco.com",
            badgeColor = 0xFF06B6D4,
            description = "مصنع وطني رائد للأدوية وفق معايير التصنيع الجيد GMP، يوفر بدائل ممتازة بجودة علاجية وسعر مناسب."
        ),
        PharmaCompany(
            id = "global_pharma",
            nameAr = "العالمية لصناعة الأدوية (Global Pharma)",
            nameEn = "Globalpharma",
            country = "الإمارات العربية المتحدة",
            agentName = "وكالة الرعاية المتحدة للأدوية",
            agentPhone = "+971 4 885 1122",
            website = "https://globalpharma.ae",
            badgeColor = 0xFFF59E0B,
            description = "مصنّع إقليمي معتمد لأدوية الجهاز الهضمي، المسكنات، ومضادات الحساسية."
        ),
        PharmaCompany(
            id = "hikma",
            nameAr = "حكمة فارما (Hikma Pharmaceuticals)",
            nameEn = "Hikma Pharmaceuticals",
            country = "الأردن / بريطانيا",
            agentName = "وكالة الأمل والتطوير الصيدلاني",
            agentPhone = "+962 6 580 2900",
            website = "https://www.hikma.com",
            badgeColor = 0xFF9333EA,
            description = "مجموعة دوائية عالمية متخصصة في المضادات الحيوية المحقونة، أدوية الضغط والجهاز التنفسي."
        ),
        PharmaCompany(
            id = "pfizer",
            nameAr = "فايزر العالمية (Pfizer)",
            nameEn = "Pfizer Inc",
            country = "الولايات المتحدة الأمريكية",
            agentName = "الشركة العامة للتوكيلات الدوائية",
            agentPhone = "+1 800 879 3477",
            website = "https://www.pfizer.com",
            badgeColor = 0xFF3B82F6,
            description = "عملاق الصناعات الدوائية واللقاحات وأدوية الدهون ومضادات الالتهاب غير الستيرويدية."
        ),
        PharmaCompany(
            id = "yemen_pharma",
            nameAr = "الشركة اليمنية لصناعة وتجارة الأدوية (يدكو / Yemen Pharma)",
            nameEn = "Yedco Pharma",
            country = "اليمن",
            agentName = "الإدارة العامة للتوزيع الطبي",
            agentPhone = "+967 1 234 560",
            website = "https://yedco.org",
            badgeColor = 0xFF14B8A6,
            description = "أول وأعرق شركة أدوية يمنية، تغطي احتياجات المستشفيات والمراكز الصحية الوطنية."
        )
    )

    private fun getInitialDrugs() = listOf(
        CompanyDrug(
            id = "d1",
            companyId = "sanofi",
            tradeName = "بلافيكس (Plavix)",
            genericName = "Clopidogrel (كلوبيدوجريل)",
            strength = "75 mg",
            form = "أقراص مغلفة (Film-coated Tablets)",
            agent = "شركة الشرق للتوزيع والتوكيلات الطبية",
            standardDosage = "قرص واحد يومياً مع الطعام أو بدونه، يفضل في نفس الوقت.",
            commonInteractions = "تجنب الاستخدام المتزامن مع أوميبرازول (Omeprazole) لأنه يقلل فعالية البلافيكس، استبدله بـ بانتوبرازول. حذارِ من مضادات التخثر الأخرى دون إشراف.",
            storageAndTips = "يحفظ تحت 25 درجة مئوية. انتبه لأي علامات نزيف لثوي أو كدمات مفاجئة واستشر الطبيب فوراً.",
            priceEstimate = "معتمد بالتسعيرة الرسمية",
            leafletSummary = "مضاد لتجمع الصفائح الدموية للوقاية من النوبات القلبية والسكتات الدماغية وتجلط الدعامات.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: يجب إيقاف الدواء قبل العمليات الجراحية المبرمجة بـ 5 إلى 7 أيام بالتنسيق مع الطبيب الجراح."
        ),
        CompanyDrug(
            id = "d2",
            companyId = "sanofi",
            tradeName = "أماريل (Amaryl)",
            genericName = "Glimepiride (جليمبريد)",
            strength = "2 mg / 3 mg",
            form = "أقراص قابلة للكسر",
            agent = "شركة الشرق للتوزيع والتوكيلات الطبية",
            standardDosage = "قرص واحد صباحاً قبل وجبة الإفطار مباشرة أو أول وجبة رئيسية.",
            commonInteractions = "الكحول وحاصرات بيتا قد تخفي أعراض هبوط السكر. تزداد فعاليته مع مضادات الالتهاب غير الستيرويدية.",
            storageAndTips = "احتفظ دائماً بمصدر سكر سريع (حلوى أو عصير) تحسباً لأعراض هبوط السكر (تعرق، رجفة، جوع شديد).",
            priceEstimate = "متوفر في الصيدليات",
            leafletSummary = "محفز لإفراز الأنسولين من خلايا بيتا في البنكرياس لعلاج السكري من النوع الثاني.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: ينصح بالبدء بجرعة 1mg للمسنين ومراقبة وظائف الكلى الدورية."
        ),
        CompanyDrug(
            id = "d3",
            companyId = "novartis",
            tradeName = "فولتارين (Voltaren)",
            genericName = "Diclofenac Sodium (ديكلوفيناك الصوديوم)",
            strength = "50 mg / 75 mg SR / 100 mg",
            form = "أقراص معوية / حقن عضلية",
            agent = "وكالة النور والمستقبل الدوائية",
            standardDosage = "قرص واحد بعد وجبة دسمة أو مرتين يومياً حسب شدة الألم.",
            commonInteractions = "خطر مضاعف للقرحة والنزيف المعوي عند استخدامه مع الأسبرين أو الكورتيزون. يقلل من فعالية أدوية الضغط ومدرات البول.",
            storageAndTips = "يؤخذ مع كوب ماء ممتلئ بعد الأكل مباشرة لحماية المعدة. تجنب النوم بعد تناوله لمدة 15 دقيقة.",
            priceEstimate = "سعر مدعوم",
            leafletSummary = "مسكن قوي وخافض للحرارة ومضاد للالتهاب الروماتيزمي والتنكيسي الحاد والمزمن.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: يمنع لمرضى الفشل القلبي المتقدم ومرضى القرحة الهضمية النشطة، ويفضل وصف واقٍ للمعدة معه."
        ),
        CompanyDrug(
            id = "d4",
            companyId = "novartis",
            tradeName = "جالفوس ميت (Galvus Met)",
            genericName = "Vildagliptin + Metformin (فيلداجليبتين + ميتفورمين)",
            strength = "50/1000 mg",
            form = "أقراص مغلفة",
            agent = "وكالة النور والمستقبل الدوائية",
            standardDosage = "قرص واحد مرتين يومياً مع الوجبات الرئيسية صباحاً ومساءً.",
            commonInteractions = "يجب إيقافه قبل التصوير بالصبغة الشعاعية بـ 48 ساعة لتجنب الحماض اللبني (Lactic Acidosis).",
            storageAndTips = "اشرب كميات كافية من الماء يومياً. خذه مع الأكل لتقليل الاضطرابات الهضمية.",
            priceEstimate = "متوفر رسمي",
            leafletSummary = "علاج مزدوج لتنظيم سكر الدم عبر آليتين متكاملتين دون التسبب في زيادة الوزن.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: فحص إنزيمات الكبد ALT/AST ضروري قبل البدء وكل 3 أشهر خلال السنة الأولى."
        ),
        CompanyDrug(
            id = "d5",
            companyId = "spimaco",
            tradeName = "سابوفين (Sapofen)",
            genericName = "Ibuprofen (إيبوبروفين)",
            strength = "400 mg / 600 mg",
            form = "أقراص مغلفة / شراب معلق",
            agent = "شركة الخليج والجزيرة الصيدلانية",
            standardDosage = "قرص كل 8 ساعات عند اللزوم بعد الأكل.",
            commonInteractions = "يتداخل مع مضادات التجلط وأدوية الضغط ومثبطات ACE.",
            storageAndTips = "لا يؤخذ على معدة فارغة. لا تتجاوز 1200mg يومياً دون استشارة طبية.",
            priceEstimate = "اقتصادي متاح للجميع",
            leafletSummary = "مسكن ألم وخافض حرارة للبالغين والأطفال، مضاد للالتهاب.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: بديل ممتاز وآمن عند استخدامه بجرعات منضبطة ولأقصر مدة علاجية."
        ),
        CompanyDrug(
            id = "d6",
            companyId = "shaphaco",
            tradeName = "شفاكلاف (Shaphaclav)",
            genericName = "Amoxicillin + Clavulanic Acid (أموكسيسيلين + كلافولانيك أسيد)",
            strength = "1 g / 625 mg",
            form = "أقراص مغلفة / معلق جاف للأطفال",
            agent = "مؤسسة شفا المركزية للتسويق الدوائي",
            standardDosage = "قرص واحد كل 12 ساعة في بداية الوجبة.",
            commonInteractions = "يقلل من فعالية حبوب منع الحمل المركبة. يزيد تركيز الميثوتريكسات في الدم.",
            storageAndTips = "الشراب بعد التحضير يحفظ بالثلاجة لمدة 7 أيام فقط ويرج جيداً قبل كل جرعة.",
            priceEstimate = "سعر مناسب جداً",
            leafletSummary = "مضاد حيوي واسع الطيف لعلاج التهابات الجهاز التنفسي والمسالك والجلد.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: إكمال كورس العلاج كاملاً (5 إلى 7 أيام) إلزامي لمنع حدوث المقاومة البكتيرية."
        ),
        CompanyDrug(
            id = "d7",
            companyId = "shaphaco",
            tradeName = "شَفادول إكسترا (Shaphadol Extra)",
            genericName = "Paracetamol 500mg + Caffeine 65mg",
            strength = "500/65 mg",
            form = "أقراص سريعة المفعول",
            agent = "مؤسسة شفا المركزية للتسويق الدوائي",
            standardDosage = "قرص إلى قرصين كل 6 إلى 8 ساعات عند الصداع والألم (أقصى جرعة 4000mg باراسيتامول).",
            commonInteractions = "تجنب الجمع مع أدوية نزلات البرد الأخرى التي تحتوي على باراسيتامول لتفادي التسمم الكبدي.",
            storageAndTips = "تجنب الإفراط في شرب الشاي والقهوة أثناء استخدامه لتجنب التوتر وخفقان القلب بسبب الكافيين.",
            priceEstimate = "متوفر شعبياً",
            leafletSummary = "مسكن قوي للصداع النصفي، آلام الأسنان، الصداع التوتري، وخافض للحرارة.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: آمن لمعظم المرضى وخيار مثالي لمرضى قرحة المعدة كبديل للديكلوفيناك."
        ),
        CompanyDrug(
            id = "d8",
            companyId = "pfizer",
            tradeName = "ليبيتور (Lipitor)",
            genericName = "Atorvastatin (أتورفاستاتين)",
            strength = "20 mg / 40 mg",
            form = "أقراص مغلفة",
            agent = "الشركة العامة للتوكيلات الدوائية",
            standardDosage = "قرص واحد يومياً ليلاً قبل النوم.",
            commonInteractions = "عصير الجريب فروت يزيد تركيز الدواء في الدم بشكل خطير. المضادات الحيوية مثل كلاريثروميسين تزيد خطر ألم العضلات.",
            storageAndTips = "إذا شعرت بألم عضلي غير مبرر أو بول داكن توقف عن الدواء واستشر طبيبك لإجراء فحص CPK.",
            priceEstimate = "متوفر",
            leafletSummary = "مخفض للكوليسترول الضار LDL والشحوم الثلاثية وواقي من الجلطات القلبية.",
            supervisorNotes = "ملاحظة د.ص أنس باقاضي: يجب الالتزام بحمية غذائية متوازنة وممارسة المشي لتحقيق أقصى فائدة قلبية."
        )
    )

    private fun getInitialArchive() = listOf(
        CompanyArchiveItem(
            id = "arch_1",
            companyId = "sanofi",
            title = "النشرة الدوائية الرسمية لعقار بلافيكس (Plavix Leaflet)",
            fileType = ArchiveType.LEAFLET,
            summary = "دليل الاستخدام الكامل لجرعات 75mg، مؤشرات متلازمة الشريان التاجي الحادة، وإرشادات الجراحة.",
            dateAdded = "2026-11-05",
            sourceUrlOrPath = "https://products.sanofi.com/plavix.pdf"
        ),
        CompanyArchiveItem(
            id = "arch_2",
            companyId = "novartis",
            title = "دراسة سريرية: الأمان القلبي لمركبات الفيلداجليبتين (Galvus)",
            fileType = ArchiveType.RESEARCH,
            summary = "تحليل شمولي لنتائج دراسات الأمان الوعائي لمرضى السكري والقصور الكلوي المتوسط.",
            dateAdded = "2026-11-03",
            sourceUrlOrPath = "https://novartis-clinical.org/galvus-safety.pdf"
        ),
        CompanyArchiveItem(
            id = "arch_3",
            companyId = "shaphaco",
            title = "كتالوج أدوية شركة شفا فارما ومواصفات GMP المحدثة",
            fileType = ArchiveType.PDF_FILE,
            summary = "الملف التعريفي بجميع خطوط الإنتاج: المضادات، السوائل الفموية، الكبسولات، ومراكز الوكلاء بالمحافظات.",
            dateAdded = "2026-11-01",
            sourceUrlOrPath = "https://shaphaco.com/catalog2026.pdf"
        ),
        CompanyArchiveItem(
            id = "arch_4",
            companyId = "spimaco",
            title = "رابط بوابة الوكلاء والتسجيل الدوائي - سبيماكو",
            fileType = ArchiveType.WEB_LINK,
            summary = "بوابة التحقق من الباركود والتأكد من عبوات الدواء الأصلية للوكيل المعتمد.",
            dateAdded = "2026-10-28",
            sourceUrlOrPath = "https://portal.spimaco.com.sa/verify"
        )
    )
}
