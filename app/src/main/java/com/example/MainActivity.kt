package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.analytics.WellbeingAnalyticsScreen
import com.example.ui.drawer.AppDrawerScreen
import com.example.ui.launcher.HomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DistractionFreeTheme

enum class LauncherScreen {
    HOME,
    APP_DRAWER,
    ANALYTICS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
            var currentScreen by remember { mutableStateOf(LauncherScreen.HOME) }

            DistractionFreeTheme(themePreset = selectedTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        BentoBottomNav(
                            currentScreen = currentScreen,
                            onSelectScreen = { currentScreen = it }
                        )
                    }
                ) { innerPadding ->

                    // Handle back press to navigate to Home before exiting
                    BackHandler(enabled = currentScreen != LauncherScreen.HOME) {
                        currentScreen = LauncherScreen.HOME
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                LauncherScreen.HOME -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onOpenAppDrawer = { currentScreen = LauncherScreen.APP_DRAWER },
                                        onOpenAnalytics = { currentScreen = LauncherScreen.ANALYTICS },
                                        onOpenSettings = { currentScreen = LauncherScreen.SETTINGS }
                                    )
                                }
                                LauncherScreen.APP_DRAWER -> {
                                    AppDrawerScreen(
                                        viewModel = viewModel,
                                        onBackToHome = { currentScreen = LauncherScreen.HOME }
                                    )
                                }
                                LauncherScreen.ANALYTICS -> {
                                    WellbeingAnalyticsScreen(
                                        viewModel = viewModel,
                                        onBackToHome = { currentScreen = LauncherScreen.HOME }
                                    )
                                }
                                LauncherScreen.SETTINGS -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onBackToHome = { currentScreen = LauncherScreen.HOME }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}

@Composable
private fun BentoBottomNav(
    currentScreen: LauncherScreen,
    onSelectScreen: (LauncherScreen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BentoNavItem(
            label = "Home",
            isSelected = currentScreen == LauncherScreen.HOME,
            onClick = { onSelectScreen(LauncherScreen.HOME) },
            testTag = "nav_home"
        )
        BentoNavItem(
            label = "Apps",
            isSelected = currentScreen == LauncherScreen.APP_DRAWER,
            onClick = { onSelectScreen(LauncherScreen.APP_DRAWER) },
            testTag = "nav_apps"
        )
        BentoNavItem(
            label = "Data",
            isSelected = currentScreen == LauncherScreen.ANALYTICS,
            onClick = { onSelectScreen(LauncherScreen.ANALYTICS) },
            testTag = "nav_data"
        )
        BentoNavItem(
            label = "Config",
            isSelected = currentScreen == LauncherScreen.SETTINGS,
            onClick = { onSelectScreen(LauncherScreen.SETTINGS) },
            testTag = "nav_config"
        )
    }
}

@Composable
private fun BentoNavItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .alpha(if (isSelected) 1f else 0.35f)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

