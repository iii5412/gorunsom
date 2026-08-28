package com.example.domain.model

object BreathingConstants {
    const val SESSION_DURATION_MS: Long = 120000L
    const val INHALE_DURATION_MS: Long = 4000L
    const val EXHALE_DURATION_MS: Long = 6000L
    const val HOLD_DURATION_MS: Long = 0L
    const val CYCLE_DURATION_MS: Long = 10000L
    const val TOTAL_CYCLES: Int = 12

    const val BACKGROUND_RESUME_LIMIT_MS: Long = 30000L
    const val ACTIVE_DRAFT_PERSIST_INTERVAL_MS: Long = 5000L

    const val REVIEW_FIRST_COMPLETED_COUNT: Int = 3
    const val REVIEW_COOLDOWN_DAYS: Long = 90L
    const val REVIEW_MAX_REQUEST_COUNT: Int = 3

    const val COMFORT_MIN: Int = 0
    const val COMFORT_MAX: Int = 10

    const val AD_TO_ACTION_MIN_SPACING_DP: Int = 32
    const val MIN_TOUCH_TARGET_DP: Int = 48

    const val NOTICE_VERSION: Int = 2
}

enum class BreathingPhase {
    INHALE,
    EXHALE
}

enum class AbortReason {
    user_stop,
    background_timeout,
    app_terminated,
    unexpected_error
}

enum class DraftStatus {
    active,
    completed_awaiting_feedback
}
