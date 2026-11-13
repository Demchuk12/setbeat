package com.vadem.setbeat.workout

import com.vadem.setbeat.domain.Phase
import com.vadem.setbeat.domain.WorkoutEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutControllerTest {

    private val store = FakeStore()
    private val tracker = FakeTracker()
    private val restTimer = FakeRestTimer()
    private val archive = FakeArchive()
    private val clock = FakeClock()

    private fun TestScope.controller(plans: PlanSource = PlanSource { testPlan }): WorkoutController {
        var ids = 0
        return WorkoutController(
            scope = backgroundScope,
            store = store,
            plans = plans,
            tracker = tracker,
            restTimer = restTimer,
            archive = archive,
            clock = clock,
            logger = SilentLogger,
            newId = { "s${++ids}" },
        ).also { runCurrent() }
    }

    private fun TestScope.step(seconds: Long, action: () -> Unit) {
        clock.time += seconds * 1000
        action()
        runCurrent()
    }

    @Test
    fun `loads the plan and starts a tracked session`() = runTest {
        val controller = controller()
        assertEquals(PlanStatus.Ready(testPlan), controller.plan.value)

        step(0) { controller.startSession() }

        assertEquals(Phase.IDLE, controller.state.value?.phase)
        assertTrue(store.events.getValue("s1").single() is WorkoutEvent.SessionStarted)
        assertEquals(listOf("start:s1"), tracker.calls)
    }

    @Test
    fun `persists every accepted command`() = runTest {
        val controller = controller()
        step(0) { controller.startSession() }
        step(5) { controller.startSet() }
        step(30) { controller.endSet() }
        step(10) { controller.logSet(102.5, 4, 8.0) }

        assertEquals(
            listOf("SessionStarted", "SetStarted", "SetEnded", "SetLogged"),
            store.events.getValue("s1").map { it::class.simpleName },
        )
        assertEquals(102.5, controller.state.value?.results?.single()?.weight)
    }

    @Test
    fun `ignores commands the engine rejects`() = runTest {
        val controller = controller()
        step(0) { controller.startSession() }
        step(1) { controller.endSet() }

        assertEquals(1, store.events.getValue("s1").size)
        assertEquals(Phase.IDLE, controller.state.value?.phase)
    }

    @Test
    fun `schedules the rest timer during recovery`() = runTest {
        val controller = controller()
        step(0) { controller.startSession() }
        step(5) { controller.startSet() }
        step(30) { controller.endSet() }

        assertEquals(120_000L, restTimer.scheduled)

        step(60) { controller.startSet() }
        assertNull(restTimer.scheduled)
    }

    @Test
    fun `finishing ends the session, stops tracking and archives once tracking ends`() = runTest {
        val controller = controller()
        step(0) { controller.startSession() }
        step(10) { controller.finish() }

        assertEquals(Phase.COMPLETED, controller.state.value?.phase)
        assertEquals(clock.time, store.ended["s1"])
        assertEquals(listOf("start:s1", "stop"), tracker.calls)
        assertTrue(archive.archived.isEmpty())

        controller.onTrackingEnded("ended")
        assertEquals(listOf("s1"), archive.archived)
    }

    @Test
    fun `dismissing the summary before tracking ends still archives the session`() = runTest {
        val controller = controller()
        step(0) { controller.startSession() }
        step(10) { controller.finish() }
        step(1) { controller.dismiss() }

        controller.onTrackingEnded("ended")

        assertNull(controller.state.value)
        assertEquals(listOf("s1"), archive.archived)
    }

    @Test
    fun `losing the sensor mid workout interrupts the session`() = runTest {
        val controller = controller()
        step(0) { controller.startSession() }
        step(5) { controller.startSet() }

        clock.time += 1_000
        controller.onTrackingEnded("permission lost")

        assertEquals(Phase.INTERRUPTED, controller.state.value?.phase)
        assertTrue(store.events.getValue("s1").last() is WorkoutEvent.Interrupted)
        assertEquals(listOf("s1"), archive.archived)
    }

    @Test
    fun `restores an open session after a restart and resumes tracking`() = runTest {
        store.createSession("old", 0)
        store.append("old", WorkoutEvent.SessionStarted(testPlan, 0))
        store.append("old", WorkoutEvent.SetStarted(1_000))
        store.append("old", WorkoutEvent.SetEnded(31_000))

        val controller = controller()

        assertEquals(Phase.RECOVERY, controller.state.value?.phase)
        assertEquals(listOf("resume:old"), tracker.calls)
    }

    @Test
    fun `archives a session that finished right before a crash`() = runTest {
        store.createSession("old", 0)
        store.append("old", WorkoutEvent.SessionStarted(testPlan, 0))
        store.append("old", WorkoutEvent.Finished(5_000))

        val controller = controller()

        assertEquals(Phase.COMPLETED, controller.state.value?.phase)
        assertEquals(5_000L, store.ended["old"])
        assertEquals(listOf("old"), archive.archived)
        assertTrue(tracker.calls.isEmpty())
    }

    @Test
    fun `a plan that fails to load blocks starting a session`() = runTest {
        val controller = controller(plans = PlanSource { error("broken json") })
        step(0) { controller.startSession() }

        assertEquals(PlanStatus.Failed, controller.plan.value)
        assertNull(controller.state.value)
        assertTrue(store.events.isEmpty())
    }
}
