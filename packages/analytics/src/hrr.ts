import { heartRateAt } from "./heartRate.js";
import type { HeartRateSample } from "./types.js";

export const HRR_INTERVALS = [30, 60, 120] as const;
export type HrrInterval = (typeof HRR_INTERVALS)[number];

export type HrrResult =
  | { status: "ok"; seconds: number; value: number; endBpm: number; laterBpm: number }
  | { status: "rest_too_short" | "no_end_sample" | "no_later_sample"; seconds: number };

export function heartRateRecovery(
  samples: HeartRateSample[],
  setEndedAt: number,
  restEndedAt: number | null,
  seconds: number,
): HrrResult {
  const laterAt = setEndedAt + seconds * 1000;
  if (restEndedAt !== null && restEndedAt < laterAt) return { status: "rest_too_short", seconds };
  const endBpm = heartRateAt(samples, setEndedAt);
  if (endBpm === null) return { status: "no_end_sample", seconds };
  const laterBpm = heartRateAt(samples, laterAt);
  if (laterBpm === null) return { status: "no_later_sample", seconds };
  return { status: "ok", seconds, value: endBpm - laterBpm, endBpm, laterBpm };
}
