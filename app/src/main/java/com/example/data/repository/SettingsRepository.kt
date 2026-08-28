package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("goreunsum_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            schemaVersion = prefs.getInt(KEY_SCHEMA_VERSION, 1),
            onboardingAccepted = prefs.getBoolean(KEY_ONBOARDING_ACCEPTED, false),
            onboardingAcceptedAt = prefs.getString(KEY_ONBOARDING_ACCEPTED_AT, null),
            noticeVersion = prefs.getInt(KEY_NOTICE_VERSION, 1),
            voiceGuideEnabled = prefs.getBoolean(KEY_VOICE_GUIDE_ENABLED, false),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
            completedSessionCount = prefs.getInt(KEY_COMPLETED_SESSION_COUNT, 0),
            lastReviewRequestAt = if (prefs.contains(KEY_LAST_REVIEW_REQUEST_AT)) prefs.getLong(KEY_LAST_REVIEW_REQUEST_AT, 0L) else null,
            reviewRequestCount = prefs.getInt(KEY_REVIEW_REQUEST_COUNT, 0)
        )
    }

    fun setOnboardingAccepted(acceptedAt: String, noticeVersion: Int = 1) {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_ACCEPTED, true)
            .putString(KEY_ONBOARDING_ACCEPTED_AT, acceptedAt)
            .putInt(KEY_NOTICE_VERSION, noticeVersion)
            .apply()
        _settings.value = _settings.value.copy(
            onboardingAccepted = true,
            onboardingAcceptedAt = acceptedAt,
            noticeVersion = noticeVersion
        )
    }

    fun setVoiceGuideEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_GUIDE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(voiceGuideEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(hapticsEnabled = enabled)
    }

    fun updateCompletedSessionCount(count: Int) {
        prefs.edit().putInt(KEY_COMPLETED_SESSION_COUNT, count).apply()
        _settings.value = _settings.value.copy(completedSessionCount = count)
    }

    fun recordReviewRequested(nowMs: Long) {
        val newCount = _settings.value.reviewRequestCount + 1
        prefs.edit()
            .putLong(KEY_LAST_REVIEW_REQUEST_AT, nowMs)
            .putInt(KEY_REVIEW_REQUEST_COUNT, newCount)
            .apply()
        _settings.value = _settings.value.copy(
            lastReviewRequestAt = nowMs,
            reviewRequestCount = newCount
        )
    }

    companion object {
        private const val KEY_SCHEMA_VERSION = "schema_version"
        private const val KEY_ONBOARDING_ACCEPTED = "onboarding_accepted"
        private const val KEY_ONBOARDING_ACCEPTED_AT = "onboarding_accepted_at"
        private const val KEY_NOTICE_VERSION = "notice_version"
        private const val KEY_VOICE_GUIDE_ENABLED = "voice_guide_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_COMPLETED_SESSION_COUNT = "completed_session_count"
        private const val KEY_LAST_REVIEW_REQUEST_AT = "last_review_request_at"
        private const val KEY_REVIEW_REQUEST_COUNT = "review_request_count"
    }
}
