import type { EventPayload, HeartRateSample, SessionExport, WorkoutPlan } from "../src/types.js";

export const START = 1_700_000_000_000;
export const s = (seconds: number) => START + seconds * 1000;

export const plan: WorkoutPlan = {
  id: "test",
  name: "Test plan",
  exercises: [
    { name: "Squat", sets: 2, reps: 5, weight: 100, restSeconds: 120 },
    { name: "Row", sets: 1, reps: 10, weight: 50, restSeconds: 60 },
  ],
};

type Payload = EventPayload extends infer P ? (P extends { at: number } ? Omit<P, "at"> : never) : never;

export function session(
  timeline: Array<[seconds: number, payload: Payload]>,
  samples: HeartRateSample[] = [],
  endedAt: number | null = null,
): SessionExport {
  const payloads: EventPayload[] = [
    { type: "session_started", plan, at: START },
    ...timeline.map(([seconds, payload]) => ({ ...payload, at: s(seconds) }) as EventPayload),
  ];
  return {
    schemaVersion: 2,
    id: "session-1",
    exerciseType: "STRENGTH_TRAINING",
    startedAt: START,
    endedAt,
    events: payloads.map((payload, seq) => ({
      id: `e${seq}`,
      seq,
      timestamp: payload.at,
      deviceId: "watch",
      schemaVersion: 1,
      payload,
    })),
    samples,
  };
}

export function samplesFrom(fromSeconds: number, toSeconds: number, bpm: (t: number) => number, accuracy = "ACCURACY_HIGH") {
  const result: HeartRateSample[] = [];
  for (let t = fromSeconds; t <= toSeconds; t++) result.push({ timestamp: s(t), bpm: bpm(t), accuracy });
  return result;
}
