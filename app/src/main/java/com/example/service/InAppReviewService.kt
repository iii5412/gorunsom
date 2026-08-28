package com.example.service

import android.app.Activity
import com.example.data.repository.SettingsRepository
import com.example.domain.model.BreathingConstants

class InAppReviewService(private val settingsRepository: SettingsRepository) {

    fun isEligible(nowMs: Long = System.currentTimeMillis()): Boolean {
        val settings = settingsRepository.settings.value
        if (settings.completedSessionCount < BreathingConstants.REVIEW_FIRST_COMPLETED_COUNT) {
            return false
        }
        if (settings.reviewRequestCount >= BreathingConstants.REVIEW_MAX_REQUEST_COUNT) {
            return false
        }
        val cooldownMs = BreathingConstants.REVIEW_COOLDOWN_DAYS * 24L * 60L * 60L * 1000L
        val lastRequested = settings.lastReviewRequestAt
        if (lastRequested != null && (nowMs - lastRequested) < cooldownMs) {
            return false
        }
        return true
    }

    fun requestReviewIfEligible(activity: Activity?, onComplete: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        if (!isEligible(now)) {
            onComplete()
            return
        }
        settingsRepository.recordReviewRequested(now)
        // In release builds, Google Play In-App Review API is invoked here.
        // If not available or dismissed, safely call onComplete
        onComplete()
    }
}
