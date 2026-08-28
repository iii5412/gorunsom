package com.example.domain.engine

import com.example.domain.model.BreathingConstants
import com.example.domain.model.BreathingPhase

data class EngineSnapshot(
    val elapsedActiveMs: Long,
    val phase: BreathingPhase,
    val cycleIndex: Int,
    val cycleElapsedMs: Long,
    val phaseElapsedMs: Long,
    val phaseDurationMs: Long,
    val phaseProgress: Float,
    val circleExpansion: Float,
    val remainingTimeMs: Long,
    val remainingTimeFormatted: String,
    val isCompleted: Boolean,
    val phaseJustStarted: Boolean,
    val voiceCue: VoiceCue?
)

enum class VoiceCue(val text: String) {
    INHALE_LIGHT("코로 부드럽게 들이쉬세요."),
    EXHALE_SLOW("입으로 편안히 길게 내쉬세요."),
    REASSURANCE("억지로 깊게 쉬지 않아도 괜찮아요.")
}

class SessionTimerEngine {

    fun calculate(elapsedActiveMs: Long, previousElapsedMs: Long = -1L): EngineSnapshot {
        val boundedElapsed = elapsedActiveMs.coerceIn(0L, BreathingConstants.SESSION_DURATION_MS)
        val isCompleted = boundedElapsed >= BreathingConstants.SESSION_DURATION_MS

        val cycleIndex = (boundedElapsed / BreathingConstants.CYCLE_DURATION_MS)
            .toInt()
            .coerceIn(0, BreathingConstants.TOTAL_CYCLES - 1)
        val cycleElapsedMs = boundedElapsed % BreathingConstants.CYCLE_DURATION_MS

        val phase: BreathingPhase
        val phaseElapsedMs: Long
        val phaseDurationMs: Long
        val phaseProgress: Float
        val circleExpansion: Float

        if (cycleElapsedMs < BreathingConstants.INHALE_DURATION_MS) {
            phase = BreathingPhase.INHALE
            phaseElapsedMs = cycleElapsedMs
            phaseDurationMs = BreathingConstants.INHALE_DURATION_MS
            phaseProgress = (phaseElapsedMs.toFloat() / phaseDurationMs.toFloat()).coerceIn(0f, 1f)
            circleExpansion = phaseProgress
        } else {
            phase = BreathingPhase.EXHALE
            phaseElapsedMs = cycleElapsedMs - BreathingConstants.INHALE_DURATION_MS
            phaseDurationMs = BreathingConstants.EXHALE_DURATION_MS
            phaseProgress = (phaseElapsedMs.toFloat() / phaseDurationMs.toFloat()).coerceIn(0f, 1f)
            circleExpansion = 1f - phaseProgress
        }

        val remainingTimeMs = (BreathingConstants.SESSION_DURATION_MS - boundedElapsed).coerceAtLeast(0L)
        val minutes = remainingTimeMs / 60000L
        val seconds = (remainingTimeMs % 60000L) / 1000L
        val remainingTimeFormatted = String.format("%02d:%02d", minutes, seconds)

        val prevCycleElapsed = if (previousElapsedMs >= 0) previousElapsedMs % BreathingConstants.CYCLE_DURATION_MS else -1L
        val prevPhase = if (prevCycleElapsed in 0L until BreathingConstants.INHALE_DURATION_MS) BreathingPhase.INHALE else BreathingPhase.EXHALE
        val phaseJustStarted = (previousElapsedMs < 0) || (prevPhase != phase) || (previousElapsedMs / BreathingConstants.CYCLE_DURATION_MS != boundedElapsed / BreathingConstants.CYCLE_DURATION_MS)

        // Voice cue determination based on PRD:
        // Cycles 1, 4, 7, 10 (0-indexed: 0, 3, 6, 9): regular inhale/exhale guidance.
        // Cycle 6 (0-indexed: 5): reassurance instead of the standard inhale cue.
        var voiceCue: VoiceCue? = null
        if (phaseJustStarted) {
            if (cycleIndex in listOf(0, 3, 6, 9)) {
                voiceCue = if (phase == BreathingPhase.INHALE) VoiceCue.INHALE_LIGHT else VoiceCue.EXHALE_SLOW
            } else if (cycleIndex == 5 && phase == BreathingPhase.INHALE) {
                voiceCue = VoiceCue.REASSURANCE
            }
        }

        return EngineSnapshot(
            elapsedActiveMs = boundedElapsed,
            phase = phase,
            cycleIndex = cycleIndex,
            cycleElapsedMs = cycleElapsedMs,
            phaseElapsedMs = phaseElapsedMs,
            phaseDurationMs = phaseDurationMs,
            phaseProgress = phaseProgress,
            circleExpansion = circleExpansion,
            remainingTimeMs = remainingTimeMs,
            remainingTimeFormatted = remainingTimeFormatted,
            isCompleted = isCompleted,
            phaseJustStarted = phaseJustStarted,
            voiceCue = voiceCue
        )
    }
}
