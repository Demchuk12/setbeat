import { describe, expect, it } from "vitest";
import { summarizeSession } from "../src/summary.js";
import { s, samplesFrom, session } from "./fixtures.js";

const bpm = (t: number) => {
  if (t >= 10 && t <= 40) return 100 + (t - 10);
  if (t > 40 && t < 170) return Math.max(90, 130 - 0.5 * (t - 40));
  if (t >= 170 && t <= 200) return 100 + (t - 170);
  return 95;
};

const workout = session(
  [
    [10, { type: "set_started" }],
    [40, { type: "set_ended" }],
    [45, { type: "set_logged", weight: 100, reps: 5, rpe: 8 }],
    [60, { type: "paused" }],
    [80, { type: "resumed" }],
    [170, { type: "set_started" }],
    [200, { type: "set_ended" }],
    [210, { type: "finished" }],
  ],
  samplesFrom(0, 210, bpm),
  s(210),
);

describe("summarizeSession", () => {
  const summary = summarizeSession(workout);

  it("summarizes the session", () => {
    expect(summary).toMatchObject({
      planName: "Test plan",
      status: "completed",
      durationMs: 210_000,
      totals: { sets: 2, volume: 1000 },
    });
  });

  it("excludes pauses from rest time", () => {
    expect(summary.sets[0]?.restMs).toBe(110_000);
    expect(summary.totals.averageRestMs).toBe(110_000);
  });

  it("computes set metrics and HRR within the rest", () => {
    const first = summary.sets[0]!;

    expect(first).toMatchObject({ durationMs: 30_000, peakBpm: 130, endBpm: 129 });
    expect(first.hrr[30]).toMatchObject({ status: "ok", value: 14 });
    expect(first.hrr[60]).toMatchObject({ status: "ok", value: 29 });
    expect(first.hrr[120]).toMatchObject({ status: "ok", value: 39 });
    expect(summary.totals.averageHrr60).toBe(29);
  });

  it("does not compute HRR past the end of the recording", () => {
    expect(summary.sets[1]?.hrr[60].status).toBe("no_later_sample");
  });

  it("marks sessions without an end as open", () => {
    const open = summarizeSession(session([[10, { type: "set_started" }]], samplesFrom(0, 20, () => 100)));

    expect(open.status).toBe("open");
    expect(open.durationMs).toBeNull();
    expect(open.quality.samples).toBe(21);
  });
});
