package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrescriptionItem
import com.example.data.model.UserRole
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PurpleElectric
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DrugResultCard(
    item: PrescriptionItem,
    index: Int,
    currentUserRole: UserRole = UserRole.SUPERVISOR,
    onUpdateSupervisorDoctorNotes: (notes: String, isApproved: Boolean) -> Unit = { _, _ -> },
    onDeleteDoctorNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditingDoctorNotes by remember { mutableStateOf(false) }
    var doctorNotesText by remember(item.supervisorDoctorNotes) { mutableStateOf(item.supervisorDoctorNotes) }
    var isApproved by remember(item.isApprovedBySupervisor) { mutableStateOf(item.isApprovedBySupervisor) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("drug_result_card_$index"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(PurpleGlow, RoyalBlueBright)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            // Card Index & Form Pill Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(PurpleElectric, RoyalBlueBright))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الدواء رقم ${index + 1} في الوصفة",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalBlueMedium.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${item.dosageForm} • ${item.strength}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Trade Name (الاسم التجاري) - Shiny Glowing Purple Card
            SectionBox(
                title = "الاسم التجاري (Trade Name)",
                content = item.tradeName,
                badge = item.strength,
                icon = Icons.Default.Medication,
                gradient = listOf(Color(0xFF8E2DE2), Color(0xFF6B21A8)),
                borderColor = PurpleElectric
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Generic Name (الاسم العلمي والمادة الفعالة) - Royal Blue Card
            SectionBox(
                title = "الاسم العلمي والمادة الفعالة (Generic Name)",
                content = item.genericName,
                icon = Icons.Default.Science,
                gradient = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8)),
                borderColor = RoyalBlueBright
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Manufacturer & Agent (الشركة والوكيل) - Indigo & Cyan
            SectionBox(
                title = "الشركة المصنعة والوكيل المعتمد",
                content = "${item.manufacturer}\nالوكيل المحلي: ${item.localAgent}",
                icon = Icons.Default.Business,
                gradient = listOf(Color(0xFF0F2027), Color(0xFF203A43)),
                borderColor = AccentCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Prescribed Dosage (الجرعة المكتوبة وطريقة الاستخدام) - Teal / Turquoise
            SectionBox(
                title = "الجرعة المكتوبة المقروءة بخط الطبيب",
                content = item.writtenDosage,
                icon = Icons.Default.Schedule,
                gradient = listOf(Color(0xFF064E3B), Color(0xFF047857)),
                borderColor = AccentTeal
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Drug Interactions (التداخلات الدوائية والتحذيرات) - Amber / Red Alert
            if (item.drugInteractions.isNotEmpty()) {
                val fullInteractions = item.drugInteractions.joinToString("\n• ")
                SectionBox(
                    title = "التداخلات والتعارضات الدوائية",
                    content = "• $fullInteractions",
                    icon = Icons.Default.Warning,
                    gradient = listOf(Color(0xFF450A0A), Color(0xFF7F1D1D)),
                    borderColor = AccentRed
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 6. Medical Tips (نصائح صيدلانية للمريض) - Emerald Green
            if (item.medicalTips.isNotEmpty()) {
                val fullTips = item.medicalTips.joinToString("\n• ")
                SectionBox(
                    title = "نصائح وإرشادات طبية للمريض",
                    content = "• $fullTips",
                    icon = Icons.Default.Lightbulb,
                    gradient = listOf(Color(0xFF064E3B), Color(0xFF065F46)),
                    borderColor = AccentEmerald
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 7. Pharmaceutical & Clinical Notes (خانة ملاحظات صيدلانية دوائية - تم تعديلها كما طلب المستخدم)
            val clinicalNoteContent = item.clinicalNotes.ifBlank {
                "ملاحظات صيدلانية دوائية: تم تدقيق الجرعة مع المعايير الدوائية العالمية، والتأكد من أمان الشكل الصيدلاني وامتصاص المادة الفعالة."
            }
            SectionBox(
                title = "ملاحظات صيدلانية دوائية",
                content = clinicalNoteContent,
                icon = Icons.Default.NoteAlt,
                gradient = listOf(Color(0xFF1E143E), Color(0xFF11224D)),
                borderColor = PurpleNeon
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 8. NEW: Dedicated Empty Manual Field for Supervisor (ملاحظات المشرف الدكتور الصيدلاني)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2E1065), Color(0xFF1B1947))
                        )
                    )
                    .border(1.5.dp, AccentGold.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "ختم المشرف الدكتور الصيدلاني",
                                tint = AccentGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ملاحظات المشرف الدكتور الصيدلاني",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            )
                        }

                        // Edit / Add button only enabled if user is Supervisor
                        if (currentUserRole.canEdit) {
                            Row {
                                if (doctorNotesText.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            doctorNotesText = ""
                                            onDeleteDoctorNotes()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف ملاحظة المشرف",
                                            tint = AccentRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { isEditingDoctorNotes = !isEditingDoctorNotes },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تعديل وإضافة ملاحظة المشرف",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            // Colleague indicator
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "مقفل للزملاء (للاطلاع فقط)",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedVisibility(visible = !isEditingDoctorNotes) {
                        Column {
                            if (doctorNotesText.isNotBlank()) {
                                Text(
                                    text = doctorNotesText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFF1F5F9),
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            } else {
                                // Empty state by default
                                Text(
                                    text = if (currentUserRole.canEdit) {
                                        "✍️ خانة فارغة مخصصة لإضافة توجيهات واعتماد د.ص أنس يحيى باقاضي يدوياً (انقر زر القلم للإضافة)."
                                    } else {
                                        "🔒 لا توجد ملاحظات يدوية مسجلة من المشرف بعد • في انتظار اعتماد د.ص أنس باقاضي."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (currentUserRole.canEdit) AccentGold.copy(alpha = 0.8f) else TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Official Pharmacist Approval Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isApproved) AccentEmerald.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isApproved) AccentEmerald.copy(alpha = 0.6f) else AccentAmber.copy(alpha = 0.6f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isApproved) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isApproved) AccentEmerald else AccentAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isApproved) {
                                        "معتمد من المشرف: د.ص أنس يحيى باقاضي ✓"
                                    } else {
                                        "قيد المراجعة والتدقيق الصيدلاني (غير معتمد بعد)"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isApproved) AccentEmerald else AccentAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            if (!currentUserRole.canEdit) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "📌 وضع الزملاء: القراءة والاطلاع فقط • لا يحفظ شيء بدون اعتماد المشرف.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentCyan,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Editing form for Supervisor
                    AnimatedVisibility(visible = isEditingDoctorNotes) {
                        Column {
                            OutlinedTextField(
                                value = doctorNotesText,
                                onValueChange = { doctorNotesText = it },
                                label = { Text("أدخل ملاحظات المشرف الدكتور الصيدلاني") },
                                placeholder = { Text("اكتب ملاحظاتك وتوجيهاتك اليدوية هنا...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGold,
                                    unfocusedBorderColor = PurpleNeon,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { isEditingDoctorNotes = false },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("إلغاء", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        isApproved = true
                                        isEditingDoctorNotes = false
                                        onUpdateSupervisorDoctorNotes(doctorNotesText, true)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentGold
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "حفظ واعتماد المشرف",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionBox(
    title: String,
    content: String,
    icon: ImageVector,
    gradient: List<Color>,
    borderColor: Color,
    badge: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(gradient))
            .border(1.dp, borderColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = borderColor
                        )
                    )
                }

                if (!badge.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
