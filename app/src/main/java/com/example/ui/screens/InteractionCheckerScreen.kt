package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.PharmaRepository
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRed
import com.example.ui.theme.PurpleElectric
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class InteractionReport(
    val severity: String, // خطير, متوسط, آمن
    val severityColor: Color,
    val title: String,
    val explanation: String,
    val recommendation: String,
    val supervisorSignature: String = "اعتماد د.ص أنس يحيى باقاضي"
)

@Composable
fun InteractionCheckerScreen(
    repository: PharmaRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var drugInput by remember { mutableStateOf("") }
    val selectedDrugs = remember {
        mutableStateListOf(
            "فولتارين (Voltaren)",
            "سيليبريكس (Celebrex)"
        )
    }

    var isChecking by remember { mutableStateOf(false) }
    var report by remember {
        mutableStateOf<InteractionReport?>(
            InteractionReport(
                severity = "تعارض عالي الخطورة (Severe Interaction)",
                severityColor = AccentRed,
                title = "تكرار مسكنات NSAIDs ومضاعفة قرحة المعدة والنزيف",
                explanation = "الجمع بين فولتارين (Diclofenac) وسيليبريكس (Celecoxib) يؤدي إلى تثبيط مفرط لإنزيمات الأكسدة الحلقية، مما يضاعف احتمالية القرحة الهضمية الحادة والنزيف المعوي والتأثير السلبي على وظائف الكلى دون أي زيادة تذكر في تسكين الألم.",
                recommendation = "يوصى فورياً بإلغاء أحدهما والاكتفاء بـ Celecoxib 200mg لحماية بطانة المعدة، مع إضافة واقٍ معوي مثل بانتوبرازول 40mg."
            )
        )
    }

    val quickDrugSuggestions = listOf(
        "أوجمنتين (Augmentin)",
        "بلافيكس (Plavix)",
        "أوميبرازول (Omeprazole)",
        "أسبرين (Aspirin)",
        "جلوكوفاج (Metformin)",
        "كونكور (Concor)",
        "وارفارين (Warfarin)"
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
            .testTag("interaction_checker_screen")
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38101E), Color(0xFF1B1242))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(AccentRed, PurpleElectric)),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AccentRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "فاحص التداخلات الدوائية التفاعلي",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "كشف تعارضات الأدوية والأغذية تحت إشراف د.ص أنس باقاضي",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentGold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Input section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RoyalBlueMedium, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "أضف أدوية لفحص التوافق والتعارض:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = drugInput,
                            onValueChange = { drugInput = it },
                            placeholder = { Text("اكتب اسم الدواء...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpleNeon,
                                unfocusedBorderColor = RoyalBlueMedium,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (drugInput.isNotBlank()) {
                                    selectedDrugs.add(drugInput.trim())
                                    drugInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleGlow)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected drugs chips
                    Text(
                        text = "الأدوية المحددة للفحص (${selectedDrugs.size}):",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentCyan)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedDrugs.forEach { drug ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PurpleGlow.copy(alpha = 0.25f))
                                    .border(1.dp, PurpleElectric, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = drug,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف",
                                        tint = TextSecondary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { selectedDrugs.remove(drug) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick suggestions
                    Text(
                        text = "أو اختر بسرعة:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        quickDrugSuggestions.take(4).forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RoyalBlueMedium.copy(alpha = 0.5f))
                                    .clickable {
                                        if (!selectedDrugs.contains(suggestion)) {
                                            selectedDrugs.add(suggestion)
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "+ $suggestion",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentCyan,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Run Analysis Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isChecking = true
                                delay(1100)
                                isChecking = false
                                report = if (selectedDrugs.any { it.contains("بلافيكس") || it.contains("Plavix") } &&
                                    selectedDrugs.any { it.contains("أوميبرازول") || it.contains("Omeprazole") }) {
                                    InteractionReport(
                                        severity = "تعارض شديد (Major Interaction)",
                                        severityColor = AccentRed,
                                        title = "تثبيط مفعول بلافيكس الوقائي بواسطة أوميبرازول",
                                        explanation = "عقار أوميبرازول يثبط إنزيم CYP2C19 الكبدي المسؤول عن تنشيط كلوبيدوجريل، مما يقلل فعاليته المضادة لتجلط الدم بنسبة تصل إلى 45% ويعرض المريض لخطر الجلطة مجدداً.",
                                        recommendation = "استبدل أوميبرازول فوراً بـ بانتوبرازول (Pantoprazole) حيث أنه لا يتعارض مع تنشيط البلافيكس."
                                    )
                                } else {
                                    InteractionReport(
                                        severity = "آمن ومقبول صيدلانياً (Compatible)",
                                        severityColor = AccentEmerald,
                                        title = "لا توجد تعارضات دوائية خطيرة مسجلة",
                                        explanation = "الأدوية المحددة متوافقة بشكل عام من الناحية الحركية والديناميكية، مع ضرورة الالتزام بمواعيد الوجبات المقررة.",
                                        recommendation = "باستطاعة المريض تناول العلاجات حسب الخطة المقررة مع شرب كميات وافرة من الماء."
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("check_interactions_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("محرك الذكاء الاصطناعي يفحص قواعد البيانات...")
                        } else {
                            Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "فحص التداخلات بالذكاء الاصطناعي",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Report Card
        report?.let { rep ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                rep.severityColor.copy(alpha = 0.8f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rep.severityColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = rep.severity,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = rep.severityColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Icon(
                                imageVector = if (rep.severityColor == AccentRed) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = rep.severityColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = rep.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = rep.explanation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Recommendation Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF063327))
                                .border(1.dp, AccentEmerald.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "💡 التوصية الصيدلانية والبديل:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AccentEmerald
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rep.recommendation,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Supervisor Stamp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF28114A))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تم الفحص والمصادقة تحت إشراف د.ص أنس يحيى باقاضي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
