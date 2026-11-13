# SetBeat

[![CI](https://github.com/Demchuk12/setbeat/actions/workflows/ci.yml/badge.svg)](https://github.com/Demchuk12/setbeat/actions/workflows/ci.yml)

Workout logger for Galaxy Watch 8: sets, weights, reps, rest timer and heart rate recovery, recorded on the wrist without a phone.

Work in progress. The watch app runs a full workout (plan, sets, rest timer, heart rate, crash recovery) and sends finished sessions to the phone, which shows heart rate, rest and recovery per set. Next: validating it in real gym sessions.

## Structure

- `watch/` — Wear OS app
- `docs/` — product brief and architecture decisions
- `analysis/` — heart rate plots and rough HRR from exported sessions
- `packages/analytics` — session metrics shared by the phone app and the future backend
- `phone/` — Angular + Capacitor app that receives sessions from the watch
