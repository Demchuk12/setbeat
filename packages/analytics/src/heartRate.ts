import type { HeartRateSample } from "./types.js";

export const RELIABLE_ACCURACY = new Set(["ACCURACY_HIGH", "ACCURACY_MEDIUM"]);
export const SAMPLE_WINDOW_MS = 3_000;
export const GAP_MS = 1_500;

export function isReliable(sample: HeartRateSample): boolean {
  return sample.accuracy !== null && RELIABLE_ACCURACY.has(sample.accuracy);
}

export function heartRateAt(samples: HeartRateSample[], at: number, windowMs = SAMPLE_WINDOW_MS): number | null {
  const near = samples.filter((s) => isReliable(s) && Math.abs(s.timestamp - at) <= windowMs).map((s) => s.bpm);
  return median(near);
}

export function peakBetween(samples: HeartRateSample[], from: number, to: number): number | null {
  const values = samples.filter((s) => isReliable(s) && s.timestamp >= from && s.timestamp <= to).map((s) => s.bpm);
  return values.length ? Math.max(...values) : null;
}

export interface SampleQuality {
  samples: number;
  reliableShare: number;
  gaps: number;
  longestGapMs: number;
  coverage: number;
}

export function sampleQuality(samples: HeartRateSample[], from: number, to: number): SampleQuality {
  const sorted = samples.filter((s) => s.timestamp >= from && s.timestamp <= to).sort((a, b) => a.timestamp - b.timestamp);
  let gaps = 0;
  let longestGapMs = 0;
  for (let i = 1; i < sorted.length; i++) {
    const gap = sorted[i]!.timestamp - sorted[i - 1]!.timestamp;
    if (gap > GAP_MS) gaps++;
    longestGapMs = Math.max(longestGapMs, gap);
  }
  const span = to - from;
  const coveredSeconds = new Set(sorted.map((s) => Math.floor((s.timestamp - from) / 1000))).size;
  return {
    samples: sorted.length,
    reliableShare: sorted.length ? sorted.filter(isReliable).length / sorted.length : 0,
    gaps,
    longestGapMs,
    coverage: span > 0 ? Math.min(1, coveredSeconds / Math.ceil(span / 1000)) : 0,
  };
}

function median(values: number[]): number | null {
  if (!values.length) return null;
  const sorted = [...values].sort((a, b) => a - b);
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 ? sorted[mid]! : (sorted[mid - 1]! + sorted[mid]!) / 2;
}

export function heartRateTrace(samples: HeartRateSample[], from: number, to: number, points = 48): (number | null)[] {
  if (to <= from || points < 1) return [];
  const sums = new Array<number>(points).fill(0);
  const counts = new Array<number>(points).fill(0);
  const bucketMs = (to - from) / points;
  for (const sample of samples) {
    if (!isReliable(sample) || sample.timestamp < from || sample.timestamp > to) continue;
    const index = Math.min(points - 1, Math.floor((sample.timestamp - from) / bucketMs));
    sums[index]! += sample.bpm;
    counts[index]! += 1;
  }
  return sums.map((sum, i) => (counts[i] ? Math.round(sum / counts[i]!) : null));
}
