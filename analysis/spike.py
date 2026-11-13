import argparse
import json
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import pandas as pd

RELIABLE = {"ACCURACY_HIGH", "ACCURACY_MEDIUM"}
GAP_SECONDS = 1.5
HRR_SECONDS = 60
WINDOW_SECONDS = 3


def load(path: Path):
    data = json.loads(path.read_text())
    start = data["startedAt"]
    samples = pd.DataFrame(data["samples"], columns=["timestamp", "bpm", "accuracy"])
    samples["t"] = (samples["timestamp"] - start) / 1000
    return data, samples, pair_sets(data["events"], start)


def event_type(event):
    return event.get("payload", {}).get("type") or event.get("type")


def pair_sets(events, start):
    sets, opened = [], None
    for event in events:
        t = (event["timestamp"] - start) / 1000
        kind = event_type(event)
        if kind in ("set_started", "SET_START"):
            opened = t
        elif kind in ("set_ended", "SET_END") and opened is not None:
            sets.append((opened, t))
            opened = None
    return pd.DataFrame(sets, columns=["start", "end"])


def bpm_near(samples, t):
    near = samples[(samples["t"] - t).abs() <= WINDOW_SECONDS]
    return near["bpm"].mean() if len(near) else float("nan")


def set_table(samples, sets):
    rows = []
    for i, s in sets.iterrows():
        next_start = sets["start"].iloc[i + 1] if i + 1 < len(sets) else float("inf")
        rest = next_start - s["end"] if next_start != float("inf") else float("nan")
        during = samples[(samples["t"] >= s["start"]) & (samples["t"] <= s["end"])]
        end_bpm = bpm_near(samples, s["end"])
        later = s["end"] + HRR_SECONDS
        hrr = end_bpm - bpm_near(samples, later) if later <= min(next_start, samples["t"].max()) else float("nan")
        rows.append({
            "set": i + 1,
            "duration_s": round(s["end"] - s["start"]),
            "rest_s": round(rest) if rest == rest else None,
            "peak_bpm": during["bpm"].max(),
            "end_bpm": round(end_bpm),
            "hrr60": round(hrr) if hrr == hrr else None,
        })
    return pd.DataFrame(rows).astype({"rest_s": "Int64", "hrr60": "Int64"})


def quality(samples):
    gaps = samples["t"].diff().dropna()
    return {
        "samples": len(samples),
        "duration_s": round(samples["t"].max() - samples["t"].min()) if len(samples) else 0,
        "gaps": int((gaps > GAP_SECONDS).sum()),
        "longest_gap_s": round(gaps.max(), 1) if len(gaps) else 0,
        "unreliable": int((~samples["accuracy"].isin(RELIABLE)).sum()),
    }


def plot(path, data, samples, sets):
    fig, ax = plt.subplots(figsize=(12, 4))
    for _, s in sets.iterrows():
        ax.axvspan(s["start"], s["end"], color="tab:orange", alpha=0.25, lw=0)
    line = samples[["t", "bpm"]].copy()
    breaks = line.loc[line["t"].diff() > GAP_SECONDS, ["t"]].assign(t=lambda d: d["t"] - 0.001, bpm=float("nan"))
    line = pd.concat([line, breaks]).sort_values("t")
    ax.plot(line["t"], line["bpm"], color="tab:red", lw=1.2)
    bad = samples[~samples["accuracy"].isin(RELIABLE)]
    if len(bad):
        ax.scatter(bad["t"], bad["bpm"], color="black", s=8, zorder=3, label="low accuracy")
        ax.legend(loc="upper right")
    ax.set_xlabel("time, s")
    ax.set_ylabel("bpm")
    ax.set_title(f"{data['exerciseType']} · {data['id'][:8]} · sets shaded, gaps > {GAP_SECONDS}s break the line")
    ax.grid(alpha=0.3)
    fig.tight_layout()
    out = path.with_suffix(".png")
    fig.savefig(out, dpi=150)
    plt.close(fig)
    return out


def main():
    parser = argparse.ArgumentParser(description="Heart rate and set markers from SetBeat session exports")
    parser.add_argument("files", nargs="+", type=Path)
    args = parser.parse_args()

    for path in args.files:
        data, samples, sets = load(path)
        print(f"\n{path.name}")
        print("  " + ", ".join(f"{k}={v}" for k, v in quality(samples).items()))
        if len(sets):
            print(set_table(samples, sets).to_string(index=False))
        else:
            print("  no sets")
        print(f"  plot: {plot(path, data, samples, sets)}")


if __name__ == "__main__":
    main()
