import { describe, expect, it } from "vitest";
import { buildTimeline, pausedWithin } from "../src/sets.js";
import { s, session } from "./fixtures.js";

describe("buildTimeline", () => {
  it("maps sets to plan exercises in order", () => {
    const { sets } = buildTimeline(
      session([
        [10, { type: "set_started" }],
        [40, { type: "set_ended" }],
        [50, { type: "set_logged", weight: 102.5, reps: 4, rpe: 8.5 }],
        [170, { type: "set_started" }],
        [200, { type: "set_ended" }],
        [210, { type: "set_logged", weight: 102.5, reps: 4, rpe: null }],
        [330, { type: "set_started" }],
        [360, { type: "set_ended" }],
        [370, { type: "finished" }],
      ]).events,
    );

    expect(sets.map((x) => [x.exercise, x.setInExercise, x.weight, x.reps, x.logged])).toEqual([
      ["Squat", 0, 102.5, 4, true],
      ["Squat", 1, 102.5, 4, true],
      ["Row", 0, 50, 10, false],
    ]);
    expect(sets[0]).toMatchObject({ startedAt: s(10), endedAt: s(40), rpe: 8.5 });
  });

  it("uses the last lifted values of the same exercise for unlogged sets", () => {
    const { sets } = buildTimeline(
      session([
        [0, { type: "set_started" }],
        [30, { type: "set_ended" }],
        [35, { type: "set_logged", weight: 95, reps: 6, rpe: null }],
        [150, { type: "set_started" }],
        [180, { type: "set_ended" }],
        [300, { type: "set_started" }],
      ]).events,
    );

    expect(sets[1]).toMatchObject({ weight: 95, reps: 6, logged: false });
  });

  it("drops a set that was running when the session was interrupted", () => {
    const timeline = buildTimeline(
      session([
        [0, { type: "set_started" }],
        [30, { type: "set_ended" }],
        [40, { type: "set_started" }],
        [50, { type: "interrupted", reason: "sensor lost" }],
      ]).events,
    );

    expect(timeline.sets).toHaveLength(1);
    expect(timeline.interrupted).toEqual({ reason: "sensor lost", at: s(50) });
  });

  it("orders events by seq, not by array position", () => {
    const events = session([
      [0, { type: "set_started" }],
      [30, { type: "set_ended" }],
    ]).events.reverse();

    expect(buildTimeline(events).plan?.id).toBe("test");
  });

  it("tracks pauses and closes an open pause on finish", () => {
    const { pauses } = buildTimeline(
      session([
        [10, { type: "paused" }],
        [40, { type: "resumed" }],
        [100, { type: "paused" }],
        [130, { type: "finished" }],
      ]).events,
    );

    expect(pauses).toEqual([
      { startedAt: s(10), endedAt: s(40) },
      { startedAt: s(100), endedAt: s(130) },
    ]);
  });
});

describe("pausedWithin", () => {
  it("counts only the overlapping part of each pause", () => {
    const pauses = [
      { startedAt: s(10), endedAt: s(40) },
      { startedAt: s(90), endedAt: null },
    ];

    expect(pausedWithin(pauses, s(20), s(100))).toBe(30_000);
  });
});
