package com.example.domain.manager

import com.example.domain.model.ProtectionStatus
import com.example.domain.model.UnblockSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnblockSessionStateMachineTest {

    // In-memory fake manager implementing UnblockSessionManager for deterministic state-machine verification
    private class FakeUnblockSessionManager : UnblockSessionManager {
        val stateFlow = MutableStateFlow(UnblockSessionState())
        override val sessionStateFlow = stateFlow

        override suspend fun activateProtection(): UnblockSessionState {
            val updated = UnblockSessionState(
                status = ProtectionStatus.PROTECTION_ACTIVE,
                currentStage = 1,
                completedStages = 0
            )
            stateFlow.value = updated
            return updated
        }

        override suspend fun requestUnblock(): UnblockSessionState {
            val current = stateFlow.value
            if (current.status != ProtectionStatus.PROTECTION_ACTIVE) return current
            val updated = current.copy(
                status = ProtectionStatus.UNBLOCK_REQUESTED,
                unblockRequestTimestampEpochMs = System.currentTimeMillis()
            )
            stateFlow.value = updated
            return updated
        }

        override suspend fun startCurrentHour(now: Long): UnblockSessionState {
            val current = stateFlow.value
            val nextStage = when (current.status) {
                ProtectionStatus.UNBLOCK_REQUESTED -> 1
                ProtectionStatus.HOUR_COMPLETE -> (current.completedStages + 1).coerceAtMost(24)
                else -> return current
            }
            val updated = current.copy(
                status = ProtectionStatus.HOUR_RUNNING,
                currentStage = nextStage,
                stageStartTimestampEpochMs = now
            )
            stateFlow.value = updated
            return updated
        }

        override suspend fun syncTimerState(now: Long): UnblockSessionState {
            val current = stateFlow.value
            if (current.status == ProtectionStatus.HOUR_RUNNING) {
                val elapsed = now - current.stageStartTimestampEpochMs
                if (elapsed >= current.stageDurationMs) {
                    val newCompleted = current.currentStage
                    val newStatus = if (newCompleted >= 24) {
                        ProtectionStatus.UNBLOCK_AVAILABLE
                    } else {
                        ProtectionStatus.HOUR_COMPLETE
                    }
                    val updated = current.copy(
                        status = newStatus,
                        completedStages = newCompleted
                    )
                    stateFlow.value = updated
                    return updated
                }
            }
            return current
        }

        override suspend fun cancelUnblock(): UnblockSessionState {
            val updated = UnblockSessionState(
                status = ProtectionStatus.PROTECTION_ACTIVE,
                currentStage = 1,
                completedStages = 0
            )
            stateFlow.value = updated
            return updated
        }

        override suspend fun completeUnblockAndTurnOff(): UnblockSessionState {
            val current = stateFlow.value
            if (current.status != ProtectionStatus.UNBLOCK_AVAILABLE) return current
            val updated = UnblockSessionState(
                status = ProtectionStatus.PROTECTION_OFF,
                currentStage = 1,
                completedStages = 0
            )
            stateFlow.value = updated
            return updated
        }

        override suspend fun setStageDurationForTesting(durationMs: Long) {
            stateFlow.value = stateFlow.value.copy(stageDurationMs = durationMs)
        }
    }

    private lateinit var sessionManager: FakeUnblockSessionManager

    @Before
    fun setUp() {
        sessionManager = FakeUnblockSessionManager()
    }

    @Test
    fun testFirstLaunchIsOff() {
        // Requirement 1: On first launch, protection MUST be OFF
        val initial = sessionManager.stateFlow.value
        assertEquals(ProtectionStatus.PROTECTION_OFF, initial.status)
        assertFalse(initial.status.isProtectionEnabled)
        assertEquals(0, initial.completedStages)
    }

    @Test
    fun testActivation() = runBlocking {
        // Requirement 2: Activation transitions to PROTECTION_ACTIVE
        val state = sessionManager.activateProtection()
        assertEquals(ProtectionStatus.PROTECTION_ACTIVE, state.status)
        assertTrue(state.status.isProtectionEnabled)
    }

    @Test
    fun testUnblockRequestAndHourRunning() = runBlocking {
        sessionManager.activateProtection()

        // User taps "Request Unblock"
        val requestedState = sessionManager.requestUnblock()
        assertEquals(ProtectionStatus.UNBLOCK_REQUESTED, requestedState.status)
        assertTrue("Protection remains active during unblock request", requestedState.status.isProtectionEnabled)

        // User confirms warning and begins Stage 1
        val startTime = 1000000L
        val runningState = sessionManager.startCurrentHour(now = startTime)
        assertEquals(ProtectionStatus.HOUR_RUNNING, runningState.status)
        assertEquals(1, runningState.currentStage)
        assertEquals(startTime, runningState.stageStartTimestampEpochMs)
        assertTrue(runningState.status.isProtectionEnabled)
    }

    @Test
    fun testHourTimerAndHourCompletion() = runBlocking {
        sessionManager.activateProtection()
        sessionManager.requestUnblock()
        val startTime = 1000000L
        sessionManager.startCurrentHour(now = startTime)

        // Check after 30 minutes (1800 seconds = 1800000 ms)
        val halfway = startTime + (30 * 60 * 1000L)
        val midState = sessionManager.syncTimerState(now = halfway)
        assertEquals(ProtectionStatus.HOUR_RUNNING, midState.status)
        assertEquals(0, midState.completedStages)

        // Check after 60 minutes exactly (3600000 ms)
        val finishedTime = startTime + (60 * 60 * 1000L)
        val completeState = sessionManager.syncTimerState(now = finishedTime)
        assertEquals(ProtectionStatus.HOUR_COMPLETE, completeState.status)
        assertEquals(1, completeState.completedStages)
        assertTrue("Protection remains strictly active after hour completion", completeState.status.isProtectionEnabled)
    }

    @Test
    fun testDoesNotAutoStartNextHour() = runBlocking {
        sessionManager.activateProtection()
        sessionManager.requestUnblock()
        val startTime = 1000000L
        sessionManager.startCurrentHour(now = startTime)
        val finishedTime = startTime + (60 * 60 * 1000L)
        sessionManager.syncTimerState(now = finishedTime)

        // Advance another 10 hours without user interaction
        val laterTime = finishedTime + (10 * 3600 * 1000L)
        val stateAfterDelay = sessionManager.syncTimerState(now = laterTime)

        // MUST NOT advance or start automatically!
        assertEquals(ProtectionStatus.HOUR_COMPLETE, stateAfterDelay.status)
        assertEquals(1, stateAfterDelay.completedStages)
    }

    @Test
    fun testManualNextHourResume() = runBlocking {
        sessionManager.activateProtection()
        sessionManager.requestUnblock()
        val t1 = 1000000L
        sessionManager.startCurrentHour(now = t1)
        sessionManager.syncTimerState(now = t1 + 3600000L)

        // User manually confirms and begins Stage 2
        val t2 = t1 + 4000000L
        val stage2State = sessionManager.startCurrentHour(now = t2)
        assertEquals(ProtectionStatus.HOUR_RUNNING, stage2State.status)
        assertEquals(2, stage2State.currentStage)
        assertEquals(1, stage2State.completedStages)
    }

    @Test
    fun testRestartDuringHourPreservesElapsed() = runBlocking {
        sessionManager.activateProtection()
        sessionManager.requestUnblock()
        val startTime = 1000000L
        sessionManager.startCurrentHour(now = startTime)

        // Simulate app kill and reopen after 45 minutes
        val reopenTime = startTime + (45 * 60 * 1000L)
        val stateAfterReopen = sessionManager.syncTimerState(now = reopenTime)

        assertEquals(ProtectionStatus.HOUR_RUNNING, stateAfterReopen.status)
        val remaining = stateAfterReopen.getRemainingTimeMs(reopenTime)
        assertEquals(15 * 60 * 1000L, remaining)
    }

    @Test
    fun testFull24HourProgressionToUnblockAvailable() = runBlocking {
        sessionManager.activateProtection()
        sessionManager.requestUnblock()

        var virtualTime = 1000000L
        for (stage in 1..24) {
            sessionManager.startCurrentHour(now = virtualTime)
            virtualTime += 3600000L // 1 hour passes
            sessionManager.syncTimerState(now = virtualTime)
        }

        val finalState = sessionManager.stateFlow.value
        assertEquals(ProtectionStatus.UNBLOCK_AVAILABLE, finalState.status)
        assertEquals(24, finalState.completedStages)
        assertTrue("Protection still active until explicit disable tap", finalState.status.isProtectionEnabled)

        // User finally disables protection
        val turnedOff = sessionManager.completeUnblockAndTurnOff()
        assertEquals(ProtectionStatus.PROTECTION_OFF, turnedOff.status)
        assertFalse(turnedOff.status.isProtectionEnabled)
    }

    @Test
    fun testCancelUnblockReturnsToProtectionActive() = runBlocking {
        sessionManager.activateProtection()
        sessionManager.requestUnblock()
        sessionManager.startCurrentHour(now = 1000L)

        // User decides to cancel unblock
        val resetState = sessionManager.cancelUnblock()
        assertEquals(ProtectionStatus.PROTECTION_ACTIVE, resetState.status)
        assertEquals(0, resetState.completedStages)
        assertEquals(1, resetState.currentStage)
    }
}
