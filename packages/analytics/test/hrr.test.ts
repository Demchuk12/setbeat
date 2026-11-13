import { describe, expect, it } from "vitest";
import { heartRateAt, heartRateTrace, sampleQuality } from "../src/heartRate.js";
import { heartRateRecovery } from "../src/hrr.js";
import { s, samplesFrom } from "./fixtures.js";

const recovering = samplesFrom(0, 200, (t) => (t <= 40 ? 100 + t : Math.max(90, 140 - 0.5 * (t - 40))));

describe("heartRateRecovery", () => {
  it("is the drop from set end to n seconds later", () => {
    const result = heartRateRecovery(recovering, s(40), null, 60);

    expect(result).toMatchObject({ status: "ok", endBpm: 139, laterBpm: 110 });
    expect(result.status === "ok" && result.value).toBe(29);
  });

  it("is not computed when the next set starts too early", () => {
    expect(heartRateRecovery(recovering, s(40), s(90), 60).status).toBe("rest_too_short");
    expect(heartRateRecovery(recovering, s(40), s(100), 60).status).toBe("ok");
  });

  it("needs a reliable sample near both points", () => {
    const withGap = recovering.filter((x) => x.timestamp < s(95) || x.timestamp > s(105));

    expect(heartRateRecovery(withGap, s(40), null, 60).status).toBe("no_later_sample");
    expect(heartRateRecovery([], s(40), null, 60).status).toBe("no_end_sample");
  });
});

describe("heartRateAt", () => {
  it("ignores unreliable samples", () => {
    const samples = [
      { timestamp: s(10), bpm: 100, accuracy: "ACCURACY_HIGH" },
      { timestamp: s(11), bpm: 180, accuracy: "UNRELIABLE" },
      { timestamp: s(12), bpm: 102, accuracy: "ACCURACY_MEDIUM" },
      { timestamp: s(12), bpm: 60, accuracy: null },
    ];

    expect(heartRateAt(samples, s(11))).toBe(101);
  });
});

describe("sampleQuality", () => {
  it("reports gaps, coverage and the reliable share", () => {
    const samples = [
      ...samplesFrom(0, 9, () => 100),
      ...samplesFrom(15, 19, () => 100, "ACCURACY_LOW"),
    ];

    expect(sampleQuality(samples, s(0), s(20))).toEqual({
      samples: 15,
      reliableShare: 10 / 15,
      gaps: 1,
      longestGapMs: 6_000,
      coverage: 15 / 20,
    });
  });
});

describe("heartRateTrace", () => {
  it("averages reliable samples into equal time buckets", () => {
    const samples = [
      ...samplesFrom(0, 9, () => 100),
      ...samplesFrom(10, 19, () => 140),
      { timestamp: s(15), bpm: 200, accuracy: "UNRELIABLE" },
    ];

    expect(heartRateTrace(samples, s(0), s(20), 2)).toEqual([100, 140]);
  });

  it("leaves gaps as null", () => {
    expect(heartRateTrace(samplesFrom(0, 4, () => 90), s(0), s(20), 4)).toEqual([90, null, null, null]);
  });

  it("returns nothing for an empty range", () => {
    expect(heartRateTrace([], s(10), s(10))).toEqual([]);
  });
});
