# ECU Forge Project Status

Last updated:
- 2026-03-15

## Summary

Project is in foundation-to-core transition.

Current position:
- Phase 0 mostly complete (architecture baseline and CI bootstrap done).
- Phase 1 partially complete (transport/session contracts and fake adapters done).
- Phase 2 started with read-only identification domain baseline.

## Completed Work

Architecture and modules:
- Multi-module skeleton created: `app`, `core`, `transport`, `feature-diagnostics`, `feature-telemetry`, `feature-map`.
- Dependency directions documented in `docs/MODULE_DEPENDENCY_RULES.md`.

CI and quality:
- GitHub Actions workflow added for build, unit tests, and lint.
- CI reported green by project update.

Core contracts:
- Typed transport contracts and failure model in `core`.
- Session state model with transition guards and error codes in `core`.
- Unit tests for transport result model and session transitions.

Transport testability:
- Scripted fake transport gateway for deterministic Bluetooth/USB-like scenarios.
- Unit tests for nominal, connection failure, invalid endpoint, and timeout behavior.

Diagnostics MVP baseline:
- Read-only ECU identification use case in `feature-diagnostics`.
- Compatibility gate before transport usage.
- UI-state model (`Loading`, `Success`, `Error`) and coordinator.
- Unit tests for unsupported ECU, connect failure, nominal identification, timeout, and coordinator progression.

## In Progress / Pending

Near-term pending items:
- Enforce module dependency rules in CI (automated forbidden-edge check).
- Wire diagnostics read-only flow to a concrete screen in `app`.
- Add compatibility matrix v0 document for supported ECU families.

Follow-up after wiring:
- Extend diagnostics to DTC read flow.
- Start telemetry screen wiring with buffered sampling strategy.

## Safety Validation Rules

- Validate each milestone increment in read-only mode first.
- Avoid big-bang validation at the end.
- Keep write/flash blocked until dedicated safety validation is completed.

## Risks and Watch Items

- Gradle wrapper files are still expected from Android Studio sync in some environments.
- Dependency-rule enforcement is documented but not yet automated in CI.
- Read-only identification logic is currently domain-first; app-level UX wiring is pending.

## Recommended Next Step

Execute in this order:
1. Add CI dependency-rule enforcement.
2. Wire diagnostics identification flow into `app` screen/state.
3. Publish compatibility matrix v0.

## Resume Pointers

Primary references:
- `docs/NEXT_ACTIONS.md` (execution queue)
- `docs/ROADMAP.md` (phase planning)
- `docs/MODULE_DEPENDENCY_RULES.md` (architecture constraints)
- `AGENTS.md` (quality/safety operating contract)
