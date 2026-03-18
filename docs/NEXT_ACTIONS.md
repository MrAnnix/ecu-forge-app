# ECU Forge Next Actions

## Purpose

This is the practical execution companion for the roadmap.
Use it to decide what to do next without replanning from scratch.
Current snapshot reference: `docs/PROJECT_STATUS.md`.

## Current Focus

Current target phase:
- Phase 2 -> Phase 3 transition (read-only diagnostics and telemetry hardening)

Safety validation rules:
- Validate each milestone increment in read-only mode first.
- Avoid big-bang validation at the end.
- Keep write/flash blocked until dedicated safety validation is completed.

Current objective:
- Convert read-only baselines into app-visible workflows with auditable fallback behavior and storage integration.

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
- Task 10 (Read-only telemetry snapshot baseline): feature use case, app wiring, and tests completed.
- Task 11 (Read-only telemetry buffered sampling baseline): multi-frame sampling, stability checks, and tests completed.
- Task 12 (Provider contract baseline): feature entrypoint contracts added to swap demo/fake providers without app-flow changes.
- Task 13 (Diagnostics input hardening): identification and DTC request validation + negative-path parser tests completed.
- Task 14 (Compatibility matrix model baseline): model-level gate evidence and matrix entries added.
- Task 15 (Telemetry export baseline): deterministic export schema and retention policy defined with tests.
- Task 16 (DTC reference catalog baseline): versioned Triumph 2016-2019 dataset, validator, and provenance doc completed.
- Task 17 (DTC multi-catalog selection baseline): catalog index, vehicle-context selector, and ReadDtc opt-in enrichment completed.
- Task 18 (Default DTC fallback baseline): TuneECU-derived English fallback dictionary added as `defaultCatalog` with provisional `source.type=unknown`.
- Task 19 (App DTC selector wiring baseline): app inputs now map to `VehicleCatalogContext` and opt-in catalog descriptions in diagnostics flow.
- Next recommended task: implement storage path integration for telemetry export artifacts.

## Prioritized Pending Checklist

- [ ] Implement telemetry export storage path integration using `telemetry-export.v1` and retention policy.
- [ ] Expand compatibility evidence with transport/hardware parity traces per model.
- [ ] Add i18n mapping for DTC `titleKey` resources (English first, other locales later).
- [ ] Resolve DTC source redistribution/licensing status and update provenance metadata.
- [ ] Execute phased AGP/Gradle deprecation cleanup to preserve Gradle 10 compatibility.

## Historical Baseline Task List (Completed)

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

Note:
- Tasks above are preserved for historical traceability.
- Active execution should follow `## Prioritized Pending Checklist`.

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
- Wire app vehicle selector to `ReadDtcRequest.vehicleCatalogContext` and enable `preferCatalogDescriptions` in selected flows.
- Implement storage path integration for telemetry export artifacts.
- Expand model-level compatibility evidence with transport/hardware parity validation traces.
- Add DTC i18n `titleKey` resource mapping and confirm dataset redistribution terms.
- Track AGP/Gradle deprecation cleanup and remove temporary suppressions safely.
