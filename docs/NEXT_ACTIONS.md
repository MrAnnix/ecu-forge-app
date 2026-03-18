# ECU Forge Next Actions

## Purpose

This is the practical execution companion for the roadmap.
Use it to decide what to do next without replanning from scratch.
Current snapshot reference: `docs/PROJECT_STATUS.md`.

## Current Focus

Current target phase:
- Phase 0 (foundation stabilization)

Safety validation rules:
- Validar cada incremento por hito en modo read-only primero.
- Evitar validación big-bang al final.
- Mantener write/flash bloqueado hasta validación de seguridad dedicada.

Current objective:
- Prepare architecture and tooling so transport/session work can be implemented safely in Phase 1.

Execution status:
- Task 1 (Module baseline): completed.
- Task 2 (Dependency rules): completed (documented and enforced with `verifyModuleDependencyRules` in CI).
- Task 3 (CI baseline): completed with debug/release coverage and toolchain traceability logs.
- Task 4 (Transport contracts): typed contracts and baseline tests added.
- Task 5 (Session state model hardening and guard rules): completed.
- Task 6 (Fake adapters for tests): completed with scripted Bluetooth/USB fake gateway.
- Task 7 (Read-only identification MVP start): baseline use case, app wiring, and variant-specific behavior/tests added.
- Task 8 (Compatibility matrix v0): completed in `docs/COMPATIBILITY_MATRIX_V0.md`.
- Task 9 (Read-only DTC flow): baseline use case and app wiring completed.
- Next recommended task: continue telemetry read-only wiring.

## Priority Queue

Priority 1:
- Create module skeletons: `core`, `transport`, `feature-diagnostics`, `feature-telemetry`, `feature-map`.
- Define allowed dependency directions between modules.
- Add baseline quality checks (`lint`, unit tests) to CI.

Priority 2:
- Define domain contracts for transport and session state.
- Add fake adapters (Bluetooth/USB) for deterministic tests.
- Create session transition tests for nominal and failure paths.

Priority 3:
- Build first read-only identification flow entry screen.
- Add compatibility gate in domain layer before any operation starts.

## Task List (Sequential)

1. Module baseline
- Add Gradle modules with minimal compile-ready setup.
- Ensure app depends only on public APIs from feature/core modules.
- Keep transport/protocol code out of UI layer.

Definition of done:
- Project sync succeeds.
- Debug build succeeds.
- No cyclic dependencies.

2. Dependency rules
- Document module dependency rules in `docs/`.
- Enforce with Gradle checks where possible.

Definition of done:
- Rules are explicit and reviewed.
- Any forbidden dependency fails CI or is detectable in review checklist.

Current artifact:
- `docs/MODULE_DEPENDENCY_RULES.md`

3. CI baseline
- Add/verify CI workflow for build + unit tests + lint.
- Ensure pull requests run checks automatically.

Definition of done:
- CI green on baseline branch.
- Failed lint/tests block merge.

4. Transport contracts
- Add interfaces for transport lifecycle and data exchange.
- Add typed result model for success/error/timeouts.

Definition of done:
- Contracts compile and are covered by unit tests.
- No Android framework dependency in domain contracts.

5. Session state model
- Define explicit session states and transition events.
- Add transition guard rules and failure fallback behavior.

Definition of done:
- Transition tests cover connect failure, timeout, disconnect, and retry.
- Invalid transitions return predictable errors.

6. Fake adapters for tests
- Implement in-memory/fake Bluetooth and USB adapters.
- Support scripted responses for deterministic scenarios.

Definition of done:
- Tests run without physical device.
- Failure modes are reproducible.

7. Read-only identification MVP start
- Implement first read-only ECU identification use case.
- Add UI state rendering for loading/success/failure.

Definition of done:
- End-to-end read-only identification flow works with fake adapter.
- Errors are visible and actionable.

8. Compatibility matrix v0
- Define and document compatibility rules for ECU configurations.
- Validate initial set of configurations against these rules.

Definition of done:
- Compatibility matrix is published and accessible.
- Initial configurations are verified to meet compatibility requirements.

## Stop Conditions

Do not start map write/flash work until all are true:
- Backup-first policy and pre-check gates are implemented.
- Compatibility matrix rules are in place.
- Failure and rollback behavior are documented and tested.

## Daily Working Rhythm

At start of day:
- Pick one task from the sequential list.
- Write expected outcome and validation command before coding.

Before opening PR:
- Re-run tests/lint locally.
- Add risk notes and rollback notes.
- Confirm no unrelated refactor is included.

## Quick Resume Prompt

If you return later, continue with:
- Continue telemetry read-only wiring.
- Expand compatibility matrix with model-level validation evidence.
