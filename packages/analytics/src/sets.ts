import type { EventPayload, SessionEvent, WorkoutPlan } from "./types.js";

export interface PerformedSet {
  index: number;
  exercise: string;
  exerciseIndex: number;
  setInExercise: number;
  weight: number;
  reps: number;
  rpe: number | null;
  logged: boolean;
  startedAt: number;
  endedAt: number;
}

export interface Pause {
  startedAt: number;
  endedAt: number | null;
}

export interface SessionTimeline {
  plan: WorkoutPlan | null;
  sets: PerformedSet[];
  pauses: Pause[];
  finishedAt: number | null;
  interrupted: { reason: string; at: number } | null;
}

interface Cursor {
  exercise: number;
  set: number;
}

export function buildTimeline(events: SessionEvent[]): SessionTimeline {
  const payloads = [...events].sort((a, b) => a.seq - b.seq).map((e) => e.payload);
  const timeline: SessionTimeline = { plan: null, sets: [], pauses: [], finishedAt: null, interrupted: null };
  let cursor: Cursor = { exercise: 0, set: 0 };
  let runningSince: number | null = null;
  let unlogged: { startedAt: number; endedAt: number } | null = null;

  const record = (values: { weight: number; reps: number; rpe: number | null } | null) => {
    if (!unlogged || !timeline.plan) return;
    const exercise = timeline.plan.exercises[cursor.exercise];
    if (!exercise) return;
    const target = values ?? defaultsFor(timeline.sets, cursor.exercise, exercise.weight, exercise.reps);
    timeline.sets.push({
      index: timeline.sets.length,
      exercise: exercise.name,
      exerciseIndex: cursor.exercise,
      setInExercise: cursor.set,
      weight: target.weight,
      reps: target.reps,
      rpe: target.rpe,
      logged: values !== null,
      startedAt: unlogged.startedAt,
      endedAt: unlogged.endedAt,
    });
    unlogged = null;
    cursor = cursor.set + 1 < exercise.sets ? { exercise: cursor.exercise, set: cursor.set + 1 } : { exercise: cursor.exercise + 1, set: 0 };
  };

  for (const event of payloads) {
    switch (event.type) {
      case "session_started":
        timeline.plan = event.plan;
        break;
      case "set_started":
        record(null);
        runningSince = event.at;
        break;
      case "set_ended":
        if (runningSince !== null) unlogged = { startedAt: runningSince, endedAt: event.at };
        runningSince = null;
        break;
      case "set_logged":
        record({ weight: event.weight, reps: event.reps, rpe: event.rpe });
        break;
      case "paused":
        timeline.pauses.push({ startedAt: event.at, endedAt: null });
        break;
      case "resumed":
        closeLastPause(timeline.pauses, event.at);
        break;
      case "finished":
      case "interrupted":
        record(null);
        closeLastPause(timeline.pauses, event.at);
        runningSince = null;
        timeline.finishedAt = event.at;
        if (event.type === "interrupted") timeline.interrupted = { reason: event.reason, at: event.at };
        break;
      default:
        assertNever(event);
    }
  }
  return timeline;
}

function defaultsFor(sets: PerformedSet[], exerciseIndex: number, weight: number, reps: number) {
  const previous = sets.findLast((s) => s.exerciseIndex === exerciseIndex);
  return { weight: previous?.weight ?? weight, reps: previous?.reps ?? reps, rpe: null };
}

function closeLastPause(pauses: Pause[], at: number) {
  const last = pauses.at(-1);
  if (last && last.endedAt === null) last.endedAt = at;
}

export function pausedWithin(pauses: Pause[], from: number, to: number): number {
  return pauses.reduce((total, pause) => {
    const start = Math.max(pause.startedAt, from);
    const end = Math.min(pause.endedAt ?? to, to);
    return total + Math.max(0, end - start);
  }, 0);
}

function assertNever(value: never): never {
  throw new Error(`unknown event ${JSON.stringify(value as EventPayload)}`);
}
