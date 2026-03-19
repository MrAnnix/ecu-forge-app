# ECU Forge Project Status

Last updated:
- 2026-03-19

## Summary

Project is in read-only diagnostics completion.

Current position:
- Phase 0 complete (architecture and CI baseline stable).
- Phase 1 complete for baseline scope (transport/session contracts and deterministic fake adapters).
- Phase 2 in progress with identification, DTC read, telemetry read, and DTC catalog selection baseline; current delivery focus is full app UX completion for this read-only path.
- Next gated write phase is service-light reset only; broader write/map scope remains blocked.

## Completed Work

Architecture and modules:
- Multi-module skeleton created: `app`, `core`, `transport`, `feature-diagnostics`, `feature-telemetry`, `feature-map`.
- Dependency directions documented in `docs/MODULE_DEPENDENCY_RULES.md`.

CI and quality:
- GitHub Actions workflow runs module dependency verification, unit tests, lint, and debug/release builds.
- `verifyModuleDependencyRules` is enforced in CI and fails on forbidden edges or undeclared/stale module rules.
- CI logs now include toolchain traceability (`java -version` and `./gradlew --version`).
- Kotlin quality gates are enforced with `qualityCheck` (Ktlint + Detekt), including KDoc rules for public APIs.
- Test assertions were standardized to AssertJ across modules (`app`, `core`, `transport`, `feature-diagnostics`, `feature-telemetry`) with explicit assertion descriptions.
- Detekt now blocks alternative assertion imports (`org.junit.Assert`, `kotlin.test`, `Truth`) to keep AssertJ as the mandatory style.

Build and toolchain hardening:
- `compileSdk` is aligned to `36` across Android modules (`app`, `core`, `transport`, `feature-diagnostics`, `feature-telemetry`, `feature-map`).
- Version catalog updates applied for stable AndroidX/tooling revisions (`core-ktx`, `activity`, `material`, `versions` plugin).
- Temporary warning suppression added in `gradle.properties`: `android.suppressUnsupportedCompileSdk=36`.
- Minor Ktlint compliance fix applied in app formatting path (`IdentificationStatusFormatter`).
- Continuity docs were aligned post-DTC (`docs/NEXT_ACTIONS.md`, `docs/ROADMAP.md`) to prioritize telemetry read-only wiring.

Core contracts:
- Typed transport contracts and failure model in `core`.
- Transport endpoint contract now includes WiFi host/port endpoint type for ELM327-compatible TCP adapter paths.
- Session state model with transition guards and error codes in `core`.
- Unit tests for transport result model and session transitions.

Transport testability:
- Scripted fake transport gateway for deterministic Bluetooth/USB-like scenarios.
- Unit tests for nominal, connection failure, invalid endpoint, and timeout behavior.
- Product transport baseline is explicitly ELM327-compatible Bluetooth, USB cable, and WiFi for read-only diagnostics continuity.

