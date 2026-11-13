import type { SetMetrics } from "@setbeat/analytics";

export interface WorkoutRange {
  from: number;
  to: number;
  label: string;
  tone: "effort" | "recovery";
}

export interface Timeline {
  from: number;
  to: number;
  points: number;
}

export function workoutRanges(sets: readonly SetMetrics[], timeline: Timeline): WorkoutRange[] {
  const step = (timeline.to - timeline.from) / timeline.points;
  if (!(step > 0)) return [];
  const index = (at: number) => Math.min(timeline.points - 1, Math.max(0, Math.floor((at - timeline.from) / step)));

  return sets.flatMap((set, i) => {
    const work: WorkoutRange = {
      from: index(set.startedAt),
      to: index(set.endedAt),
      label: `${set.exercise} ${set.setInExercise + 1}`,
      tone: "effort",
    };
    const next = sets[i + 1];
    if (!next) return [work];
    const rest: WorkoutRange = { from: index(set.endedAt), to: index(next.startedAt), label: "Відпочинок", tone: "recovery" };
    return [work, rest];
  });
}
