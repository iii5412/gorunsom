package com.example.domain.model

data class SessionRecord(
    val schemaVersion: Int = 1,
    val sessionId: String,
    val startedAt: String,
    val endedAt: String,
    val activeDurationMs: Long,
    val wallClockDurationMs: Long,
    val inhaleMs: Long = BreathingConstants.INHALE_DURATION_MS,
    val exhaleMs: Long = BreathingConstants.EXHALE_DURATION_MS,
    val holdMs: Long = BreathingConstants.HOLD_DURATION_MS,
    val comfortBefore: Int?,
    val comfortAfter: Int?,
    val completed: Boolean,
    val abortReason: AbortReason?,
    val createdAt: String
)

data class ActiveSessionDraft(
    val sessionId: String,
    val status: DraftStatus,
    val startedAt: String,
    val completedAt: String? = null,
    val activeElapsedMs: Long,
    val comfortBefore: Int?,
    val lastPersistedAt: String
)

data class AppSettings(
    val schemaVersion: Int = 1,
    val onboardingAccepted: Boolean = false,
    val onboardingAcceptedAt: String? = null,
    val noticeVersion: Int = 1,
    val voiceGuideEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val completedSessionCount: Int = 0,
    val lastReviewRequestAt: Long? = null,
    val reviewRequestCount: Int = 0
)
