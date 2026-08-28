package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.GoreunsumDatabase
import com.example.data.repository.SessionRepository
import com.example.data.repository.SettingsRepository
import com.example.service.AudioGuideService
import com.example.service.HapticsService
import com.example.service.InAppReviewService
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.ScreenDestination
import com.example.ui.screens.AppInfoScreen
import com.example.ui.screens.BackgroundResumeDialog
import com.example.ui.screens.BreathingSessionScreen
import com.example.ui.screens.ComfortInputScreen
import com.example.ui.screens.DeleteConfirmDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OpenSourceLicensesScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SafetyHelpScreen
import com.example.ui.screens.SafetyOnboardingScreen
import com.example.ui.screens.SessionCompletedScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StopConfirmDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = GoreunsumDatabase.getInstance(applicationContext)
        val sessionRepository = SessionRepository(database.sessionDao())
        val settingsRepository = SettingsRepository(applicationContext)
        val hapticsService = HapticsService(applicationContext)
        val audioGuideService = AudioGuideService(applicationContext)
        val inAppReviewService = InAppReviewService(settingsRepository)

        MainViewModelFactory(
            sessionRepository = sessionRepository,
            settingsRepository = settingsRepository,
            hapticsService = hapticsService,
            audioGuideService = audioGuideService,
            inAppReviewService = inAppReviewService
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                // Lifecycle observer to handle background pause and resume (FR-027)
                DisposableEffect(this) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> viewModel.onAppPaused()
                            Lifecycle.Event.ON_RESUME -> viewModel.onAppResumed()
                            else -> {}
                        }
                    }
                    lifecycle.addObserver(observer)
                    onDispose {
                        lifecycle.removeObserver(observer)
                    }
                }

                // Show snackbars when message is set
                uiState.snackbarMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        snackbarHostState.showSnackbar(msg)
                        viewModel.onDismissSnackbar()
                    }
                }

                // Handle In-App Review when reaching completion screen
                LaunchedEffect(uiState.currentScreen) {
                    if (uiState.currentScreen == ScreenDestination.SESSION_COMPLETED) {
                        viewModel.requestInAppReviewIfEligible(this@MainActivity)
                    }
                }

                // Global Back Handler
                BackHandler {
                    val handled = viewModel.navigateBack()
                    if (!handled) {
                        finish()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavigation(
                            uiState = uiState,
                            viewModel = viewModel
                        )

                        // Overlays & Dialogs
                        if (uiState.showStopDialog) {
                            StopConfirmDialog(
                                onResume = { viewModel.onResumeFromStopDialog() },
                                onConfirmStop = { viewModel.onConfirmAbortSession() },
                                onDismiss = { viewModel.onResumeFromStopDialog() }
                            )
                        }

                        if (uiState.showBackgroundResumeDialog) {
                            BackgroundResumeDialog(
                                onResume = { viewModel.onResumeFromBackgroundDialog() },
                                onEndSession = { viewModel.onAbortFromBackgroundDialog() },
                                onDismiss = { viewModel.onResumeFromBackgroundDialog() }
                            )
                        }

                        if (uiState.showDeleteConfirmDialog) {
                            DeleteConfirmDialog(
                                onConfirmDelete = { viewModel.onConfirmDeleteAllRecords() },
                                onDismiss = { viewModel.onDismissDeleteDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    Crossfade(targetState = uiState.currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            ScreenDestination.SPLASH -> {
                // Instant load
            }
            ScreenDestination.ONBOARDING -> {
                SafetyOnboardingScreen(
                    onAccept = { viewModel.onAcceptOnboarding() }
                )
            }
            ScreenDestination.HOME -> {
                HomeScreen(
                    onStartBreathing = { viewModel.onStartBreathingFlow() },
                    onOpenHelp = { viewModel.navigateTo(ScreenDestination.SAFETY_HELP) },
                    onOpenSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                )
            }
            ScreenDestination.COMFORT_BEFORE -> {
                ComfortInputScreen(
                    title = "지금 얼마나 편안하신가요?",
                    actionButtonText = "시작하기",
                    selectedScore = uiState.comfortBeforeSelected,
                    onScoreSelected = { score -> viewModel.onComfortBeforeSelected(score) },
                    onSubmit = { score -> viewModel.onStartSession(score) },
                    onSkip = { viewModel.onStartSession(null) }
                )
            }
            ScreenDestination.BREATHING_SESSION -> {
                BreathingSessionScreen(
                    snapshot = uiState.engineSnapshot,
                    onStop = { viewModel.onStopRequested() },
                    onOpenHelp = { viewModel.navigateTo(ScreenDestination.SAFETY_HELP) }
                )
            }
            ScreenDestination.COMFORT_AFTER -> {
                ComfortInputScreen(
                    title = "지금은 얼마나 편안하신가요?",
                    actionButtonText = "완료",
                    selectedScore = uiState.comfortAfterSelected,
                    onScoreSelected = { score -> viewModel.onComfortAfterSelected(score) },
                    onSubmit = { score -> viewModel.onComfortAfterSubmitted(score) },
                    onSkip = { viewModel.onComfortAfterSubmitted(null) }
                )
            }
            ScreenDestination.SESSION_COMPLETED -> {
                SessionCompletedScreen(
                    onHome = { viewModel.navigateTo(ScreenDestination.HOME) }
                )
            }
            ScreenDestination.SAFETY_HELP -> {
                SafetyHelpScreen(
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.SETTINGS -> {
                SettingsScreen(
                    appSettings = uiState.appSettings,
                    totalRecords = uiState.totalRecordCount,
                    onVoiceGuideToggled = { enabled -> viewModel.onVoiceGuideToggled(enabled) },
                    onHapticsToggled = { enabled -> viewModel.onHapticsToggled(enabled) },
                    onDeleteAllRecords = { viewModel.onRequestDeleteAllRecords() },
                    onOpenSafetyNotice = { viewModel.navigateTo(ScreenDestination.SAFETY_HELP) },
                    onOpenPrivacyPolicy = { viewModel.navigateTo(ScreenDestination.PRIVACY_POLICY) },
                    onOpenLicenses = { viewModel.navigateTo(ScreenDestination.OPEN_SOURCE_LICENSES) },
                    onOpenAppInfo = { viewModel.navigateTo(ScreenDestination.APP_INFO) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.APP_INFO -> {
                AppInfoScreen(
                    onOpenSafetyNotice = { viewModel.navigateTo(ScreenDestination.SAFETY_HELP) },
                    onOpenPrivacyPolicy = { viewModel.navigateTo(ScreenDestination.PRIVACY_POLICY) },
                    onOpenLicenses = { viewModel.navigateTo(ScreenDestination.OPEN_SOURCE_LICENSES) },
                    onBack = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                )
            }
            ScreenDestination.PRIVACY_POLICY -> {
                PrivacyPolicyScreen(
                    onBack = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                )
            }
            ScreenDestination.OPEN_SOURCE_LICENSES -> {
                OpenSourceLicensesScreen(
                    onBack = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                )
            }
        }
    }
}
