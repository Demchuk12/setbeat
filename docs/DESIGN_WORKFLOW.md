# Design workflow

## Which skill when

| Surface | Read first | Then |
| --- | --- | --- |
| Watch screens (Wear OS) | `impeccable` + `reference/android.md`, mode Operate | Wear Material 3 components and theme; screenshots from the Wear emulator or the watch |
| Phone screens (Angular) | `vd-design-principles` | Vadem UI components and tokens only; `vd-a11y-check` for interactive parts |
| New surface or redesign | `frontend-design` | Plan first, review against the brief, then build |
| After any visual change | `vd-design-review` | Squint, grayscale, both themes, real Ukrainian content |

## Product direction

SetBeat is used mid-workout: sweaty hands, glances of a second or two, between heavy sets.

- One primary action per screen, reachable without scrolling.
- Numbers first: rest countdown, BPM, weight × reps. Labels stay short and quiet.
- Phone is for reviewing after the workout: history, trends, calm and readable.

## Color

Color encodes the workout state; everything else stays neutral. Product hues sit on Vadem's
shared OKLCH lightness/chroma curve, so they match its contrast at every step.

| Role | Hue / chroma | Dark (step 400) | Light text (step 700) | Use |
| --- | --- | --- | --- | --- |
| Action | Vadem accent 285 / 0.2 | `#9C93FF` | `#6855D6` | Primary buttons only |
| Effort | 65 / 0.16 | `#DC9241` | `#934B00` | Set timer, "time to lift", set ranges in charts |
| Recovery | 215 / 0.12 | `#4FB4CB` | `#006E84` | Rest countdown, recovery ranges in charts |
| Heart rate | 10 / 0.17 | `#ED7D91` | — | Heart icon only, never text |

- Effort vs recovery stays distinct with deuteranopia and protanopia (ΔE 19 / 18); red/green
  pairs drop below 10 and are not used.
- Effort is close to Vadem `warning` (75) and heart rate to `danger` (25): keep warnings and
  errors as text badges, never as large fills next to workout colors.
- Watch: `ui/theme/SetBeatTheme.kt` (`WorkoutColors`, `phaseColor`). Phone: `src/styles.css`
  defines `--sb-hue-effort` / `--sb-hue-recovery` on Vadem's public `--vd-l-*` / `--vd-c-*` curve
  and colors chart ranges with tones `effort` and `recovery`.

## Vadem UI

The phone uses `@vadem/ui` from npm. Do not restyle its components locally; when SetBeat
needs something the library lacks, write it down in `docs/VADEM_UI_REQUESTS.md` and tell the
owner instead of building a one-off copy.
