export interface PlannedExercise {
  name: string;
  sets: number;
  reps: number;
  weight: number;
  weightStep?: number;
  restSeconds: number;
}

export interface WorkoutPlan {
  id: string;
  name: string;
  exercises: PlannedExercise[];
}

export type EventPayload =
  | { type: "session_started"; plan: WorkoutPlan; at: number }
  | { type: "set_started"; at: number }
  | { type: "set_ended"; at: number }
  | { type: "set_logged"; weight: number; reps: number; rpe: number | null; at: number }
  | { type: "paused"; at: number }
  | { type: "resumed"; at: number }
  | { type: "finished"; at: number }
  | { type: "interrupted"; reason: string; at: number };

export interface SessionEvent {
  id: string;
  seq: number;
  timestamp: number;
  deviceId: string;
  schemaVersion: number;
  payload: EventPayload;
}

export interface HeartRateSample {
  timestamp: number;
  bpm: number;
  accuracy: string | null;
}

export interface SessionExport {
  schemaVersion: number;
  id: string;
  exerciseType: string;
  startedAt: number;
  endedAt: number | null;
  events: SessionEvent[];
  samples: HeartRateSample[];
}
