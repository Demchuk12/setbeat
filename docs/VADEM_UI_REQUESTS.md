# Vadem UI requests

Things SetBeat needs from `@vadem/ui`. SetBeat does not build local copies; it waits or ships without.

| Need | Why in SetBeat | Proposal | Status |
| --- | --- | --- | --- |
| Icons `refresh-cw`, `watch`, `heart-pulse` | Sync button, watch status badge, empty state and heart-rate labels have no fitting icon (`activity` is used as a stand-in) | Add the three Lucide icons to `tools/icons.json` | open |
| Highlighted ranges on `vd-line-chart` | Session detail will show heart rate over time with sets shaded and rest windows marked | Optional `bands: { from, to, label }[]` drawn behind the series, listed in the data table | later, check when the detail screen starts |
| Product ramps from a hue | SetBeat adds effort (65°) and recovery (215°) colors and has to repeat Vadem's `oklch(var(--vd-l-N) calc(chroma * var(--vd-c-N)) hue)` formula per step | Documented way for an app to declare a ramp from `hue` + `chroma` (generated steps 50–950), so product colors follow future curve changes | open |
| Non-zero value axis on `vd-line-chart` | Heart rate lives between ~60 and ~190 bpm; a zero baseline squeezes the trace into the top half of the plot | Optional `domain: [min, max]` or `zero: false` for line charts (bars keep zero) | open |