Diagnostics MVP baseline:
- Read-only ECU identification use case in `feature-diagnostics`.
- Read-only DTC retrieval flow wired in `app` with dedicated action and state rendering.
- Compatibility gate before transport usage.
- Compatibility matrix v0 published in `docs/COMPATIBILITY_MATRIX_V0.md`.
- UI-state model (`Loading`, `Success`, `Error`) and coordinator.
- Demo transport wiring is isolated by variant (`debug` uses fake transport, `release` returns `DEMO_DISABLED`).
- Variant-specific tests cover debug and release behavior.
- App formatter tests cover DTC state rendering (loading, empty, populated, error).
- Identification and DTC requests now enforce explicit input validation (`REQUEST_INVALID`) before transport access.
- Identification parser now rejects malformed payloads with unknown/duplicate keys through negative-path tests.
- Compatibility gate now exposes model-level evidence checks (`isModelSupported`) with deterministic unit coverage.
- Compatibility gate now enforces model+transport validation (`isModelSupportedForTransport`) using versioned evidence resource `compatibility/model_transport_parity.v1.json`.
- Transport parity baseline references TuneECU phase-0 artifacts (`analysis/fixtures` and transport validation report) for validated tuples.
- Transport parity coverage now includes explicit nominal/failure scenario references for Bluetooth and USB baseline tuples.
- Compatibility transport evidence now promotes `KEIHIN/KM602EU + BLUETOOTH` to `VALIDATED` with gate and use-case tests updated to preserve inferred-tuple rejection behavior for remaining entries.
- DTC reference catalog baseline added in `feature-diagnostics` with a versioned JSON dataset (`triumph_pcodes_2016_2019.v1`) and deterministic validation (code format, duplicates, provenance metadata).
- DTC data provenance documentation added in `docs/DTC_DATA_PROVENANCE.md` to track source metadata and licensing follow-up actions.
- Multi-catalog DTC selection baseline added through `catalog_index.v1.json` and `IndexedDtcCatalogRepository` with deterministic fallback to `defaultCatalog`.
- Default catalog fallback now points to `tuneecu_pcodes_en_default.v1.json` (English) with provisional `source.type=unknown` metadata.
- `ReadDtcRequest` now supports vehicle context (`make`, `model`, `modelYear`) and opt-in catalog description replacement in `ReadDtcUseCase`.
- Selection behavior and integration notes documented in `docs/DTC_CATALOG_SELECTION.md`.
- English UX copy baseline for vehicle selector and DTC catalog messaging documented in `docs/DTC_UX_COPY.md`.
- App vehicle selector baseline is now wired to diagnostics DTC flow (`VehicleCatalogContext` + `preferCatalogDescriptions`) with mapper tests in `app` and contract coverage in `feature-diagnostics`.
- App DTC rendering currently uses catalog/ECU descriptions from JSON datasets only; Android `titleKey` string lookup is intentionally deferred.

Telemetry baseline:
- Read-only telemetry snapshot use case added in `feature-telemetry` with input validation and parse error handling.
- Demo transport wiring is isolated by variant (`debug` uses fake transport, `release` returns `DEMO_DISABLED`).
- App wiring added with dedicated telemetry action, UI state rendering, and formatter coverage.
- Telemetry unit tests cover success, timeout, invalid payload, and variant behavior.
- Buffered sampling baseline added (multi-frame read, buffer validation, and signal-set stability checks).
- Telemetry success state now includes buffered frame metadata for auditable read-only traces.
- Telemetry export schema `telemetry-export.v1` and deterministic formatter added for support diagnostics artifacts.
- Retention policy baseline defined (`maxAgeDays=30`, `maxExportCount=100`) with unit coverage.
- Telemetry export storage integration added with app-private file persistence (`filesDir/telemetry-exports`), deterministic file naming, and retention cleanup flow.
- Telemetry export persistence now validates successful telemetry state inputs and reports predictable write/list/delete failure outcomes.

Provider contract baseline:
- Feature entrypoints now expose provider contracts (`DiagnosticsFlowProvider`, `TelemetryFlowProvider`) to replace demo/fake wiring without changing app call sites.
- Default behavior remains variant-safe (`debug` demo provider, `release` demo-disabled provider) and can be swapped through `installProvider(...)`.
- Contract tests were added to verify provider override behavior in both diagnostics and telemetry modules.
- Transport-backed diagnostics provider scaffold is now available for read-only identification/DTC flows behind existing provider contracts, with deterministic scenario-unavailable responses for demo-only timeout entrypoints.
- Debug diagnostics default provider now pilots transport-backed read-only identification and DTC flows through a concrete Bluetooth adapter wrapper (`DebugBluetoothTransportGateway`) while preserving release `DEMO_DISABLED` behavior.
- Debug adapter boundary tests now cover invalid endpoint, connection failure, and read-timeout mappings with deterministic error codes.
- Debug diagnostics provider pilot now includes USB adapter wiring (`DebugUsbTransportGateway`) as the default debug path, with deterministic connect and timeout behavior for read-only identification/DTC flows.
- Debug diagnostics WiFi gateway pilot (`DebugWifiTransportGateway`) is now available with deterministic boundary tests (invalid endpoint, connection failure, read timeout); default debug path remains USB until parity evidence promotion.
- Transport-backed telemetry provider scaffold is now available for read-only telemetry flows (`TransportBackedTelemetryFlowProvider`) behind existing feature contracts.
- Debug telemetry default provider now pilots concrete USB adapter wiring (`DebugUsbTelemetryTransportGateway`) with deterministic boundary tests (invalid endpoint, connection failure, read timeout) while preserving release `DEMO_DISABLED` behavior.
- Debug telemetry WiFi gateway pilot (`DebugWifiTelemetryTransportGateway`) is now available with deterministic boundary tests (invalid endpoint, connection failure, read timeout); default debug path remains USB until parity evidence promotion.

