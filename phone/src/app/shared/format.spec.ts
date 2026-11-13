import { describe, expect, it } from "vitest";
import { formatBpm, formatDecimal, formatDuration, formatKilograms, formatNumber, formatPercent } from "./format";

describe("format", () => {
  it("formats durations", () => {
    expect(formatDuration(null)).toBe("—");
    expect(formatDuration(65_000)).toBe("1:05");
    expect(formatDuration(3_725_000)).toBe("1:02:05");
  });

  it("formats numbers with units", () => {
    expect(formatNumber(null)).toBe("—");
    expect(formatNumber(29.4)).toBe("29");
    expect(formatKilograms(1250).replace(/\s/g, " ")).toBe("1 250 кг");
    expect(formatKilograms(102.5)).toBe("102,5 кг");
    expect(formatDecimal(8.5)).toBe("8,5");
    expect(formatDecimal(null)).toBe("—");
    expect(formatBpm(118)).toBe("118 уд/хв");
    expect(formatPercent(0.874)).toBe("87%");
  });
});
