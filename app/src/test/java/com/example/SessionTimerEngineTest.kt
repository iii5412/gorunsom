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
    fun `test inhale phase progress at 2000ms`() {
        val snapshot = engine.calculate(2000L, 1980L)
        assertEquals(BreathingPhase.INHALE, snapshot.phase)
        assertEquals(0, snapshot.cycleIndex)
        assertEquals(2000L, snapshot.cycleElapsedMs)
        assertEquals(0.5f, snapshot.phaseProgress, 0.01f)
        assertEquals(0.5f, snapshot.circleExpansion, 0.01f)
        assertFalse(snapshot.isCompleted)
    }

    @Test
    fun `test transition to exhale at 4000ms`() {
        val snapshot = engine.calculate(4000L, 3980L)
        assertEquals(BreathingPhase.EXHALE, snapshot.phase)
        assertEquals(0, snapshot.cycleIndex)
        assertEquals(4000L, snapshot.cycleElapsedMs)
        assertEquals(0L, snapshot.phaseElapsedMs)
        assertEquals(BreathingConstants.EXHALE_DURATION_MS, snapshot.phaseDurationMs)
        assertTrue(snapshot.phaseJustStarted)
        assertEquals(VoiceCue.EXHALE_SLOW, snapshot.voiceCue)
        assertEquals(1f, snapshot.circleExpansion, 0.01f)
    }

    @Test
    fun `test exhale progress at 7000ms`() {
        val snapshot = engine.calculate(7000L, 6980L)
        assertEquals(BreathingPhase.EXHALE, snapshot.phase)
        assertEquals(0, snapshot.cycleIndex)
        assertEquals(7000L, snapshot.cycleElapsedMs)
        assertEquals(3000L, snapshot.phaseElapsedMs)
        // 3000ms out of 6000ms exhale = 50%
        assertEquals(0.5f, snapshot.phaseProgress, 0.01f)
        assertEquals(0.5f, snapshot.circleExpansion, 0.01f)
    }

    @Test
    fun `test second cycle starts at 10000ms`() {
        val snapshot = engine.calculate(10000L, 9980L)
        assertEquals(BreathingPhase.INHALE, snapshot.phase)
        assertEquals(1, snapshot.cycleIndex)
        assertEquals(0L, snapshot.cycleElapsedMs)
        assertTrue(snapshot.phaseJustStarted)
        // Cycle 2 (index 1) does not have voice cue
        assertNull(snapshot.voiceCue)
        assertEquals("01:50", snapshot.remainingTimeFormatted)
    }

    @Test
    fun `test 6th cycle (index 5) special reassurance voice cue`() {
        val elapsed = 5 * BreathingConstants.CYCLE_DURATION_MS // 50000ms
        val snapshot = engine.calculate(elapsed, elapsed - 20)
        assertEquals(5, snapshot.cycleIndex)
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
    fun `test total cycles count exactly 12`() {
        val totalMs = BreathingConstants.SESSION_DURATION_MS
        val totalCycles = totalMs / BreathingConstants.CYCLE_DURATION_MS
        assertEquals(12L, totalCycles)
    }
}
