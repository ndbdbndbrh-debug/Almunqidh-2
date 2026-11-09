package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiPrescriptionAnalyzer
import com.example.data.model.PrescriptionItem
import com.example.data.model.PrescriptionScan
import com.example.data.model.UserRole
import com.example.data.repository.PharmaRepository
import com.example.ui.components.BrainState
import com.example.ui.components.DrugResultCard
import com.example.ui.components.NeuralBrainVisualizer
import com.example.ui.components.SubscriptionPaymentDialog
import com.example.ui.components.UserRoleSwitchDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PurpleElectric
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.ShareHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(
    repository: PharmaRepository,
    analyzer: GeminiPrescriptionAnalyzer,
    modifier: Modifier = Modifier,
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentScan by repository.currentScan.collectAsState()
    val currentUserRole by repository.currentUserRole.collectAsState()

    var brainState by remember { mutableStateOf(BrainState.SUCCESS) }
    var selectedPresetIndex by remember { mutableStateOf(0) }

    var showUserRoleDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

    // Activity Result Launchers for Camera and Gallery
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            coroutineScope.launch {
                brainState = BrainState.UPLOADING
                delay(700)
                brainState = BrainState.ANALYZING
                val analyzedScan = analyzer.analyzePrescription(bitmap, isHandwrittenPreference = true)
                repository.setCurrentScan(analyzedScan)
                brainState = BrainState.SUCCESS
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                brainState = BrainState.UPLOADING
                val bitmap = try {
                    if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                } catch (e: Exception) {
                    null
                }
                delay(600)
                brainState = BrainState.ANALYZING
                val analyzedScan = analyzer.analyzePrescription(bitmap, isHandwrittenPreference = true)
                repository.setCurrentScan(analyzedScan)
                brainState = BrainState.SUCCESS
            }
        }
    }

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
            .testTag("scanner_screen_lazy_column")
    ) {
        // Top Header with Role Switcher & Subscription Plan Button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            TopAppHeader(
                currentRole = currentUserRole,
                onOpenRoleSwitch = { showUserRoleDialog = true },
                onOpenSubscription = { onNavigateToSubscription() }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Colleague Mode Warning Banner
        if (currentUserRole == UserRole.COLLEAGUE) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF162547))
                        .border(1.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حساب الزملاء: صلاحيات التحميل والقراءة والاطلاع فقط • لا يُحفظ شيء دون اعتماد المشرف الصيدلاني.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Neural Brain Visualizer
        item {
            NeuralBrainVisualizer(
                state = brainState,
                confidence = currentScan?.confidence ?: "98.5%",
                onTap = {
                    coroutineScope.launch {
                        brainState = BrainState.ANALYZING
                        delay(1200)
                        brainState = BrainState.SUCCESS
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Action Buttons Row: Camera, Gallery & Presets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Camera Button
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("camera_scan_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleGlow
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تصوير الروشتة",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Gallery Button
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("gallery_upload_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlueBright
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "رفع صورة",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Prescriptions Chips
            Text(
                text = "⚡ نماذج وصفات جاهزة للتجربة والفحص الفوري:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(3) { index ->
                    val title = when (index) {
                        0 -> "روشتة باطنية وصدرية (يدوية)"
                        1 -> "روشتة عظام (تداخل دوائي)"
                        else -> "روشتة سكري وضغط"
                    }
                    val isSelected = selectedPresetIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPresetIndex = index
                            coroutineScope.launch {
                                brainState = BrainState.ANALYZING
                                delay(900)
                                val preset = repository.getPresetPrescription(index)
                                repository.setCurrentScan(preset)
                                brainState = BrainState.SUCCESS
                            }
                        },
                        label = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleGlow.copy(alpha = 0.35f),
                            selectedLabelColor = PurpleNeon,
                            containerColor = RoyalBlueMedium.copy(alpha = 0.4f),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PurpleElectric else RoyalBlueMedium
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Prescription Metadata Card
        currentScan?.let { scan ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                Brush.horizontalGradient(listOf(PurpleGlow.copy(alpha = 0.5f), AccentCyan.copy(alpha = 0.5f))),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = scan.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (scan.isHandwritten) "خط يدوي طبيب" else "وصفة مطبوعة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "🏥 ${scan.clinicOrDoctor} • 📅 ${scan.date}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dr. Anas Overall Supervisor Note
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF28104E))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scan.supervisorOverallComment,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFDE68A),
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section Title for Results
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 الأدوية المستخرجة والتحليل الدوائي (${scan.items.size}):",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    // Share button
                    Button(
                        onClick = { ShareHelper.sharePrescription(context, scan) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("share_prescription_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مشاركة للجميع",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // List of Drug Result Cards
            itemsIndexed(scan.items) { index, item ->
                DrugResultCard(
                    item = item,
                    index = index,
                    currentUserRole = currentUserRole,
                    onUpdateSupervisorDoctorNotes = { newDoctorNotes, isApproved ->
                        repository.updateSupervisorDoctorNotes(item.id, newDoctorNotes, isApproved)
                    },
                    onDeleteDoctorNotes = {
                        repository.updateSupervisorDoctorNotes(item.id, "", false)
                    }
                )
            }
        }

        // Bottom Social Sharing Banner
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SocialDistributionBanner(
                onShareApp = { ShareHelper.shareAppInvitation(context) }
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Role Switch Dialog
    if (showUserRoleDialog) {
        UserRoleSwitchDialog(
            currentRole = currentUserRole,
            onRoleSelected = { newRole ->
                repository.setUserRole(newRole)
            },
            onDismiss = { showUserRoleDialog = false }
        )
    }

    // Subscription & Remittance Dialog
    if (showSubscriptionDialog) {
        SubscriptionPaymentDialog(
            subscriptionDetails = repository.subscriptionDetails,
            onDismiss = { showSubscriptionDialog = false }
        )
    }
}

@Composable
private fun TopAppHeader(
    currentRole: UserRole,
    onOpenRoleSwitch: () -> Unit,
    onOpenSubscription: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2C1052),
                        Color(0xFF131B4D)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(PurpleElectric, RoyalBlueBright)),
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = PurpleNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "المُنقذ الدوائي",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Text(
                    text = "تحت إشراف: د.ص أنس يحيى باقاضي",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGold
                    )
                )
            }

            // Subscription Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F3E2B))
                    .border(1.dp, AccentEmerald, RoundedCornerShape(10.dp))
                    .clickable { onOpenSubscription() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "شهر مجاناً • 25 و 50 ريال",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Role Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF181C3F))
                .border(1.dp, RoyalBlueMedium, RoundedCornerShape(10.dp))
                .clickable { onOpenRoleSwitch() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (currentRole == UserRole.SUPERVISOR) Icons.Default.VerifiedUser else Icons.Default.Group,
                    contentDescription = null,
                    tint = if (currentRole == UserRole.SUPERVISOR) AccentGold else AccentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentRole.titleAr,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (currentRole == UserRole.SUPERVISOR) AccentGold.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentRole.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (currentRole == UserRole.SUPERVISOR) AccentGold else AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Text(
                text = "تبديل ▾",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PurpleNeon,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun SocialDistributionBanner(onShareApp: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18153A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(AccentGold.copy(alpha = 0.7f), PurpleElectric)),
                    RoundedCornerShape(18.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌐 تجربة مجانية شهر كامل لجميع الصيادلة والزملاء",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "يمكنك تحميل وتثبيت التطبيق ومشاركته عبر واتساب وفيسبوك لخدمة المرضى والصيادلة. اشتراك رمزي (25 ريال سعودي لمدة 4 أشهر أو 50 ريال سعودي للاشتراك السنوي) عبر شبكات الصرافة اليمنية بعد الشهر المجاني.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 18.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onShareApp,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleGlow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مشاركة التطبيق عبر واتساب وفيسبوك الآن",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