## In Progress / Pending

Near-term pending items:
- Complete end-to-end app UX for read-only diagnostics: transport selector (Bluetooth/USB/WiFi), searchable vehicle selector (make/model/year), DTC retrieval, and telemetry retrieval.
- Expand transport-specific parity evidence from baseline tuples to additional validated models and fresh live captures.
- Add Android i18n resource mapping for DTC `titleKey` values once generation/maintenance workflow is defined.
- Verify redistribution terms for external DTC source material before broad release packaging (tracked as release gate, not current implementation blocker).
- Promote debug adapter pilots into non-demo hardware-backed validation paths (ELM327 Bluetooth, USB cable, and WiFi) with reproducible verification evidence aligned to user-selected hardware.

Parity evidence blocker snapshot (2026-03-18):
- TuneECU batch parity run confirms `bt-nominal` parity pass.
- Remaining scenario captures are missing in TuneECU live folder (`bt-failure`, `usb-nominal`, `usb-permission-denied`), blocking additional tuple promotion.

## Safety Validation Rules

- Validate each milestone increment in read-only mode first.
- Avoid big-bang validation at the end.
- Keep write/flash blocked until dedicated safety validation is completed.

## Risks and Watch Items

- `android.suppressUnsupportedCompileSdk=36` is temporary and should be removed once AGP/toolchain is fully aligned.
- Gradle deprecation warnings remain (`incompatible with Gradle 10`) and need phased cleanup.
- Read-only identification is wired and testable, but still demo-scaffolded pending real transport provider wiring.
- Model-level baseline evidence exists, but transport/hardware parity validation by model remains pending.
- Live parity closure is currently gated by missing TuneECU capture files for 3 of 4 baseline scenarios.
- Vehicle selector UX currently provides baseline context wiring; searchable make/model/year UX completion and validation evidence are still pending.

## Status Update Policy

- Every relevant change (build, CI, architecture wiring, quality gates, dependencies) must be reflected in this file on the same delivery cycle.

## Recommended Next Step

Execute in this order:
1. Complete full read-only diagnostics UX path (transport selector -> searchable vehicle selector -> DTC retrieval -> telemetry retrieval) and validate end-to-end behavior.
2. Expand transport parity evidence for remaining inferred tuples with fresh live-capture traces.
3. Promote debug adapter pilots into non-demo hardware-backed validation paths with reproducible verification evidence.
4. Track AGP/Gradle deprecation cleanup to keep CI future-proof for Gradle 10.
5. Keep service-light reset as the first controlled write increment after read-only completion; keep broader write/flash blocked.

## Resume Pointers

Primary references:
- `docs/NEXT_ACTIONS.md` (execution queue)
- `docs/ROADMAP.md` (phase planning)
- `docs/MODULE_DEPENDENCY_RULES.md` (architecture constraints)
- `docs/SESSION_HANDOFF_2026-03-18.md` (latest session decisions, commits, and resume checklist)
- `AGENTS.md` (quality/safety operating contract)
