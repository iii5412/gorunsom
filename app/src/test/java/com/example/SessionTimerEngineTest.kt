package com.example

import com.example.domain.engine.SessionTimerEngine
import com.example.domain.engine.VoiceCue
import com.example.domain.model.BreathingConstants
import com.example.domain.model.BreathingPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTimerEngineTest {

    private val engine = SessionTimerEngine()

    @Test
    fun `test initial state at 0ms`() {
        val snapshot = engine.calculate(0L, -1L)
        assertEquals(BreathingPhase.INHALE, snapshot.phase)
        assertEquals(0, snapshot.cycleIndex)
        assertEquals(0L, snapshot.cycleElapsedMs)
        assertEquals(120000L, snapshot.remainingTimeMs)
        assertEquals("02:00", snapshot.remainingTimeFormatted)
        assertFalse(snapshot.isCompleted)
        assertTrue(snapshot.phaseJustStarted)
        assertEquals(VoiceCue.INHALE_LIGHT, snapshot.voiceCue)
        assertEquals(0f, snapshot.circleExpansion, 0.001f)
    }

    @Test
    fun `test inhale phase progress at 1000ms`() {
        val snapshot = engine.calculate(1000L, 980L)
        assertEquals(BreathingPhase.INHALE, snapshot.phase)
        assertEquals(0, snapshot.cycleIndex)
        assertEquals(1000L, snapshot.cycleElapsedMs)
        assertEquals(0.5f, snapshot.phaseProgress, 0.01f)
        assertEquals(0.5f, snapshot.circleExpansion, 0.01f)
        assertFalse(snapshot.isCompleted)
    }

    @Test
    fun `test transition to exhale at 2000ms`() {
        val snapshot = engine.calculate(2000L, 1980L)
        assertEquals(BreathingPhase.EXHALE, snapshot.phase)
        assertEquals(0, snapshot.cycleIndex)
        assertEquals(2000L, snapshot.cycleElapsedMs)
        assertEquals(0L, snapshot.phaseElapsedMs)
        assertEquals(BreathingConstants.EXHALE_DURATION_MS, snapshot.phaseDurationMs)
        assertTrue(snapshot.phaseJustStarted)
        assertEquals(VoiceCue.EXHALE_SLOW, snapshot.voiceCue)
        assertEquals(1f, snapshot.circleExpansion, 0.01f)
    }

    @Test
    fun `test exhale progress at 4000ms`() {
        val snapshot = engine.calculate(4000L, 3980L)
        assertEquals(BreathingPhase.EXHALE, snapshot.phase)
        assertEquals(0, snapshot.cycleIndex)
        assertEquals(4000L, snapshot.cycleElapsedMs)
        assertEquals(2000L, snapshot.phaseElapsedMs)
        // 2000ms out of 4000ms exhale = 50%
        assertEquals(0.5f, snapshot.phaseProgress, 0.01f)
        assertEquals(0.5f, snapshot.circleExpansion, 0.01f)
    }

    @Test
    fun `test second cycle starts at 6000ms`() {
        val snapshot = engine.calculate(6000L, 5980L)
        assertEquals(BreathingPhase.INHALE, snapshot.phase)
        assertEquals(1, snapshot.cycleIndex)
        assertEquals(0L, snapshot.cycleElapsedMs)
        assertTrue(snapshot.phaseJustStarted)
        // Cycle 2 (index 1) does not have voice cue
        assertNull(snapshot.voiceCue)
        assertEquals("01:54", snapshot.remainingTimeFormatted)
    }

    @Test
    fun `test 10th cycle (index 9) special reassurance voice cue`() {
        val elapsed = 9 * BreathingConstants.CYCLE_DURATION_MS // 54000ms
        val snapshot = engine.calculate(elapsed, elapsed - 20)
        assertEquals(9, snapshot.cycleIndex)
        assertEquals(BreathingPhase.INHALE, snapshot.phase)
        assertTrue(snapshot.phaseJustStarted)
        assertEquals(VoiceCue.REASSURANCE, snapshot.voiceCue)
    }

    @Test
    fun `test completion at 120000ms`() {
        val snapshot = engine.calculate(120000L, 119980L)
        assertTrue(snapshot.isCompleted)
        assertEquals(0L, snapshot.remainingTimeMs)
        assertEquals("00:00", snapshot.remainingTimeFormatted)
    }

    @Test
    fun `test total cycles count exactly 20`() {
        val totalMs = BreathingConstants.SESSION_DURATION_MS
        val totalCycles = totalMs / BreathingConstants.CYCLE_DURATION_MS
        assertEquals(20L, totalCycles)
    }
}
