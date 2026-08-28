package com.example.ui

import android.app.Activity
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SessionRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.engine.EngineSnapshot
import com.example.domain.engine.SessionTimerEngine
import com.example.domain.model.AbortReason
import com.example.domain.model.ActiveSessionDraft
import com.example.domain.model.AppSettings
import com.example.domain.model.BreathingConstants
import com.example.domain.model.DraftStatus
import com.example.domain.model.SessionRecord
import com.example.service.AudioGuideService
import com.example.service.HapticsService
import com.example.service.InAppReviewService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ScreenDestination {
    SPLASH,
    ONBOARDING,
    HOME,
    COMFORT_BEFORE,
    BREATHING_SESSION,
    COMFORT_AFTER,
    SESSION_COMPLETED,
    SETTINGS,
    APP_INFO,
    PRIVACY_POLICY,
    OPEN_SOURCE_LICENSES
}

data class MainUiState(
    val currentScreen: ScreenDestination = ScreenDestination.SPLASH,
    val previousScreen: ScreenDestination? = null,
    val appSettings: AppSettings = AppSettings(),
    val isSessionActive: Boolean = false,
    val engineSnapshot: EngineSnapshot? = null,
    val comfortBeforeSelected: Int? = null,
    val comfortAfterSelected: Int? = null,
    val showStopDialog: Boolean = false,
    val showBackgroundResumeDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val totalRecordCount: Int = 0
)

