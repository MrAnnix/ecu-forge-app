# ECU Forge Next Actions

## Purpose

This is the practical execution companion for the roadmap.
Use it to decide what to do next without replanning from scratch.
Current snapshot reference: `docs/PROJECT_STATUS.md`.
Current sprint brief: `docs/SPRINT_PLAN_2026-03-18.md`.

## Current Focus

Current target phase:
- Phase 2 completion (full read-only diagnostics user flow) -> Phase 3 preparation (controlled service-light reset)

Safety validation rules:
- Validate each milestone increment in read-only mode first.
- Avoid big-bang validation at the end.
- Keep write/flash blocked until dedicated safety validation is completed.

Current objective:
- Deliver a fully functional read-only diagnostics path in app-visible UX:
  - transport selection (Bluetooth, USB cable, WiFi),
  - searchable vehicle selection (make/model/year),
  - full DTC retrieval,
  - telemetry retrieval.

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
- Task 20 (Telemetry export storage integration baseline): app-private export persistence + retention cleanup + deterministic export ids completed.
- Task 21 (Compatibility transport evidence baseline): model+transport validated tuples loaded from versioned resource with TuneECU phase-0 references.
- Task 22 (Compatibility scenario coverage baseline): BT/USB nominal+failure references expanded for KEIHIN model baseline and inferred tuples tracked for additional families.
- Task 23 (DTC titleKey i18n decision): deferred Android `strings.xml` lookup and kept JSON-catalog descriptions as the single source for current read-only flows.
- Task 24 (Diagnostics transport-backed provider scaffold): added read-only transport-backed provider wiring for identification/DTC behind feature contracts with deterministic non-demo timeout behavior.
- Task 25 (Compatibility tuple promotion): promoted `KEIHIN/KM602EU + BLUETOOTH` from `INFERRED` to `VALIDATED` with transport-gate and identification-flow test coverage.
- Task 26 (Diagnostics debug adapter pilot): wired default debug diagnostics identification/DTC flows through transport-backed provider using concrete `DebugBluetoothTransportGateway` with deterministic boundary tests (invalid endpoint, connect failure, read timeout).
- Task 27 (Diagnostics USB debug adapter pilot): switched default debug diagnostics provider to concrete `DebugUsbTransportGateway` with deterministic boundary tests (invalid endpoint, connect failure, read timeout).
- Task 28 (Telemetry USB debug adapter pilot): wired default debug telemetry flow through `TransportBackedTelemetryFlowProvider` using concrete `DebugUsbTelemetryTransportGateway` with deterministic boundary tests (invalid endpoint, connect failure, read timeout).
- Task 29 (Transport requirement documentation): documented ELM327-compatible Bluetooth, USB cable, and WiFi as the explicit transport baseline for read-only diagnostics continuity.
- Task 30 (WiFi transport contract baseline): added `TransportEndpoint.Wifi(host, port)` support in core/transport contracts and fake gateway validation coverage.
- Task 31 (Diagnostics WiFi debug gateway pilot): added `DebugWifiTransportGateway` with deterministic boundary tests (invalid endpoint, connect failure, read timeout); non-demo promotion remains pending parity/hardware evidence.
- Task 32 (Telemetry WiFi debug gateway pilot): added `DebugWifiTelemetryTransportGateway` with deterministic boundary tests (invalid endpoint, connect failure, read timeout); non-demo promotion remains pending parity/hardware evidence.
- Task 33 (Compatibility WiFi hint normalization): updated `EcuCompatibilityGate` transport normalization to accept `WIFI`/`WI-FI` hints while still requiring validated tuple evidence for runtime support.
- Task 34 (App transport selector wiring baseline): added app-level transport selector mapping and feature-entry transport configuration hooks so diagnostics and telemetry flows can switch between Bluetooth/USB/WiFi in debug read-only mode.
- Task 35 (Searchable vehicle selector baseline): migrated make/model/year inputs to searchable dropdown UX with deterministic option lists for read-only DTC catalog context mapping.
- Task 36 (Telemetry Bluetooth debug gateway pilot): added `DebugBluetoothTelemetryTransportGateway` with deterministic boundary tests and integrated runtime selection via telemetry provider contract.
- Task 37 (Transport configuration profile analysis): documented required and advanced user-configurable parameters for Bluetooth/USB/WiFi in `docs/TRANSPORT_CONFIGURATION_PROFILE.md`.
- Task 38 (Transport profile MVP implementation): added app transport profile factory/store, dynamic transport form fields, validation, and runtime profile application into diagnostics/telemetry debug providers.
- Task 39 (Device settings screen split): moved transport profile editing from inline main screen panel to dedicated `DeviceSettingsActivity` launched from top app bar settings action.
- Next recommended task: complete transport + searchable vehicle selector UX path, then expand live-capture parity evidence for remaining inferred tuples.
- Prepared promotion candidate in sprint template: `SIEMENS/SIE-ECU-01 + BLUETOOTH`.

