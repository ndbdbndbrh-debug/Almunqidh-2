package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionDetails
import com.example.data.model.YemenRemittanceNetwork
import com.example.data.repository.PharmaRepository
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlansScreen(
    repository: PharmaRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val subscription = repository.subscriptionDetails

    var selectedPlanId by remember { mutableStateOf("plan_4_months") } // free_month, plan_4_months, plan_annual
    var selectedNetwork by remember { mutableStateOf(subscription.networks.firstOrNull()) }

    var senderName by remember { mutableStateOf("") }
    var transferRefNumber by remember { mutableStateOf("") }
    var showConfirmationForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        RoyalBlueDeep,
                        Color(0xFF0D1438),
                        Color(0xFF190D32),
                        RoyalBlueDeep
                    )
                )
            )
            .padding(horizontal = 14.dp)
            .testTag("subscription_plans_screen")
    ) {
        // Screen Header & Supervisor Badge
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SubscriptionHeaderCard(subscription = subscription)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Active Free Trial Banner
        item {
            FreeTrialStatusCard(subscription = subscription)
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section Title: Subscription Plans
        item {
            SectionTitle(
                title = "باقات الاشتراك المتاحة",
                subtitle = "اختر الباقة المناسبة لاحتياجاتك واستمتع بكافة الميزات السريرية",
                icon = Icons.Default.WorkspacePremium,
                accentColor = AccentGold
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Plan 1: Free Month
        item {
            PlanCard(
                title = "باقة التجربة المجانية",
                badge = "مفتوح للجميع",
                price = "مجاناً بالكامل",
                duration = "شهر كامل (30 يوماً)",
                description = "فحص وقراءة غير محدودة للوصفات الطبية، كشف التداخلات الدوائية، وتصفح دليل شركات الأدوية دون أي رسوم.",
                features = listOf(
                    "قراءة الوصفات اليدوية والمطبوعة بالذكاء الاصطناعي",
                    "فحص التعارضات والتداخلات الدوائية",
                    "الوصول الكامل لأرشيف شركات الأدوية في اليمن",
                    "صلاحية شهر كامل بدون شروط أو بطاقات ائتمان"
                ),
                isCurrent = true,
                isSelected = selectedPlanId == "free_month",
                borderColor = AccentEmerald,
                accentColor = AccentEmerald,
                badgeBg = AccentEmerald.copy(alpha = 0.2f),
                onSelect = {
                    selectedPlanId = "free_month"
                    Toast.makeText(context, "أنت الآن تستمتع بالباقة المجانية", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Plan 2: 4 Months Plan (25 SAR)
        item {
            PlanCard(
                title = "باقة التوفير (٤ أشهر)",
                badge = "الأكثر شعبية ⚡",
                price = "25 ريال سعودي",
                duration = "أربعة أشهر (120 يوماً)",
                description = "استمرارية كاملة مع دعم سريري مباشر وميزات متقدمة للصيادلة والأطباء والمستشفيات.",
                features = listOf(
                    "كافة ميزات الباقة المجانية بدون توقف",
                    "تحديثات مستمرة لكتالوجات الوكلاء والشركات الدوائية",
                    "إمكانية إرسال استفسارات دوائية للمشرف د.ص أنس باقاضي",
                    "توفير مالي كبير (فقط 6.25 ريال شهرياً)"
                ),
                isCurrent = false,
                isSelected = selectedPlanId == "plan_4_months",
                borderColor = PurpleElectric,
                accentColor = PurpleNeon,
                badgeBg = PurpleGlow.copy(alpha = 0.35f),
                onSelect = {
                    selectedPlanId = "plan_4_months"
                    showConfirmationForm = true
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Plan 3: Annual Plan (50 SAR)
        item {
            PlanCard(
                title = "الاشتراك السنوي الذهبي",
                badge = "أفضل قيمة 👑 توفير 50%",
                price = "50 ريال سعودي",
                duration = "سنة كاملة (12 شهراً)",
                description = "الحل الشامل والمثالي للصيدليات والمراكز الطبية مع الأولوية القصوى في الدعم والمصادقات.",
                features = listOf(
                    "وصول سنوي غير محدود وتحديثات يومية متواصلة",
                    "أولوية قصوى في اعتماد المشرف الصيدلاني للتقارير والوصفات",
                    "أرشفة دائمة وغير محدودة لكافة الوصفات والملفات",
                    "قيمة استثنائية (أقل من 4.2 ريال سعودي شهرياً)"
                ),
                isCurrent = false,
                isSelected = selectedPlanId == "plan_annual",
                borderColor = AccentGold,
                accentColor = AccentGold,
                badgeBg = AccentGold.copy(alpha = 0.3f),
                onSelect = {
                    selectedPlanId = "plan_annual"
                    showConfirmationForm = true
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section Title: Available Yemeni Remittance Methods
        item {
            SectionTitle(
                title = "طرق وشبكات التحويل المعتمدة في اليمن",
                subtitle = "تُرسل الحوالات والرسوم إلى أي من الشبكات والمحافظ التالية مع ميزة النسخ الفوري",
                icon = Icons.Default.AccountBalance,
                accentColor = AccentCyan
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // List of Yemeni Remittance Networks
        items(subscription.networks) { network ->
            YemenRemittanceCard(
                network = network,
                isSelected = selectedNetwork?.id == network.id,
                onSelect = {
                    selectedNetwork = network
                    showConfirmationForm = true
                },
                onCopy = {
                    copyToClipboard(
                        context = context,
                        text = "${network.name}: ${network.details}",
                        toastMsg = "تم نسخ بيانات ${network.name}"
                    )
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: Supervisor Contact Details
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(
                title = "معلومات التواصل الخاصة بالمشرف",
                subtitle = "للاستفسارات وتأكيد عمليات التحويل وتفعيل الاشتراكات",
                icon = Icons.Default.Person,
                accentColor = AccentGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            SupervisorDetailedContactCard(
                subscription = subscription,
                onWhatsApp = {
                    val planName = when (selectedPlanId) {
                        "plan_4_months" -> "باقة 4 أشهر (25 ريال سعودي)"
                        "plan_annual" -> "الاشتراك السنوي (50 ريال سعودي)"
                        else -> "استفسار عن الاشتراك المجاني"
                    }
                    val networkText = selectedNetwork?.name ?: "إحدى شبكات الصرافة اليمنية"
                    val msg = "السلام عليكم د.ص أنس يحيى باقاضي، أرغب في تأكيد اشتراك تطبيق المنقذ الدوائي:\n- الباقة: $planName\n- طريقة التحويل: $networkText"
                    val uri = Uri.parse("https://wa.me/967${subscription.supervisorPhone}?text=" + Uri.encode(msg))
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${subscription.supervisorPhone}")
                    }
                    context.startActivity(intent)
                },
                onEmail = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${subscription.supervisorEmail}")
                        putExtra(Intent.EXTRA_SUBJECT, "تأكيد اشتراك تطبيق المنقذ الدوائي")
                    }
                    context.startActivity(intent)
                },
                onCopyPhone = {
                    copyToClipboard(context, subscription.supervisorPhone, "تم نسخ رقم المشرف: ${subscription.supervisorPhone}")
                },
                onCopyEmail = {
                    copyToClipboard(context, subscription.supervisorEmail, "تم نسخ البريد الإلكتروني للمشرف")
                }
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section: Transfer Confirmation & WhatsApp Notification Form
        item {
            TransferNotificationForm(
                selectedPlanId = selectedPlanId,
                selectedNetwork = selectedNetwork,
                senderName = senderName,
                transferRefNumber = transferRefNumber,
                onSenderNameChange = { senderName = it },
                onTransferRefChange = { transferRefNumber = it },
                onSendNotification = {
                    val planName = when (selectedPlanId) {
                        "plan_4_months" -> "باقة 4 أشهر (25 ريال سعودي)"
                        "plan_annual" -> "الاشتراك السنوي الكامل (50 ريال سعودي)"
                        else -> "باقة التجربة المجانية"
                    }
                    val networkName = selectedNetwork?.name ?: "التحويلات اليمنية"
                    val message = """
                        السلام عليكم ورحمة الله،
                        إلى المشرف الصيدلاني د.ص أنس يحيى باقاضي المحترم:
                        
                        تم إرسال إشعار سداد لتفعيل تطبيق *المنقذ الدوائي*:
                        📋 *الباقة المطلوبة:* $planName
                        🏦 *طريقة التحويل:* $networkName
                        👤 *اسم المودع/المحول:* ${senderName.ifBlank { "لم يحدد" }}
                        🔢 *رقم الحوالة/السند:* ${transferRefNumber.ifBlank { "مرفق مع الرسالة" }}
                        
                        نرجو اعتماد وتفعيل الاشتراك ولكم جزيل الشكر.
                    """.trimIndent()

                    val uri = Uri.parse("https://wa.me/967${subscription.supervisorPhone}?text=" + Uri.encode(message))
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SubscriptionHeaderCard(subscription: SubscriptionDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(PurpleNeon, AccentCyan, AccentGold)),
                    shape = RoundedCornerShape(22.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF241349), Color(0xFF111738))
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PurpleGlow.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "باقات الاشتراك والتحويلات",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "تطبيق المنقذ الدوائي • د.ص أنس يحيى باقاضي",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AccentGold,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "نظام اشتراك عادل وشفاف يضمن استمرارية التحليل الذكي للوصفات الطبية بدقة وسرعة وبدون أي تعقيدات.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

@Composable
private fun FreeTrialStatusCard(subscription: SubscriptionDetails) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF0D3E2B), Color(0xFF145339), Color(0xFF0F2E23))
                )
            )
            .border(1.2.dp, AccentEmerald, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AccentEmerald.copy(alpha = 0.25f))
                    .border(1.5.dp, AccentEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "فترة تجريبية مجانية لمدة شهر كامل",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = AccentEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentEmerald)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "نشط الآن",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "جميع ميزات التطبيق مجانية بالكامل للجميع بدون أي قيود لمدة 30 يوماً (متبقي ${subscription.trialDaysRemaining} يوماً).",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    badge: String,
    price: String,
    duration: String,
    description: String,
    features: List<String>,
    isCurrent: Boolean,
    isSelected: Boolean,
    borderColor: Color,
    accentColor: Color,
    badgeBg: Color,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) borderColor else borderColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    if (isSelected) {
                        Brush.verticalGradient(listOf(Color(0xFF221644), Color(0xFF13193C)))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF161B3B), Color(0xFF0F142E)))
                    }
                )
                .padding(16.dp)
        ) {
            // Top Row: Title + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price and Duration
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/ $duration",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Feature Bullets
            features.forEach { feat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feat,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) accentColor else Color(0xFF232D58)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isCurrent) Icons.Default.Verified else Icons.Default.Payments,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isCurrent -> "الباقة الحالية النشطة"
                        isSelected -> "تم اختيار هذه الباقة ✓ (المتابعة للسداد)"
                        else -> "اختيار هذه الباقة والتحويل"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun YemenRemittanceCard(
    network: YemenRemittanceNetwork,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isSelected) 1.8.dp else 1.dp,
                    color = if (isSelected) AccentCyan else RoyalBlueMedium,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    if (isSelected) Color(0xFF1B2856) else Color(0xFF141A3E)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else Color(0xFF1F2650)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = if (isSelected) AccentCyan else AccentGold,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = network.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentCyan)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "محدد للسداد",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = network.details,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222B58))
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "نسخ بيانات التحويل",
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SupervisorDetailedContactCard(
    subscription: SubscriptionDetails,
    onWhatsApp: () -> Unit,
    onCall: () -> Unit,
    onEmail: () -> Unit,
    onCopyPhone: () -> Unit,
    onCopyEmail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(AccentGold, PurpleElectric, AccentCyan)),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2B1452), Color(0xFF141A42))
                    )
                )
                .padding(18.dp)
        ) {
            // Supervisor Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(PurpleNeon, RoyalBlueBright)))
                        .border(2.dp, AccentGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "د.ص أنس يحيى باقاضي",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "د.ص أنس يحيى باقاضي",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "المشرف الصيدلاني العام والمصادق المعتمد",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AccentGold,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phone Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B2352))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "رقم الهاتف / واتساب المشرف", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                        Text(text = subscription.supervisorPhone, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }

                Row {
                    IconButton(onClick = onCopyPhone, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ الرقم", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B2352))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "البريد الإلكتروني الرسمي", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                        Text(text = subscription.supervisorEmail, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Medium))
                    }
                }

                Row {
                    IconButton(onClick = onCopyEmail, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ البريد", tint = PurpleNeon, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fast Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWhatsApp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("واتساب مباشر", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتصال هاتفي", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onEmail,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("إرسال بريد إلكتروني رسمي للمشرف")
            }
        }
    }
}