class MainViewModel(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val hapticsService: HapticsService,
    private val audioGuideService: AudioGuideService,
    private val inAppReviewService: InAppReviewService
) : ViewModel() {

    private val timerEngine = SessionTimerEngine()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var activeSessionId: String? = null
    private var sessionStartWallClock: Long = 0L
    private var sessionStartedAtIso: String = ""
    private var currentActiveElapsedMs: Long = 0L
    private var lastMonotonicTick: Long = 0L
    private var backgroundPausedAtMonotonic: Long = 0L
    private var tickerJob: Job? = null
    private var lastPersistedElapsedMs: Long = 0L

    init {
        // Collect settings
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(appSettings = settings)
            }
        }
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            // Recover any pending draft from previous unexpected app termination
            recoverPendingDraft()

            // Count records
            val totalCount = sessionRepository.countTotalSessions()
            val completedCount = sessionRepository.countCompletedSessions()
            settingsRepository.updateCompletedSessionCount(completedCount)

            _uiState.value = _uiState.value.copy(totalRecordCount = totalCount)

            // Check onboarding
            val isAccepted = settingsRepository.settings.value.onboardingAccepted
            val targetScreen = if (isAccepted) ScreenDestination.HOME else ScreenDestination.ONBOARDING
            _uiState.value = _uiState.value.copy(currentScreen = targetScreen)
        }
    }

    private suspend fun recoverPendingDraft() {
        val draft = sessionRepository.getActiveDraft() ?: return
        val nowIso = currentIsoTimestamp()
        if (draft.status == DraftStatus.completed_awaiting_feedback) {
            val record = SessionRecord(
                sessionId = draft.sessionId,
                startedAt = draft.startedAt,
                endedAt = draft.completedAt ?: nowIso,
                activeDurationMs = BreathingConstants.SESSION_DURATION_MS,
                wallClockDurationMs = BreathingConstants.SESSION_DURATION_MS,
                comfortBefore = draft.comfortBefore,
                comfortAfter = null,
                completed = true,
                abortReason = null,
                createdAt = nowIso
            )
            sessionRepository.saveRecord(record)
        } else {
            val record = SessionRecord(
                sessionId = draft.sessionId,
                startedAt = draft.startedAt,
                endedAt = nowIso,
                activeDurationMs = draft.activeElapsedMs,
                wallClockDurationMs = draft.activeElapsedMs,
                comfortBefore = draft.comfortBefore,
                comfortAfter = null,
                completed = false,
                abortReason = AbortReason.app_terminated,
                createdAt = nowIso
            )
            sessionRepository.saveRecord(record)
        }
        sessionRepository.deleteAllDrafts()
    }

    fun onAcceptOnboarding() {
        val nowIso = currentIsoTimestamp()
        settingsRepository.setOnboardingAccepted(nowIso, BreathingConstants.NOTICE_VERSION)
        navigateTo(ScreenDestination.HOME)
    }

    fun navigateTo(screen: ScreenDestination) {
        val current = _uiState.value.currentScreen
        _uiState.value = _uiState.value.copy(
            previousScreen = current,
            currentScreen = screen
        )
    }

    fun navigateBack(): Boolean {
        val current = _uiState.value.currentScreen
        when (current) {
            ScreenDestination.BREATHING_SESSION -> {
                onStopRequested()
                return true
            }
            ScreenDestination.COMFORT_BEFORE -> {
                navigateTo(ScreenDestination.HOME)
                return true
            }
            ScreenDestination.COMFORT_AFTER -> {
                // If on S07, skipping comfortAfter saves and goes to S08
                onComfortAfterSubmitted(null)
                return true
            }
            ScreenDestination.SESSION_COMPLETED -> {
                navigateTo(ScreenDestination.HOME)
                return true
            }
            ScreenDestination.SETTINGS -> {
                val dest = if (_uiState.value.isSessionActive) ScreenDestination.BREATHING_SESSION else ScreenDestination.HOME
                navigateTo(dest)
                return true
            }
            ScreenDestination.APP_INFO,
            ScreenDestination.PRIVACY_POLICY,
            ScreenDestination.OPEN_SOURCE_LICENSES -> {
                navigateTo(ScreenDestination.SETTINGS)
                return true
            }
            else -> return false
        }
    }

    fun onStartBreathingFlow() {
        _uiState.value = _uiState.value.copy(
            comfortBeforeSelected = null,
            comfortAfterSelected = null
        )
        navigateTo(ScreenDestination.COMFORT_BEFORE)
    }

    fun onComfortBeforeSelected(score: Int) {
        _uiState.value = _uiState.value.copy(comfortBeforeSelected = score)
    }

    fun onStartSession(comfortBefore: Int?) {
        activeSessionId = UUID.randomUUID().toString()
        sessionStartWallClock = System.currentTimeMillis()
        sessionStartedAtIso = currentIsoTimestamp()
        currentActiveElapsedMs = 0L
        lastMonotonicTick = SystemClock.elapsedRealtime()
        lastPersistedElapsedMs = 0L

        val initialSnapshot = timerEngine.calculate(0L, -1L)
        _uiState.value = _uiState.value.copy(
            isSessionActive = true,
            engineSnapshot = initialSnapshot,
            comfortBeforeSelected = comfortBefore,
            comfortAfterSelected = null,
            showStopDialog = false,
            showBackgroundResumeDialog = false
        )

        // Save initial draft
        viewModelScope.launch {
            sessionRepository.saveDraft(
                ActiveSessionDraft(
                    sessionId = activeSessionId!!,
                    status = DraftStatus.active,
                    startedAt = sessionStartedAtIso,
                    activeElapsedMs = 0L,
                    comfortBefore = comfortBefore,
                    lastPersistedAt = currentIsoTimestamp()
                )
            )
        }

        navigateTo(ScreenDestination.BREATHING_SESSION)
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        lastMonotonicTick = SystemClock.elapsedRealtime()

        tickerJob = viewModelScope.launch {
            var previousElapsed = currentActiveElapsedMs
            while (currentActiveElapsedMs < BreathingConstants.SESSION_DURATION_MS) {
                delay(20) // ~50fps ticker for smooth visual progress & accurate monotonic counting
                val nowMonotonic = SystemClock.elapsedRealtime()
                val delta = (nowMonotonic - lastMonotonicTick).coerceAtLeast(0L)
                lastMonotonicTick = nowMonotonic
                currentActiveElapsedMs = (currentActiveElapsedMs + delta).coerceAtMost(BreathingConstants.SESSION_DURATION_MS)

                val snapshot = timerEngine.calculate(currentActiveElapsedMs, previousElapsed)
                _uiState.value = _uiState.value.copy(engineSnapshot = snapshot)

                // Trigger Haptics and Audio cue on phase change
                if (snapshot.phaseJustStarted) {
                    val settings = _uiState.value.appSettings
                    if (snapshot.phase == com.example.domain.model.BreathingPhase.INHALE) {
                        hapticsService.vibrateInhaleStart(settings.hapticsEnabled)
                    } else {
                        hapticsService.vibrateExhaleStart(settings.hapticsEnabled)
                    }
                    snapshot.voiceCue?.let { cue ->
                        audioGuideService.playCue(cue, settings.voiceGuideEnabled)
                    }
                }

                // Persist draft periodically every 5 seconds
                if (currentActiveElapsedMs - lastPersistedElapsedMs >= BreathingConstants.ACTIVE_DRAFT_PERSIST_INTERVAL_MS) {
                    lastPersistedElapsedMs = currentActiveElapsedMs
                    activeSessionId?.let { id ->
                        sessionRepository.saveDraft(
                            ActiveSessionDraft(
                                sessionId = id,
                                status = DraftStatus.active,
                                startedAt = sessionStartedAtIso,
                                activeElapsedMs = currentActiveElapsedMs,
                                comfortBefore = _uiState.value.comfortBeforeSelected,
                                lastPersistedAt = currentIsoTimestamp()
                            )
                        )
                    }
                }

                previousElapsed = currentActiveElapsedMs
            }

            // Completed 120 seconds!
            onSession120Reached()
        }
    }

    private fun onSession120Reached() {
        tickerJob?.cancel()
        tickerJob = null
        audioGuideService.stop()

        val completedIso = currentIsoTimestamp()
        viewModelScope.launch {
            activeSessionId?.let { id ->
                sessionRepository.saveDraft(
                    ActiveSessionDraft(
                        sessionId = id,
                        status = DraftStatus.completed_awaiting_feedback,
                        startedAt = sessionStartedAtIso,
                        completedAt = completedIso,
                        activeElapsedMs = BreathingConstants.SESSION_DURATION_MS,
                        comfortBefore = _uiState.value.comfortBeforeSelected,
                        lastPersistedAt = completedIso
                    )
                )
            }
        }

        navigateTo(ScreenDestination.COMFORT_AFTER)
    }

    fun onComfortAfterSelected(score: Int) {
        _uiState.value = _uiState.value.copy(comfortAfterSelected = score)
    }

    fun onComfortAfterSubmitted(score: Int?) {
        viewModelScope.launch {
            val endIso = currentIsoTimestamp()
            val wallClockDuration = System.currentTimeMillis() - sessionStartWallClock
            val id = activeSessionId ?: UUID.randomUUID().toString()

            val record = SessionRecord(
                sessionId = id,
                startedAt = sessionStartedAtIso,
                endedAt = endIso,
                activeDurationMs = BreathingConstants.SESSION_DURATION_MS,
                wallClockDurationMs = wallClockDuration,
                comfortBefore = _uiState.value.comfortBeforeSelected,
                comfortAfter = score,
                completed = true,
                abortReason = null,
                createdAt = endIso
            )

            sessionRepository.saveRecord(record)
            sessionRepository.deleteDraft(id)

            val newCount = sessionRepository.countCompletedSessions()
            val totalCount = sessionRepository.countTotalSessions()
            settingsRepository.updateCompletedSessionCount(newCount)

            _uiState.value = _uiState.value.copy(
                isSessionActive = false,
                totalRecordCount = totalCount
            )

            navigateTo(ScreenDestination.SESSION_COMPLETED)
        }
    }

    fun onStopRequested() {
        tickerJob?.cancel()
        tickerJob = null
        audioGuideService.stop()
        _uiState.value = _uiState.value.copy(showStopDialog = true)
    }

    fun onResumeFromStopDialog() {
        _uiState.value = _uiState.value.copy(showStopDialog = false)
        startTicker()
    }

    fun onConfirmAbortSession() {
        _uiState.value = _uiState.value.copy(
            showStopDialog = false,
            isSessionActive = false
        )
        tickerJob?.cancel()
        tickerJob = null
        audioGuideService.stop()

        viewModelScope.launch {
            val endIso = currentIsoTimestamp()
            val wallClockDuration = System.currentTimeMillis() - sessionStartWallClock
            val id = activeSessionId ?: UUID.randomUUID().toString()

            val record = SessionRecord(
                sessionId = id,
                startedAt = sessionStartedAtIso,
                endedAt = endIso,
                activeDurationMs = currentActiveElapsedMs,
                wallClockDurationMs = wallClockDuration,
                comfortBefore = _uiState.value.comfortBeforeSelected,
                comfortAfter = null,
                completed = false,
                abortReason = AbortReason.user_stop,
                createdAt = endIso
            )

            sessionRepository.saveRecord(record)
            sessionRepository.deleteDraft(id)

            val totalCount = sessionRepository.countTotalSessions()
            _uiState.value = _uiState.value.copy(totalRecordCount = totalCount)

            navigateTo(ScreenDestination.HOME)
        }
    }

    fun onAppPaused() {
        if (_uiState.value.isSessionActive && _uiState.value.currentScreen == ScreenDestination.BREATHING_SESSION) {
            tickerJob?.cancel()
            tickerJob = null
            audioGuideService.stop()
            backgroundPausedAtMonotonic = SystemClock.elapsedRealtime()

            // Save draft
            activeSessionId?.let { id ->
                viewModelScope.launch {
                    sessionRepository.saveDraft(
                        ActiveSessionDraft(
                            sessionId = id,
                            status = DraftStatus.active,
                            startedAt = sessionStartedAtIso,
                            activeElapsedMs = currentActiveElapsedMs,
                            comfortBefore = _uiState.value.comfortBeforeSelected,
                            lastPersistedAt = currentIsoTimestamp()
                        )
                    )
                }
            }
        }
    }

    fun onAppResumed() {
        if (_uiState.value.isSessionActive && _uiState.value.currentScreen == ScreenDestination.BREATHING_SESSION && backgroundPausedAtMonotonic > 0L) {
            val awayDurationMs = SystemClock.elapsedRealtime() - backgroundPausedAtMonotonic
            backgroundPausedAtMonotonic = 0L

            if (awayDurationMs <= BreathingConstants.BACKGROUND_RESUME_LIMIT_MS) {
                // Show S06 Background Resume Dialog
                _uiState.value = _uiState.value.copy(showBackgroundResumeDialog = true)
            } else {
                // Background timeout: auto abort, save record, show message, go to Home
                _uiState.value = _uiState.value.copy(
                    isSessionActive = false,
                    showBackgroundResumeDialog = false,
                    snackbarMessage = "앱을 오래 떠나 있어 세션이 종료되었어요."
                )
                viewModelScope.launch {
                    val endIso = currentIsoTimestamp()
                    val id = activeSessionId ?: UUID.randomUUID().toString()
                    val record = SessionRecord(
                        sessionId = id,
                        startedAt = sessionStartedAtIso,
                        endedAt = endIso,
                        activeDurationMs = currentActiveElapsedMs,
                        wallClockDurationMs = currentActiveElapsedMs + awayDurationMs,
                        comfortBefore = _uiState.value.comfortBeforeSelected,
                        comfortAfter = null,
                        completed = false,
                        abortReason = AbortReason.background_timeout,
                        createdAt = endIso
                    )
                    sessionRepository.saveRecord(record)
                    sessionRepository.deleteDraft(id)

                    val totalCount = sessionRepository.countTotalSessions()
                    _uiState.value = _uiState.value.copy(totalRecordCount = totalCount)

                    navigateTo(ScreenDestination.HOME)
                }
            }
        }
    }

    fun onResumeFromBackgroundDialog() {
        _uiState.value = _uiState.value.copy(showBackgroundResumeDialog = false)
        startTicker()
    }

    fun onAbortFromBackgroundDialog() {
        _uiState.value = _uiState.value.copy(
            showBackgroundResumeDialog = false,
            isSessionActive = false
        )
        viewModelScope.launch {
            val endIso = currentIsoTimestamp()
            val id = activeSessionId ?: UUID.randomUUID().toString()
            val record = SessionRecord(
                sessionId = id,
                startedAt = sessionStartedAtIso,
                endedAt = endIso,
                activeDurationMs = currentActiveElapsedMs,
                wallClockDurationMs = currentActiveElapsedMs,
                comfortBefore = _uiState.value.comfortBeforeSelected,
                comfortAfter = null,
                completed = false,
                abortReason = AbortReason.user_stop,
                createdAt = endIso
            )
            sessionRepository.saveRecord(record)
            sessionRepository.deleteDraft(id)

            val totalCount = sessionRepository.countTotalSessions()
            _uiState.value = _uiState.value.copy(totalRecordCount = totalCount)

            navigateTo(ScreenDestination.HOME)
        }
    }

    fun onVoiceGuideToggled(enabled: Boolean) {
        settingsRepository.setVoiceGuideEnabled(enabled)
    }

    fun onHapticsToggled(enabled: Boolean) {
        settingsRepository.setHapticsEnabled(enabled)
    }

    fun onRequestDeleteAllRecords() {
        if (_uiState.value.totalRecordCount > 0) {
            _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = true)
        } else {
            _uiState.value = _uiState.value.copy(snackbarMessage = "삭제할 기록이 없습니다.")
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = false)
    }

    fun onConfirmDeleteAllRecords() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = false)
        viewModelScope.launch {
            sessionRepository.deleteAllRecords()
            settingsRepository.updateCompletedSessionCount(0)
            _uiState.value = _uiState.value.copy(
                totalRecordCount = 0,
                snackbarMessage = "모든 기록을 삭제했어요."
            )
        }
    }

    fun onDismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun requestInAppReviewIfEligible(activity: Activity?) {
        inAppReviewService.requestReviewIfEligible(activity)
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        audioGuideService.release()
    }

    private fun currentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        return sdf.format(Date())
    }
}

class MainViewModelFactory(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val hapticsService: HapticsService,
    private val audioGuideService: AudioGuideService,
    private val inAppReviewService: InAppReviewService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            sessionRepository,
            settingsRepository,
            hapticsService,
            audioGuideService,
            inAppReviewService
        ) as T
    }
}
