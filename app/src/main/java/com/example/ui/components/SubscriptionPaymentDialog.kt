package com.example.ui.components

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
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

@Composable
fun SubscriptionPaymentDialog(
    subscriptionDetails: SubscriptionDetails,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الاشتراك والتحويلات المالية",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
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
                    .testTag("subscription_dialog_list")
            ) {
                // Free Trial Status Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0F3E2B), Color(0xFF145339))
                                )
                            )
                            .border(1.dp, AccentEmerald, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "فترة تجريبية مجانية لمدة شهر كامل",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "التطبيق مجاني بالكامل لمدة شهر (متبقي ${subscriptionDetails.trialDaysRemaining} يوماً من التجربة المجانية).",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subscription Plans Row (25 SAR for 4 months & 50 SAR for Annual)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Plan 1: 25 SAR for 4 Months
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E1642))
                                .border(1.5.dp, PurpleElectric, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PurpleGlow.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "باقة 4 أشهر",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PurpleNeon,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "25 سعودي",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "لمدة أربعة أشهر",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Plan 2: 50 SAR Annual Plan
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF2E1C0A))
                                .border(1.5.dp, AccentGold, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentGold.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "اشتراك سنوي 👑",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AccentGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "50 سعودي",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "سنة كاملة (12 شهراً)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccentGold.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Supervisor Contact Box
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF28104E), Color(0xFF15193C))
                                )
                            )
                            .border(1.dp, PurpleElectric, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "👨‍⚕️ بيانات المشرف المعتمد لإرسال إشعارات التحويل:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Phone & WhatsApp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${subscriptionDetails.supervisorPhone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "رقم المشرف: ${subscriptionDetails.supervisorPhone}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, subscriptionDetails.supervisorPhone, "تم نسخ رقم المشرف")
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "نسخ الرقم",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Email
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${subscriptionDetails.supervisorEmail}")
                                            putExtra(Intent.EXTRA_SUBJECT, "تأكيد اشتراك تطبيق المنقذ الدوائي")
                                        }
                                        context.startActivity(intent)
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = PurpleNeon,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subscriptionDetails.supervisorEmail,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, subscriptionDetails.supervisorEmail, "تم نسخ البريد الإلكتروني")
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "نسخ البريد",
                                        tint = PurpleNeon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "🏦 شبكات الصرافة والمحافظ الإلكترونية اليمنية المعتمدة:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // List of Yemeni Remittance Networks
                items(subscriptionDetails.networks) { network ->
                    YemenWalletRow(
                        network = network,
                        onCopy = {
                            copyToClipboard(context, "${network.name}: ${network.details}", "تم نسخ بيانات ${network.name}")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = Uri.parse("https://wa.me/967${subscriptionDetails.supervisorPhone}?text=" + Uri.encode("السلام عليكم د.ص أنس باقاضي، استفسار بشأن تفعيل الاشتراك / إرسال إشعار تحويل لتطبيق المنقذ الدوائي."))
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "تأكيد التحويل عبر واتساب",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("إغلاق", color = Color.White)
            }
        },
        containerColor = Color(0xFF131838)
    )
}

@Composable
private fun YemenWalletRow(
    network: YemenRemittanceNetwork,
    onCopy: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF19204A))
            .border(1.dp, RoyalBlueMedium, RoundedCornerShape(12.dp))
            .clickable { onCopy() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = network.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = network.details,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "نسخ",
                    tint = AccentCyan,
                    modifier = Modifier.size(16.dp)
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