@Composable
private fun TransferNotificationForm(
    selectedPlanId: String,
    selectedNetwork: YemenRemittanceNetwork?,
    senderName: String,
    transferRefNumber: String,
    onSenderNameChange: (String) -> Unit,
    onTransferRefChange: (String) -> Unit,
    onSendNotification: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, AccentEmerald.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF142B38), Color(0xFF101938))
                    )
                )
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إشعار سداد وتفعيل فوري للاشتراك",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "بعد إرسال الحوالة عبر إحدى شبكات الصرافة (قطيبي، الشاملة، الكريمي، حاسب، حسب، جوالي، كاش)، أدخل البيانات واضغط لإرسال إشعار فوري للمشرف لتفعيل الحساب:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Plan and Network Summary Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF19244E))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedPlanId) {
                        "plan_4_months" -> "الباقة: 4 أشهر (25 ريال سعودي)"
                        "plan_annual" -> "الباقة: اشتراك سنوي (50 ريال سعودي)"
                        else -> "الباقة: شهر تجريبي مجاني"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = selectedNetwork?.name ?: "التحويلات اليمنية",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sender Name Field
            OutlinedTextField(
                value = senderName,
                onValueChange = onSenderNameChange,
                label = { Text("اسم المودع / المرسل (اختياري)", color = TextSecondary) },
                placeholder = { Text("مثال: د. أحمد صالح", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentEmerald,
                    unfocusedBorderColor = RoyalBlueMedium,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Transfer Reference Number Field
            OutlinedTextField(
                value = transferRefNumber,
                onValueChange = onTransferRefChange,
                label = { Text("رقم الحوالة أو السند / ملاحظات إضافية", color = TextSecondary) },
                placeholder = { Text("أدخل رقم الحوالة أو السند من شركة الصرافة", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentEmerald,
                    unfocusedBorderColor = RoyalBlueMedium,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = onSendNotification,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إرسال إشعار السداد للمشرف عبر واتساب 🚀",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMsg: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("PharmaSavior", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
}
