package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiPrescriptionAnalyzer
import com.example.data.local.PharmaDatabase
import com.example.data.repository.PharmaRepository
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.InteractionCheckerScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SubscriptionPlansScreen
import com.example.ui.screens.SupervisorScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PurpleElectric
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

enum class AppScreen(val titleAr: String, val icon: ImageVector, val tag: String) {
    SCANNER("الوصفات", Icons.Default.Psychology, "nav_scanner"),
    ARCHIVE("الشركات", Icons.Default.Folder, "nav_archive"),
    INTERACTIONS("التداخلات", Icons.Default.FlashOn, "nav_interactions"),
    SUBSCRIPTION("باقات الاشتراك", Icons.Default.Payments, "nav_subscription"),
    SUPERVISOR("د.ص أنس", Icons.Default.Person, "nav_supervisor")
}

class MainActivity : ComponentActivity() {

    private val database by lazy { PharmaDatabase.getInstance(applicationContext) }
    private val repository by lazy { PharmaRepository(database = database) }
    private val analyzer by lazy { GeminiPrescriptionAnalyzer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PharmaAppContent(repository = repository, analyzer = analyzer)
                }
            }
        }
    }
}

@Composable
fun PharmaAppContent(
    repository: PharmaRepository,
    analyzer: GeminiPrescriptionAnalyzer
) {
    var currentScreen by remember { mutableStateOf(AppScreen.SCANNER) }

    // Handle back press on sub-screens to return to Scanner
    if (currentScreen != AppScreen.SCANNER) {
        BackHandler {
            currentScreen = AppScreen.SCANNER
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            PharmaBottomNavBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.SCANNER -> ScannerScreen(
                        repository = repository,
                        analyzer = analyzer,
                        onNavigateToSubscription = { currentScreen = AppScreen.SUBSCRIPTION }
                    )
                    AppScreen.ARCHIVE -> ArchiveScreen(
                        repository = repository
                    )
                    AppScreen.INTERACTIONS -> InteractionCheckerScreen(
                        repository = repository
                    )
                    AppScreen.SUBSCRIPTION -> SubscriptionPlansScreen(
                        repository = repository
                    )
                    AppScreen.SUPERVISOR -> SupervisorScreen(
                        repository = repository,
                        onNavigateToSubscription = { currentScreen = AppScreen.SUBSCRIPTION }
                    )
                }
            }
        }
    }
}

@Composable
fun PharmaBottomNavBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(RoyalBlueDeep)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(PurpleGlow.copy(alpha = 0.6f), RoyalBlueMedium, AccentCyan.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(22.dp)
                ),
            containerColor = Color(0xFF111738),
            tonalElevation = 8.dp
        ) {
            AppScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onScreenSelected(screen) },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.titleAr,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = screen.titleAr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PurpleNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted,
                        indicatorColor = PurpleGlow.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.testTag(screen.tag)
                )
            }
        }
    }
}
