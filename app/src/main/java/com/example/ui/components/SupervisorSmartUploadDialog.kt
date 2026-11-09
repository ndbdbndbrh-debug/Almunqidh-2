package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.SupervisorExtractionResult
import com.example.data.ai.SupervisorKnowledgeAnalyzer
import com.example.data.model.CompanyArchiveItem
import com.example.data.model.CompanyDrug
import com.example.data.model.PharmaCompany
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
import kotlinx.coroutines.launch

@Composable
fun SupervisorSmartUploadDialog(
    companies: List<PharmaCompany>,
    initialMode: String = "image", // "image", "file", "folder", "link"
    onDismiss: () -> Unit,
    onSave: (drug: CompanyDrug?, archive: CompanyArchiveItem?, newCompany: PharmaCompany?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val analyzer = remember { SupervisorKnowledgeAnalyzer() }

    var currentMode by remember { mutableStateOf(initialMode) }

    // State for Image OCR
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageHintText by remember { mutableStateOf("") }

    // State for File / PDF
    var fileName by remember { mutableStateOf("دليل_أدوية_سبأفارما_2026.pdf") }
    var fileContent by remember { mutableStateOf("نشرة طبية شاملة لمستحضرات المضادات الحيوية وأدوية الجهاز الهضمي والقلب التابعة لشركة سبأفارما والوكيل المعتمد بصنعاء.") }

    // State for Folder / Catalog
    var folderName by remember { mutableStateOf("شركة يدكو الدوائية (YEDCO)") }
    var folderAgent by remember { mutableStateOf("الشركة اليمنية لصناعة وتجارة الأدوية - يدكو") }
    var folderDesc by remember { mutableStateOf("دليل الأدوية الوطنية والمحاليل الوريدية والمضادات المعتمدة من وزارة الصحة.") }

    // State for Web Link
    var webLinkUrl by remember { mutableStateOf("https://shaphaco.com/catalog/shaphadol-extra") }
    var linkDescription by remember { mutableStateOf("النشرة المعتمدة لمسكنات وأدوية شركة شيفاكو فارما") }

    // Analysis State
    var isAnalyzing by remember { mutableStateOf(false) }
    var extractedResult by remember { mutableStateOf<SupervisorExtractionResult?>(null) }

    // Activity Result Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            extractedResult = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                selectedBitmap = bitmap
                extractedResult = null
            } catch (e: Exception) {
                Toast.makeText(context, "فشل فتح الصورة المحددة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AccentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "الرفع والتحليل والأرشفة الذكية",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "للمشرف الصيدلاني د.ص أنس يحيى باقاضي",
                            style = MaterialTheme.typography.labelSmall.copy(color = AccentGold)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supervisor_smart_upload_column")
            ) {
                // Source Selector Chips
                item {
                    Text(
                        text = "اختر مصدر المحتوى لتحليله وتنسيقه أوتوماتيكياً:",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentCyan)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = currentMode == "image",
                                onClick = {
                                    currentMode = "image"
                                    extractedResult = null
                                },
                                label = { Text("📷 فحص صورة", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PurpleGlow)
                            )
                        }
                        item {
                            FilterChip(
                                selected = currentMode == "file",
                                onClick = {
                                    currentMode = "file"
                                    extractedResult = null
                                },
                                label = { Text("📄 ملف / PDF", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalBlueBright)
                            )
                        }
                        item {
                            FilterChip(
                                selected = currentMode == "folder",
                                onClick = {
                                    currentMode = "folder"
                                    extractedResult = null
                                },
                                label = { Text("📁 مجلد شركة", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentEmerald)
                            )
                        }
                        item {
                            FilterChip(
                                selected = currentMode == "link",
                                onClick = {
                                    currentMode = "link"
                                    extractedResult = null
                                },
                                label = { Text("🔗 رابط ويب", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Inputs based on selected mode
                item {
                    when (currentMode) {
                        "image" -> {
                            ImageInputSection(
                                selectedBitmap = selectedBitmap,
                                imageHintText = imageHintText,
                                onHintChange = { imageHintText = it },
                                onCameraClick = { cameraLauncher.launch(null) },
                                onGalleryClick = { galleryLauncher.launch("image/*") }
                            )
                        }
                        "file" -> {
                            FileInputSection(
                                fileName = fileName,
                                fileContent = fileContent,
                                onFileNameChange = { fileName = it },
                                onFileContentChange = { fileContent = it }
                            )
                        }
                        "folder" -> {
                            FolderInputSection(
                                folderName = folderName,
                                folderAgent = folderAgent,
                                folderDesc = folderDesc,
                                onNameChange = { folderName = it },
                                onAgentChange = { folderAgent = it },
                                onDescChange = { folderDesc = it }
                            )
                        }
                        "link" -> {
                            LinkInputSection(
                                webLinkUrl = webLinkUrl,
                                linkDescription = linkDescription,
                                onUrlChange = { webLinkUrl = it },
                                onDescChange = { linkDescription = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Analyze Trigger Button
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isAnalyzing = true
                                extractedResult = when (currentMode) {
                                    "image" -> {
                                        if (selectedBitmap != null) {
                                            analyzer.analyzeImage(selectedBitmap!!, companies, imageHintText)
                                        } else {
                                            analyzer.analyzeImage(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888), companies, imageHintText.ifBlank { "كلافوكس سبيماكو" })
                                        }
                                    }
                                    "file" -> analyzer.analyzeDocumentOrFile(fileName, fileContent, companies)
                                    "folder" -> analyzer.analyzeCompanyFolder(folderName, folderDesc, folderAgent, companies)
                                    else -> analyzer.analyzeWebLink(webLinkUrl, linkDescription, companies)
                                }
                                isAnalyzing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAnalyzing) RoyalBlueMedium else PurpleGlow
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAnalyzing
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AccentGold, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري التحليل والتصنيف الأوتوماتيكي بالذكاء الاصطناعي...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تحليل واستخراج ذكي أوتوماتيكي ⚡",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Extracted Result Card
                extractedResult?.let { result ->
                    item {
                        ExtractedPreviewCard(result = result)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        },
        confirmButton = {
            if (extractedResult != null) {
                Button(
                    onClick = {
                        val res = extractedResult!!
                        val newCompany = if (currentMode == "folder") res.company else null
                        onSave(res.drug, res.archiveItem, newCompany)
                        Toast.makeText(context, "تم حفظ ${res.drug.tradeName} ومنحه أولوية البحث ⭐", Toast.LENGTH_LONG).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اعتماد وتوسيع قاعدة البيانات مع أولوية البحث ⭐",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("إلغاء", color = Color.White)
            }
        },
        containerColor = Color(0xFF131738)
    )
}

@Composable
private fun ImageInputSection(
    selectedBitmap: Bitmap?,
    imageHintText: String,
    onHintChange: (String) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column {
        if (selectedBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.5.dp, AccentEmerald, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = selectedBitmap.asImageBitmap(),
                    contentDescription = "صورة العبوة المرفوعة",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCameraClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleGlow),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصوير بالكاميرا", fontSize = 11.sp)
            }

            Button(
                onClick = onGalleryClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("من الاستوديو", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = imageHintText,
            onValueChange = onHintChange,
            label = { Text("ملاحظة أو اسم تقريبي للدواء (اختياري)", color = TextSecondary) },
            placeholder = { Text("مثال: شراب بروفين أو أقراص بنادول", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleNeon,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
    }
}

@Composable
private fun FileInputSection(
    fileName: String,
    fileContent: String,
    onFileNameChange: (String) -> Unit,
    onFileContentChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = fileName,
            onValueChange = onFileNameChange,
            label = { Text("اسم الملف أو الكتالوج (PDF/Doc)", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalBlueBright,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fileContent,
            onValueChange = onFileContentChange,
            label = { Text("محتوى أو ملخص النشرة أو الكتالوج الطبي", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalBlueBright,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
private fun FolderInputSection(
    folderName: String,
    folderAgent: String,
    folderDesc: String,
    onNameChange: (String) -> Unit,
    onAgentChange: (String) -> Unit,
    onDescChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = folderName,
            onValueChange = onNameChange,
            label = { Text("اسم الشركة أو المجلد الجديد", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentEmerald,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = folderAgent,
            onValueChange = onAgentChange,
            label = { Text("الوكيل المعتمد في اليمن", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentEmerald,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = folderDesc,
            onValueChange = onDescChange,
            label = { Text("وصف المجلد والمنتجات المعتمدة", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentEmerald,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
private fun LinkInputSection(
    webLinkUrl: String,
    linkDescription: String,
    onUrlChange: (String) -> Unit,
    onDescChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = webLinkUrl,
            onValueChange = onUrlChange,
            label = { Text("رابط الموقع الرسمي أو البوابة الدوائية", color = TextSecondary) },
            placeholder = { Text("https://...", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = linkDescription,
            onValueChange = onDescChange,
            label = { Text("وصف الرابط أو اسم المستحضر الدوائي", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = RoyalBlueMedium,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
    }
}

@Composable
private fun ExtractedPreviewCard(result: SupervisorExtractionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B224E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, AccentGold, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "النتائج المستخرجة أوتوماتيكياً",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentEmerald.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "دقة ${result.confidence}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Company folder tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2E1C0A))
                    .padding(8.dp)
            ) {
                Text(
                    text = "📁 مجلد الشركة المخصص: ${result.company.nameAr} • الوكيل: ${result.drug.agent}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "💊 الاسم التجاري: ${result.drug.tradeName}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "🧪 المادة الفعالة: ${result.drug.genericName} (${result.drug.strength})",
                style = MaterialTheme.typography.bodySmall.copy(color = AccentCyan)
            )

            Text(
                text = "⏱️ الجرعة والاستخدام: ${result.drug.standardDosage}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF143026))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "⭐ الأولوية في البحث وتوسيع قاعدة البيانات:",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentEmerald, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = result.drug.supervisorNotes,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.sp)
                    )
                }
            }
        }
    }
}
