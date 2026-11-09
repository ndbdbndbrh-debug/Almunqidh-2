package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.UserRole
import com.example.data.model.YemenRemittanceNetwork
import com.example.data.repository.PharmaRepository
import com.example.ui.components.SubscriptionPaymentDialog
import com.example.ui.components.UserRoleSwitchDialog
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.ShareHelper

@Composable
fun SupervisorScreen(
    repository: PharmaRepository,
    modifier: Modifier = Modifier,
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUserRole by repository.currentUserRole.collectAsState()
    val subscription = repository.subscriptionDetails

    var showRoleDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

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
            .testTag("supervisor_screen_lazy_column")
    ) {
        // Supervisor Hero Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SupervisorHeroCard(subscription = subscription)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Active User Mode & Role Permissions Card
        item {
            ActiveUserRoleCard(
                currentRole = currentUserRole,
                onSwitchRole = { showRoleDialog = true }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Subscription & Yemeni Remittance Card
        item {
            SubscriptionRemittanceCard(
                subscription = subscription,
                onOpenFullDialog = { onNavigateToSubscription() }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Mission & Objectives Card
        item {
            MissionObjectivesCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Direct Supervisor Contact Actions
        item {
            SupervisorContactActionsCard(
                subscription = subscription,
                onShareApp = { ShareHelper.shareAppInvitation(context) }
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showRoleDialog) {
        UserRoleSwitchDialog(
            currentRole = currentUserRole,
            onRoleSelected = { repository.setUserRole(it) },
            onDismiss = { showRoleDialog = false }
        )
    }

    if (showSubscriptionDialog) {
        SubscriptionPaymentDialog(
            subscriptionDetails = subscription,
            onDismiss = { showSubscriptionDialog = false }
        )
    }
}

@Composable
private fun SupervisorHeroCard(subscription: SubscriptionDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(AccentGold, PurpleElectric, RoyalBlueBright)),
                    shape = RoundedCornerShape(22.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2E1153), Color(0xFF14193F))
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(PurpleNeon, RoyalBlueBright))
                    )
                    .border(2.dp, AccentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "المشرف الصيدلاني د.ص أنس يحيى باقاضي",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "د.ص أنس يحيى باقاضي",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = "المشرف العام والمصادق الصيدلاني المعتمد",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = AccentGold,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone & Email pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B234F))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📱 هاتف/واتساب: ${subscription.supervisorPhone}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "✉️ ${subscription.supervisorEmail}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun ActiveUserRoleCard(
    currentRole: UserRole,
    onSwitchRole: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (currentRole == UserRole.SUPERVISOR) AccentGold else AccentCyan,
                    RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentRole == UserRole.SUPERVISOR) Icons.Default.VerifiedUser else Icons.Default.Group,
                        contentDescription = null,
                        tint = if (currentRole == UserRole.SUPERVISOR) AccentGold else AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نوع المستخدم النشط حالياً",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Button(
                    onClick = onSwitchRole,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentRole == UserRole.SUPERVISOR) AccentGold else AccentCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "تبديل الصلاحية",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentRole.titleAr,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (currentRole == UserRole.SUPERVISOR) AccentGold else AccentCyan
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentRole.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
private fun SubscriptionRemittanceCard(
    subscription: SubscriptionDetails,
    onOpenFullDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentEmerald.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خطة الاشتراك والرسوم",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentEmerald.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تجربة شهر مجاناً",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💰 خطط الرسوم بعد الشهر المجاني:\n• 25 ريال سعودي لمدة أربعة أشهر (4 أشهر)\n• 50 ريال سعودي اشتراك سنوي كامل (12 شهراً)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🏦 تُرسل الحوالات إلى شبكات الصرافة والمحافظ الإلكترونية اليمنية المعتمدة:",
                style = MaterialTheme.typography.labelMedium.copy(color = AccentCyan)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "• شركة القطيبي للصرافة\n• بنك الشامل للصرافة\n• بنك الكريمي (الكريمي إكسبرس / المميز)\n• حاسب (بنك اليمن والكويت)\n• حسب\n• محفظة جوالي\n• محفظة كاش (Cash)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenFullDialog,
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "عرض بيانات التحويل والنسخ الفوري",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun SupervisorContactActionsCard(
    subscription: SubscriptionDetails,
    onShareApp: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RoyalBlueMedium, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "📞 قنوات التواصل المباشر مع د.ص أنس باقاضي",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Button
                Button(
                    onClick = {
                        val uri = Uri.parse("https://wa.me/967${subscription.supervisorPhone}?text=" + Uri.encode("السلام عليكم د.ص أنس يحيى باقاضي، استفسار بخصوص تطبيق المنقذ الدوائي:"))
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("واتساب المشرف", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                // Call Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${subscription.supervisorPhone}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتصال هاتفي", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Email Button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${subscription.supervisorEmail}")
                        putExtra(Intent.EXTRA_SUBJECT, "استفسار - تطبيق المنقذ الدوائي")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PurpleNeon)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مراسلة عبر البريد: ${subscription.supervisorEmail}", color = Color.White)
            }
        }
    }
}

@Composable
private fun MissionObjectivesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RoyalBlueMedium, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = PurpleNeon,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "رؤية تطبيق المُنقذ الدوائي",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "• فك رموز خطوط الأطباء اليدوية الصعبة بدقة الذكاء الاصطناعي.\n• منع التداخلات الدوائية القاتلة والازدواجيات العلاجية.\n• أرشفة كاملة لأدوية الشركات والوكلاء المعتمدين في اليمن والشرق الأوسط.\n• تدقيق واعتماد صيدلاني سريري مستمر تحت إشراف د.ص أنس يحيى باقاضي.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            )
        }
    }
}