Parity blocker snapshot (2026-03-18):
- Verified via TuneECU batch parity run: `analysis/phase0/task-0.6/tools/run-all-scenarios-parity.ps1`.
- `bt-nominal` observed trace is present and passes schema/sequence/payload parity.
- ADB diagnostics check runs successfully, but `adb devices` currently returns no attached/authorized device.
- Missing capture files block the remaining scenarios:
	- `analysis/phase0/task-0.6/live/logcat-bt-failure.txt`
	- `analysis/phase0/task-0.6/live/logcat-usb-nominal.txt`
	- `analysis/phase0/task-0.6/live/logcat-usb-permission-denied.txt`
- Capture-quality blocker: batch parity fails when a scenario log exists but has no `TransportEvent` JSON entries (needs recapture while actively reproducing that scenario in-app).
- Immediate closure commands (TuneECU workspace):
	- `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force`
	- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario bt-failure`
	- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario usb-nominal`
	- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario usb-permission-denied`
	- `./analysis/phase0/task-0.6/tools/run-all-scenarios-parity.ps1 -LogcatDirectory ./analysis/phase0/task-0.6/live`
- PR write-up template: `docs/SPRINT_PLAN_2026-03-18.md` (`## Parity Promotion PR Template`).

## Prioritized Pending Checklist

- [ ] Complete end-to-end read-only app flow integration: transport selector -> searchable vehicle selector -> DTC retrieval -> telemetry retrieval.
- [x] Implement transport configuration profile MVP fields and validation in app flow (Bluetooth/USB/WiFi required fields).
- [ ] Promote additional family/model transport tuples from `INFERRED` to `VALIDATED` using live capture parity evidence.
- [ ] Promote debug adapter pilots into non-demo hardware-backed validation paths with reproducible transport verification evidence (ELM327 Bluetooth, USB cable, and WiFi) based on user-available hardware.
- [ ] Execute phased AGP/Gradle deprecation cleanup to preserve Gradle 10 compatibility.
- [ ] Define and implement a maintainable workflow for DTC `titleKey` i18n (generation or controlled mapping) before enabling Android string lookup.
- [ ] Track DTC redistribution/licensing closure as a pre-release packaging gate (non-blocking for ongoing read-only development).
- [ ] Keep service-light reset and all broader write/flash capabilities blocked until read-only completion criteria and safety gates are explicitly met.

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
- Read-only diagnostics flow is fully functional in app UX (transport select + vehicle select + DTC + telemetry) with reproducible validation evidence.

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
- Read `docs/SESSION_HANDOFF_2026-03-18.md` to restore latest decisions and commit anchors.
- Expand model-level compatibility evidence with transport/hardware parity validation traces.
- Wire concrete Bluetooth/USB transport adapters into `DiagnosticsFlowProvider` and `TelemetryFlowProvider` for non-demo read-only validation.
- Define DTC i18n `titleKey` maintenance workflow and confirm dataset redistribution terms.
- Track AGP/Gradle deprecation cleanup and remove temporary suppressions safely.
- Keep map/write/flash work blocked until Phase 4 pre-check gates and rollback evidence are implemented and validated.
