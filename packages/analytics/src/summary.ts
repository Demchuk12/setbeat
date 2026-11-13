import { heartRateAt, heartRateTrace, peakBetween, sampleQuality, type SampleQuality } from "./heartRate.js";
import { HRR_INTERVALS, heartRateRecovery, type HrrInterval, type HrrResult } from "./hrr.js";
import { buildTimeline, pausedWithin, type PerformedSet } from "./sets.js";
import type { SessionExport } from "./types.js";

export interface SetMetrics extends PerformedSet {
  durationMs: number;
  restMs: number | null;
  peakBpm: number | null;
  endBpm: number | null;
  hrr: Record<HrrInterval, HrrResult>;
}

export interface SessionSummary {
  id: string;
  planName: string | null;
  status: "completed" | "interrupted" | "open";
  startedAt: number;
  endedAt: number | null;
  durationMs: number | null;
  sets: SetMetrics[];
  totals: {
    sets: number;
    volume: number;
    averageRestMs: number | null;
    averageHrr60: number | null;
  };
  quality: SampleQuality;
  heartRateTrace: (number | null)[];
}

export function summarizeSession(session: SessionExport): SessionSummary {
  const timeline = buildTimeline(session.events);
  const endedAt = timeline.finishedAt ?? session.endedAt;
  const lastSampleAt = session.samples.at(-1)?.timestamp ?? session.startedAt;

  const sets = timeline.sets.map((set, i): SetMetrics => {
    const nextStart = timeline.sets[i + 1]?.startedAt ?? null;
    const restMs = nextStart === null ? null : nextStart - set.endedAt - pausedWithin(timeline.pauses, set.endedAt, nextStart);
    const hrr = Object.fromEntries(
      HRR_INTERVALS.map((seconds) => [seconds, heartRateRecovery(session.samples, set.endedAt, nextStart, seconds)]),
    ) as Record<HrrInterval, HrrResult>;
    return {
      ...set,
      durationMs: set.endedAt - set.startedAt,
      restMs,
      peakBpm: peakBetween(session.samples, set.startedAt, set.endedAt),
      endBpm: heartRateAt(session.samples, set.endedAt),
      hrr,
    };
  });

  const rests = sets.flatMap((s) => (s.restMs === null ? [] : [s.restMs]));
  const hrr60 = sets.flatMap((s) => (s.hrr[60].status === "ok" ? [s.hrr[60].value] : []));

  return {
    id: session.id,
    planName: timeline.plan?.name ?? null,
    status: timeline.interrupted ? "interrupted" : endedAt !== null ? "completed" : "open",
    startedAt: session.startedAt,
    endedAt,
    durationMs: endedAt === null ? null : endedAt - session.startedAt,
    sets,
    totals: {
      sets: sets.length,
      volume: sets.reduce((sum, s) => sum + s.weight * s.reps, 0),
      averageRestMs: average(rests),
      averageHrr60: average(hrr60),
    },
    quality: sampleQuality(session.samples, session.startedAt, endedAt ?? lastSampleAt),
    heartRateTrace: heartRateTrace(session.samples, session.startedAt, endedAt ?? lastSampleAt),
  };
}

function average(values: number[]): number | null {
  return values.length ? values.reduce((a, b) => a + b, 0) / values.length : null;
}
