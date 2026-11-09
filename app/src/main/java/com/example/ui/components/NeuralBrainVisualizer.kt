package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PurpleElectric
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueDeep
import kotlin.math.cos
import kotlin.math.sin

enum class BrainState {
    IDLE,
    UPLOADING,
    ANALYZING,
    SUCCESS
}

@Composable
fun NeuralBrainVisualizer(
    state: BrainState,
    confidence: String = "98.5%",
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brainAnimations")

    // Pulse animation for gentle breathing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Fast rotation for active analysis rings
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == BrainState.ANALYZING) 2500 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == BrainState.ANALYZING) 3000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterRotation"
    )

    // Glowing aura color based on state
    val auraColor = when (state) {
        BrainState.IDLE -> PurpleGlow
        BrainState.UPLOADING -> AccentCyan
        BrainState.ANALYZING -> PurpleElectric
        BrainState.SUCCESS -> AccentEmerald
    }

    val stateText = when (state) {
        BrainState.IDLE -> "محرك الدماغ العصبي جاهز لقراءة أي وصفة طبية"
        BrainState.UPLOADING -> "جاري تحميل وقراءة بيانات الروشتة..."
        BrainState.ANALYZING -> "الذكاء الاصطناعي يفكك الخط اليدوي ويفحص التداخلات..."
        BrainState.SUCCESS -> "اكتمل التحليل الطبي الدقيق بنجاح بنسبة دقة $confidence"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brain Container with Layered Glowing Orbs & Synapses
        Box(
            modifier = Modifier
                .size(230.dp)
                .clickable { onTap() }
                .testTag("neural_brain_container"),
            contentAlignment = Alignment.Center
        ) {
            // Outer dynamic synapse aura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .drawBehind {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2

                        // Outer gradient aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    auraColor.copy(alpha = 0.45f),
                                    RoyalBlueBright.copy(alpha = 0.20f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = radius
                            ),
                            radius = radius
                        )

                        // Orbiting synaptic neural nodes
                        val nodeCount = if (state == BrainState.ANALYZING) 12 else 8
                        for (i in 0 until nodeCount) {
                            val angle = (i * (360f / nodeCount) + ringRotation) * (Math.PI / 180.0)
                            val nodeRadius = radius * 0.90f
                            val x = center.x + (nodeRadius * cos(angle)).toFloat()
                            val y = center.y + (nodeRadius * sin(angle)).toFloat()
                            drawCircle(
                                color = if (i % 2 == 0) PurpleElectric else AccentCyan,
                                radius = if (state == BrainState.ANALYZING) 5.dp.toPx() else 3.5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
            )

            // Counter-rotating decorative cybernetic ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .rotate(counterRotation)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    PurpleGlow.copy(alpha = 0.8f),
                                    AccentCyan.copy(alpha = 0.6f),
                                    RoyalBlueBright.copy(alpha = 0.4f),
                                    PurpleGlow.copy(alpha = 0.8f)
                                )
                            ),
                            style = Stroke(
                                width = if (state == BrainState.ANALYZING) 3.5.dp.toPx() else 1.8.dp.toPx()
                            )
                        )
                    }
            )

            // Inner Rotating dashed ring
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .rotate(ringRotation)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(AccentGold.copy(alpha = 0.6f), PurpleNeon.copy(alpha = 0.7f))
                        ),
                        shape = CircleShape
                    )
            )

            // Center Brain Image / Visual
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                RoyalBlueDeep,
                                Color(0xFF160D30)
                            )
                        )
                    )
                    .border(2.dp, auraColor.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.neural_brain_scan),
                    contentDescription = "الدماغ العصبي التفاعلي لقراءة الوصفات",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay status effect when analyzing
                if (state == BrainState.ANALYZING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PurpleGlow.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                } else if (state == BrainState.SUCCESS) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AccentEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "تم التحليل بنجاح",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            auraColor.copy(alpha = 0.22f),
                            RoyalBlueBright.copy(alpha = 0.15f)
                        )
                    )
                )
                .border(1.dp, auraColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (state) {
                    BrainState.IDLE -> Icons.Default.Psychology
                    BrainState.UPLOADING -> Icons.Default.Sync
                    BrainState.ANALYZING -> Icons.Default.AutoAwesome
                    BrainState.SUCCESS -> Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = auraColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stateText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
