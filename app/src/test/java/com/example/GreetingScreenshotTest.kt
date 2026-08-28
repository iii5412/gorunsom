package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.engine.SessionTimerEngine
import com.example.domain.model.AppSettings
import com.example.ui.screens.BreathingSessionScreen
import com.example.ui.screens.ComfortInputScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SafetyOnboardingScreen
import com.example.ui.screens.SessionCompletedScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun home_screen_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    onStartBreathing = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
    }

    @Test
    fun onboarding_screen_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SafetyOnboardingScreen(onAccept = {})
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/onboarding_screen.png")
    }

    @Test
    fun comfort_screen_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ComfortInputScreen(
                    title = "지금 얼마나 편안하신가요?",
                    actionButtonText = "시작하기",
                    selectedScore = 6,
                    onScoreSelected = {},
                    onSubmit = {},
                    onSkip = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/comfort_screen.png")
    }

    @Test
    fun breathing_inhale_screen_screenshot() {
        val snapshot = SessionTimerEngine().calculate(elapsedActiveMs = 37_000L)
        composeTestRule.setContent {
            MyApplicationTheme {
                BreathingSessionScreen(
                    snapshot = snapshot,
                    onStop = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/breathing_inhale_screen.png")
    }

    @Test
    fun breathing_exhale_screen_screenshot() {
        val snapshot = SessionTimerEngine().calculate(elapsedActiveMs = 39_000L)
        composeTestRule.setContent {
            MyApplicationTheme {
                BreathingSessionScreen(
                    snapshot = snapshot,
                    onStop = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/breathing_exhale_screen.png")
    }

    @Test
    fun completed_screen_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SessionCompletedScreen(onHome = {})
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/completed_screen.png")
    }

    @Test
    fun settings_screen_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    appSettings = AppSettings(),
                    totalRecords = 4,
                    onVoiceGuideToggled = {},
                    onHapticsToggled = {},
                    onDeleteAllRecords = {},
                    onOpenPrivacyPolicy = {},
                    onOpenLicenses = {},
                    onOpenAppInfo = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/settings_screen.png")
    }
}
