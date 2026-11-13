# AGENTS.md

SetBeat is a Galaxy Watch 8 workout logger. The watch owns the active session; phone and backend only store and display.

## Layout

- `watch/` — Wear OS app (Kotlin, Compose for Wear OS, Health Services, Room)
- `docs/` — product brief and ADRs
- `analysis/` — spike analysis in Python, reads session exports from `spike-data/` (gitignored, real health data)
- `packages/analytics` — TypeScript: sets, rest, HRR30/60/120 and sample quality from session exports
- `phone/` — Angular + Capacitor, Kotlin plugin `WearSync` receives sessions over the Wearable Data Layer
- later: `backend/` (NestJS + PostgreSQL), `packages/contracts`, Vadem UI in `phone/`

## Architecture

Watch (`watch/app/src/main/java/com/vadem/setbeat`), dependencies point down only:

- `domain/` — pure Kotlin: plan, events, state, `WorkoutEngine.reduce`. No Android, no coroutines.
- `workout/` — `WorkoutController` and the ports it needs (`WorkoutEventStore`, `ExerciseTracker`, `RestTimer`, `SessionArchive`, `PlanSource`, `Clock`, `Logger`). No Android imports; tested with fakes.
- `data/`, `sensor/`, `sync/`, `platform/` — adapters implementing the ports (Room, Health Services, Data Layer, vibration).
- `ui/` — Compose screens, `WorkoutViewModel`; no business rules.
- `AppContainer` — the only place that creates and wires objects.

Phone (`phone/src/app`): routes in `app.routes.ts` (list, `sessions/:id`); `wear/` wraps the native plugin behind the `WEAR_SYNC` token; `sessions/` holds `SessionStore` (state + logic) and one folder per component (`session-list/`, `session-detail/`, `session-card/`, `session-stats/`, `heart-rate-chart/`, `set-table/`), each with its template, styles, spec and private helpers; `shared/` has pure helpers. Components never call the plugin directly; metrics come from `@setbeat/analytics`.

Rules: new I/O goes behind a port; one class per responsibility; no global mutable state outside `AppContainer`/explicit event buses; every rule in `domain/` or `workout/` gets a unit test.

## Design

UI work follows [docs/DESIGN_WORKFLOW.md](docs/DESIGN_WORKFLOW.md). Project skills live in `.claude/skills`.

## Who writes what

The owner writes these by hand. The agent may review and explain, but does not write them:

- workout engine: states, transitions, crash recovery
- HRR30/60/120 and sample quality rules
- event format, idempotency, sync deduplication
- ADRs and test cases for engine and HRR

The agent can take Gradle, CI, Docker, lint/test setup, screens from a given design, NestJS CRUD, test scaffolding and fixtures.

## Rules

- One task, one small PR. Run lint, tests and build before calling it done.
- Never invent Vadem UI or Health Services APIs. Check the source or the official docs.
- No fake sensor data in production code. Mocks only through `SimulatedHeartRateProvider` in dev/test.
- New feature: brainstorm, write a short plan, then one PR per step.
- Engine, HRR and sync code goes test-first.
- Keep comments to a minimum; prefer clear names.
- Once `graphify-out/GRAPH_REPORT.md` exists, read it before cross-module work and refresh the graph after structural changes.

## Commands

```bash
cd watch
./gradlew assembleDebug
./gradlew installDebug
./gradlew lint test

npm test

CI: `.github/workflows/ci.yml` runs web, watch and phone Android jobs on every push and PR.

scripts/push-plan.sh path/to/plan.json
scripts/pull-sessions.sh
analysis/.venv/bin/python analysis/spike.py spike-data/sessions/*.json
```
