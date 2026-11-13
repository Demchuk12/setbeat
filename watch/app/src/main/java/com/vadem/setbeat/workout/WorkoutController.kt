package com.vadem.setbeat.workout

import com.vadem.setbeat.domain.IllegalTransition
import com.vadem.setbeat.domain.Phase
import com.vadem.setbeat.domain.WorkoutEngine
import com.vadem.setbeat.domain.WorkoutEvent
import com.vadem.setbeat.domain.WorkoutPlan
import com.vadem.setbeat.domain.WorkoutState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

sealed interface PlanStatus {
    data object Loading : PlanStatus
    data class Ready(val plan: WorkoutPlan) : PlanStatus
    data object Failed : PlanStatus
}

class WorkoutController(
    private val scope: CoroutineScope,
    private val store: WorkoutEventStore,
    private val plans: PlanSource,
    private val tracker: ExerciseTracker,
    private val restTimer: RestTimer,
    private val archive: SessionArchive,
    private val clock: Clock,
    private val logger: Logger,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) : TrackingListener {

    private val mutex = Mutex()

    private val _plan = MutableStateFlow<PlanStatus>(PlanStatus.Loading)
    val plan: StateFlow<PlanStatus> = _plan.asStateFlow()

    private val _state = MutableStateFlow<WorkoutState?>(null)
    val state: StateFlow<WorkoutState?> = _state.asStateFlow()

    private var sessionId: String? = null
    private var awaitingArchive: String? = null

    init {
        state.distinctUntilChangedBy { it?.phase to it?.phaseStartedAt }
            .onEach(::updateRestTimer)
            .launchIn(scope)
        scope.launch {
            loadPlan()
            restore()
        }
    }

    fun startSession() = launch {
        val plan = (_plan.value as? PlanStatus.Ready)?.plan ?: return@launch
        mutex.withLock {
            if (_state.value?.isFinished == false) return@withLock
            val id = newId()
            val started = WorkoutEvent.SessionStarted(plan, clock.now())
            store.createSession(id, started.at)
            store.append(id, started)
            sessionId = id
            _state.value = WorkoutEngine.start(started)
            tracker.start(id)
        }
    }

    fun startSet() = dispatch { WorkoutEvent.SetStarted(it) }

    fun endSet() = dispatch { WorkoutEvent.SetEnded(it) }

    fun logSet(weight: Double, reps: Int, rpe: Double?) = dispatch { WorkoutEvent.SetLogged(weight, reps, rpe, it) }

    fun pause() = dispatch { WorkoutEvent.Paused(it) }

    fun resume() = dispatch { WorkoutEvent.Resumed(it) }

    fun finish() = dispatch { WorkoutEvent.Finished(it) }

    fun dismiss() = launch {
        mutex.withLock {
            if (_state.value?.isFinished == true) {
                _state.value = null
                sessionId = null
            }
        }
    }

    override suspend fun onTrackingEnded(reason: String) {
        apply(WorkoutEvent.Interrupted(reason, clock.now()))
        val id = mutex.withLock { awaitingArchive.also { awaitingArchive = null } } ?: return
        archive.archive(id)
    }

    private suspend fun loadPlan() {
        _plan.value = try {
            PlanStatus.Ready(plans.load())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn("plan load failed", e)
            PlanStatus.Failed
        }
    }

    private suspend fun restore() = mutex.withLock {
        val open = store.findOpenSession() ?: return@withLock
        val restored = WorkoutEngine.replay(open.events)
        if (restored == null) {
            store.endSession(open.id, clock.now())
            return@withLock
        }
        sessionId = open.id
        _state.value = restored
        logger.info("restored session=${open.id} phase=${restored.phase} events=${open.events.size}")
        if (restored.isFinished) {
            store.endSession(open.id, checkNotNull(restored.endedAt))
            archive.archive(open.id)
        } else {
            tracker.resume(open.id)
        }
    }

    private fun dispatch(event: (now: Long) -> WorkoutEvent) = launch { apply(event(clock.now())) }

    private suspend fun apply(event: WorkoutEvent) = mutex.withLock {
        val current = _state.value?.takeUnless { it.isFinished } ?: return@withLock
        val id = sessionId ?: return@withLock
        val next = try {
            WorkoutEngine.reduce(current, event)
        } catch (e: IllegalTransition) {
            logger.warn(e.message.orEmpty())
            return@withLock
        }
        store.append(id, event)
        _state.value = next
        if (next.isFinished) {
            store.endSession(id, checkNotNull(next.endedAt))
            awaitingArchive = id
            tracker.stop()
        }
    }

    private fun updateRestTimer(state: WorkoutState?) {
        val remaining = state?.takeIf { it.phase == Phase.RECOVERY }?.restRemainingMillis(clock.now())
        if (remaining == null) restTimer.cancel() else restTimer.schedule(remaining)
    }

    private fun launch(block: suspend () -> Unit) {
        scope.launch { block() }
    }
}
