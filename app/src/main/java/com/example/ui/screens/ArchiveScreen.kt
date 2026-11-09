package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArchiveType
import com.example.data.model.CompanyArchiveItem
import com.example.data.model.CompanyDrug
import com.example.data.model.PharmaCompany
import com.example.data.model.UserRole
import com.example.data.repository.PharmaRepository
import com.example.ui.components.SupervisorSmartUploadDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PurpleElectric
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ArchiveScreen(
    repository: PharmaRepository,
    modifier: Modifier = Modifier
) {
    val companies by repository.companies.collectAsState()
    val drugs by repository.drugs.collectAsState()
    val archiveItems by repository.archiveItems.collectAsState()
    val currentUserRole by repository.currentUserRole.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCompanyId by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: أدوية الشركات, 1: ملفات ونشرات الأرشيف

    var showAddDialog by remember { mutableStateOf(false) }
    var showSmartUploadDialog by remember { mutableStateOf(false) }
    var smartUploadMode by remember { mutableStateOf("image") }

    // Filtered drugs with supervisor priority sorting
    val filteredDrugs = drugs.filter { drug ->
        val matchesCompany = selectedCompanyId == null || drug.companyId.equals(selectedCompanyId, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                drug.tradeName.contains(searchQuery, ignoreCase = true) ||
                drug.genericName.contains(searchQuery, ignoreCase = true) ||
                drug.agent.contains(searchQuery, ignoreCase = true)
        matchesCompany && matchesQuery
    }.sortedWith(
        compareByDescending<CompanyDrug> { it.isSupervisorPriority }
            .thenBy { it.tradeName }
    )

    // Filtered archive items with supervisor priority sorting
    val filteredArchive = archiveItems.filter { item ->
        val matchesCompany = selectedCompanyId == null || item.companyId.equals(selectedCompanyId, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.summary.contains(searchQuery, ignoreCase = true)
        matchesCompany && matchesQuery
    }.sortedWith(
        compareByDescending<CompanyArchiveItem> { it.isSupervisorPriority }
            .thenByDescending { it.dateAdded }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        RoyalBlueDeep,
                        Color(0xFF0F1535),
                        Color(0xFF170C30)
                    )
                )
            )
            .padding(horizontal = 14.dp)
            .testTag("archive_screen_lazy_column")
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            ArchiveHeader(
                currentUserRole = currentUserRole,
                onOpenAddDialog = { showAddDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Supervisor Smart Upload & Ingestion Card (Files, Folders, Images, Links)
        item {
            SupervisorSmartKnowledgeCard(
                currentUserRole = currentUserRole,
                onUploadClick = { mode ->
                    smartUploadMode = mode
                    showSmartUploadDialog = true
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("archive_search_field"),
                placeholder = {
                    Text(
                        text = "ابحث بالاسم التجاري، المادة الفعالة، أو الوكيل...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AccentCyan
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleNeon,
                    unfocusedBorderColor = RoyalBlueMedium,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Pharmaceutical Companies Folders Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📁 مجلدات الشركات الدوائية المعتمدة:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                if (selectedCompanyId != null) {
                    Text(
                        text = "إلغاء التحديد (عرض الكل)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { selectedCompanyId = null }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Horizontal Company Folders Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val isAllSelected = selectedCompanyId == null
                    CompanyFolderCard(
                        name = "جميع الشركات",
                        subtext = "${companies.size} شركة • ${drugs.size} دواء",
                        color = PurpleGlow,
                        isSelected = isAllSelected,
                        onClick = { selectedCompanyId = null }
                    )
                }

                items(companies) { company ->
                    val isSelected = selectedCompanyId == company.id
                    val companyDrugCount = drugs.count { it.companyId.equals(company.id, ignoreCase = true) }
                    CompanyFolderCard(
                        name = company.nameAr,
                        subtext = "${company.country} • $companyDrugCount دواء",
                        color = Color(company.badgeColor),
                        isSelected = isSelected,
                        onClick = {
                            selectedCompanyId = if (isSelected) null else company.id
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tabs: Drugs vs Files/Leaflets
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceCard,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PurpleNeon
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, RoyalBlueMedium, RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "💊 فهرس الأدوية (${filteredDrugs.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) PurpleNeon else TextSecondary
                            )
                        )
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "📑 ملفات ونشرات الشركات (${filteredArchive.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) PurpleNeon else TextSecondary
                            )
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Content: Drugs List
        if (selectedTab == 0) {
            if (filteredDrugs.isEmpty()) {
                item {
                    EmptyStateCard(message = "لا توجد أدوية مطابقة للبحث أو للشركة المحددة")
                }
            } else {
                items(filteredDrugs) { drug ->
                    CompanyDrugCard(drug = drug)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        } else {
            // Content: Archive Files & Links
            if (filteredArchive.isEmpty()) {
                item {
                    EmptyStateCard(message = "لا توجد ملفات أو نشرات مرفوعة لهذه الشركة حتى الآن")
                }
            } else {
                items(filteredArchive) { item ->
                    ArchiveDocumentCard(item = item)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Drug / File Dialog
    if (showAddDialog) {
        AddDrugOrArchiveDialog(
            companies = companies,
            onDismiss = { showAddDialog = false },
            onAddDrug = { newDrug ->
                repository.addManualDrug(newDrug)
                showAddDialog = false
            },
            onAddArchive = { newArchive ->
                repository.addArchiveItem(newArchive)
                showAddDialog = false
            }
        )
    }

    // Supervisor Smart Upload & Ingestion Dialog (AI Analysis & Priority Search)
    if (showSmartUploadDialog) {
        SupervisorSmartUploadDialog(
            companies = companies,
            initialMode = smartUploadMode,
            onDismiss = { showSmartUploadDialog = false },
            onSave = { drug, archive, newCompany ->
                repository.addSupervisorKnowledge(drug, archive, newCompany)
            }
        )
    }
}

@Composable
private fun SupervisorSmartKnowledgeCard(
    currentUserRole: UserRole,
    onUploadClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(AccentGold, PurpleElectric, AccentCyan)),
                    shape = RoundedCornerShape(18.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF28114D), Color(0xFF111738))
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentGold.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "منظومة رفع وتحليل البيانات بالذكاء الاصطناعي",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (currentUserRole == UserRole.SUPERVISOR) {
                                "صلاحيات المشرف د.ص أنس: تحليل أوتوماتيكي وأولوية في البحث"
                            } else {
                                "مستندات معتمدة ومحدثة من المشرف د.ص أنس يحيى باقاضي"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (currentUserRole == UserRole.SUPERVISOR) AccentGold else AccentCyan
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentEmerald.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "أولوية البحث ⭐",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "رفع وتصنيف الملفات والمجلدات وصور العبوات والروابط لتحليلها أوتوماتيكياً بالذكاء الاصطناعي وترتيبها في ملفات الشركات وتوسيع قاعدة البيانات:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    fontSize = 11.5.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Quick Action Upload Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Image
                Button(
                    onClick = { onUploadClick("image") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleGlow),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("📷 صورة عبوة", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // File
                Button(
                    onClick = { onUploadClick("file") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("📄 ملف PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Folder
                Button(
                    onClick = { onUploadClick("folder") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("📁 مجلد شركة", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Link
                Button(
                    onClick = { onUploadClick("link") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("🔗 رابط ويب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveHeader(
    currentUserRole: UserRole,
    onOpenAddDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF1E1038),
                        Color(0xFF0F1E4A)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(PurpleElectric, AccentCyan)),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "الأرشيف الذكي والمكتبة الدوائية",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = if (currentUserRole == UserRole.SUPERVISOR) {
                    "أرشفة وتدقيق واعتماد المشرف د.ص أنس باقاضي"
                } else {
                    "حساب الزملاء: للاطلاع والتحميل والاستفادة فقط"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (currentUserRole == UserRole.SUPERVISOR) AccentGold else AccentCyan
                )
            )
        }

        Button(
            onClick = onOpenAddDialog,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleGlow),
            modifier = Modifier.testTag("add_to_archive_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "إضافة",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun CompanyFolderCard(
    name: String,
    subtext: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(listOf(color.copy(alpha = 0.45f), RoyalBlueMedium))
                } else {
                    Brush.linearGradient(listOf(SurfaceCard, Color(0xFF131A38)))
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else RoyalBlueMedium,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AccentEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompanyDrugCard(drug: CompanyDrug) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RoyalBlueMedium, RoundedCornerShape(16.dp))
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.tradeName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PurpleNeon
                        )
                    )
                    Text(
                        text = "${drug.genericName} • ${drug.strength}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AccentCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoyalBlueMedium)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = drug.form,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "🏢 الوكيل: ${drug.agent}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    // Standard Dosage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C243B))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⏱️ الجرعة والاستخدام: ${drug.standardDosage}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Interactions & Warnings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3B1212))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠️ التداخلات والتحذيرات: ${drug.commonInteractions}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFCA5A5))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Leaflet snippet
                    if (drug.leafletSummary.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F3229))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "📑 ملخص النشرة: ${drug.leafletSummary}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA7F3D0))
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Dr. Anas Supervisor Note
                    if (drug.supervisorNotes.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF28114A))
                                .border(1.dp, AccentGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = drug.supervisorNotes,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccentGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ArchiveDocumentCard(item: CompanyArchiveItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RoyalBlueMedium, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PurpleGlow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.fileType) {
                        ArchiveType.LEAFLET -> Icons.Default.Description
                        ArchiveType.WEB_LINK -> Icons.Default.Link
                        ArchiveType.RESEARCH -> Icons.Default.MedicalServices
                        else -> Icons.Default.UploadFile
                    },
                    contentDescription = null,
                    tint = PurpleNeon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "${item.fileType.labelAr} • ${item.dateAdded}",
                    style = MaterialTheme.typography.labelSmall.copy(color = AccentCyan)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, RoyalBlueMedium, RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
    }
}

@Composable
private fun AddDrugOrArchiveDialog(
    companies: List<PharmaCompany>,
    onDismiss: () -> Unit,
    onAddDrug: (CompanyDrug) -> Unit,
    onAddArchive: (CompanyArchiveItem) -> Unit
) {
    var isDrugMode by remember { mutableStateOf(true) }
    var selectedCompany by remember { mutableStateOf(companies.firstOrNull()?.id ?: "sanofi") }

    var tradeName by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("أقراص") }
    var agent by remember { mutableStateOf("الوكيل المعتمد") }
    var dosage by remember { mutableStateOf("") }
    var interactions by remember { mutableStateOf("") }
    var supervisorNotes by remember { mutableStateOf("ملاحظة د.ص أنس باقاضي: تم التحقق والإضافة يدوياً للأرشيف الدوائي.") }

    var docTitle by remember { mutableStateOf("") }
    var docSummary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isDrugMode) "إضافة دواء جديد للأرشيف" else "رفع ملف / نشرة / رابط للشركة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isDrugMode,
                        onClick = { isDrugMode = true },
                        label = { Text("💊 دواء جديد") }
                    )
                    FilterChip(
                        selected = !isDrugMode,
                        onClick = { isDrugMode = false },
                        label = { Text("📑 ملف / نشرة") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isDrugMode) {
                    OutlinedTextField(
                        value = tradeName,
                        onValueChange = { tradeName = it },
                        label = { Text("الاسم التجاري") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = genericName,
                        onValueChange = { genericName = it },
                        label = { Text("المادة الفعالة (الاسم العلمي)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = strength,
                            onValueChange = { strength = it },
                            label = { Text("التركيز (500mg)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = form,
                            onValueChange = { form = it },
                            label = { Text("الشكل (أقراص/شراب)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("الجرعة والاستخدام") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = supervisorNotes,
                        onValueChange = { supervisorNotes = it },
                        label = { Text("ملاحظات د.ص أنس باقاضي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("عنوان الملف أو النشرة أو الرابط") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = docSummary,
                        onValueChange = { docSummary = it },
                        label = { Text("ملخص المحتوى أو رابط الويب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isDrugMode) {
                        if (tradeName.isNotBlank()) {
                            onAddDrug(
                                CompanyDrug(
                                    id = "manual_${System.currentTimeMillis()}",
                                    companyId = selectedCompany,
                                    tradeName = tradeName,
                                    genericName = genericName,
                                    strength = strength,
                                    form = form,
                                    agent = agent,
                                    standardDosage = dosage,
                                    commonInteractions = interactions,
                                    storageAndTips = "يحفظ في درجة حرارة الغرفة.",
                                    supervisorNotes = supervisorNotes
                                )
                            )
                        }
                    } else {
                        if (docTitle.isNotBlank()) {
                            onAddArchive(
                                CompanyArchiveItem(
                                    id = "arch_${System.currentTimeMillis()}",
                                    companyId = selectedCompany,
                                    title = docTitle,
                                    fileType = ArchiveType.LEAFLET,
                                    summary = docSummary,
                                    dateAdded = "2026-11-08"
                                )
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
            ) {
                Text(
                    text = "حفظ وإضافة للأرشيف",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        containerColor = Color(0xFF161B3D)
    )
}
