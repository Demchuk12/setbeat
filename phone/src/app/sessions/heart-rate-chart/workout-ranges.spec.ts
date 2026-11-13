import type { SetMetrics } from "@setbeat/analytics";
import { describe, expect, it } from "vitest";
import { workoutRanges } from "./workout-ranges";

const set = (index: number, startedAt: number, endedAt: number) =>
  ({ index, exercise: "Squat", setInExercise: index, startedAt, endedAt }) as SetMetrics;

describe("workoutRanges", () => {
  it("turns sets and the rest between them into chart ranges", () => {
    const ranges = workoutRanges([set(0, 10_000, 40_000), set(1, 160_000, 190_000)], { from: 0, to: 240_000, points: 24 });

    expect(ranges).toEqual([
      { from: 1, to: 4, label: "Squat 1", tone: "effort" },
      { from: 4, to: 16, label: "Відпочинок", tone: "recovery" },
      { from: 16, to: 19, label: "Squat 2", tone: "effort" },
    ]);
  });

  it("keeps ranges inside the timeline", () => {
    expect(workoutRanges([set(0, -5_000, 999_000)], { from: 0, to: 10_000, points: 10 })).toEqual([
      { from: 0, to: 9, label: "Squat 1", tone: "effort" },
    ]);
  });

  it("returns nothing for an empty timeline", () => {
    expect(workoutRanges([set(0, 0, 1)], { from: 5, to: 5, points: 10 })).toEqual([]);
  });
});
