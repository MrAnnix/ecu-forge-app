# ECU Forge Session Handoff (2026-03-18)

## Purpose

Provide a compact continuity snapshot so work can resume quickly without re-discovery.

## Scope Completed In This Session

- Continuity docs were aligned and expanded for ELM327 transport baseline (Bluetooth, USB cable, WiFi) with user-driven protocol selection.
- Compatibility tuple promotion and diagnostics/telemetry debug transport-backed pilots were completed and validated.
- WiFi transport endpoint support was added to transport contracts, fake gateway validation, and diagnostics/telemetry debug gateway pilots.
- ELM327 implementation reference was added, including architecture, adapter selection guidance, troubleshooting by symptom, and L1/L2 operational runbook.

## Key Decisions

1. Keep write/flash blocked.
- No write or flash behavior was changed.
- All changes remain in read-only diagnostics and documentation tracks.

2. DTC text source decision.
- Current DTC descriptions in app flows come from JSON catalog/default ECU payload descriptions.
- Android titleKey string lookup is deferred until a maintainable generation/mapping workflow exists.

3. Transport strategy.
- ELM327-compatible Bluetooth, USB cable, and WiFi are explicitly documented as baseline options.
- No protocol is prioritized by policy; user hardware determines selected transport.

4. Provider rollout strategy.
- Debug pilots now include concrete transport-backed paths for diagnostics and telemetry.
- Release behavior remains demo-disabled and unchanged.

## Commits Created

- d59f3bd: feat: add transport-backed debug pilots and ELM327 implementation reference
- f937a1c: feat: add wifi debug transport pilots for diagnostics and telemetry
- 0930f65: feat: normalize wifi transport hints in compatibility gate

## Main Artifacts Added/Updated

Continuity docs:
- docs/SPRINT_PLAN_2026-03-18.md
- docs/NEXT_ACTIONS.md
- docs/PROJECT_STATUS.md
- docs/ROADMAP.md
- docs/COMPATIBILITY_MATRIX_V0.md
- README.md

ELM327 implementation references:
- docs/ELM327_TRANSPORT_IMPLEMENTATION_REFERENCE.md

Diagnostics transport-backed debug pilots:
- feature-diagnostics/src/debug/java/com/ecuforge/feature/diagnostics/DebugBluetoothTransportGateway.kt
- feature-diagnostics/src/debug/java/com/ecuforge/feature/diagnostics/DebugUsbTransportGateway.kt
- feature-diagnostics/src/debug/java/com/ecuforge/feature/diagnostics/DebugWifiTransportGateway.kt
- feature-diagnostics/src/debug/java/com/ecuforge/feature/diagnostics/DiagnosticsDemoDelegate.kt
- feature-diagnostics/src/testDebug/java/com/ecuforge/feature/diagnostics/DebugBluetoothTransportGatewayTest.kt
- feature-diagnostics/src/testDebug/java/com/ecuforge/feature/diagnostics/DebugUsbTransportGatewayTest.kt
- feature-diagnostics/src/testDebug/java/com/ecuforge/feature/diagnostics/DebugWifiTransportGatewayTest.kt

Telemetry transport-backed debug pilots:
- feature-telemetry/src/main/java/com/ecuforge/feature/telemetry/TransportBackedTelemetryFlowProvider.kt
- feature-telemetry/src/debug/java/com/ecuforge/feature/telemetry/DebugUsbTelemetryTransportGateway.kt
- feature-telemetry/src/debug/java/com/ecuforge/feature/telemetry/DebugWifiTelemetryTransportGateway.kt
- feature-telemetry/src/debug/java/com/ecuforge/feature/telemetry/TelemetryDemoDelegate.kt
- feature-telemetry/src/test/java/com/ecuforge/feature/telemetry/TransportBackedTelemetryFlowProviderTest.kt
- feature-telemetry/src/testDebug/java/com/ecuforge/feature/telemetry/DebugUsbTelemetryTransportGatewayTest.kt
- feature-telemetry/src/testDebug/java/com/ecuforge/feature/telemetry/DebugWifiTelemetryTransportGatewayTest.kt

Transport contracts:
- core/src/main/java/com/ecuforge/core/transport/TransportContracts.kt
- transport/src/main/java/com/ecuforge/transport/fake/FakeTransportGateway.kt
- transport/src/test/java/com/ecuforge/transport/fake/FakeTransportGatewayTest.kt

Compatibility gate updates:
- feature-diagnostics/src/main/java/com/ecuforge/feature/diagnostics/domain/EcuCompatibilityGate.kt
- feature-diagnostics/src/test/java/com/ecuforge/feature/diagnostics/domain/EcuCompatibilityGateTest.kt

## Validation Executed

Diagnostics module:
- ./gradlew :feature-diagnostics:test
- Result: BUILD SUCCESSFUL

App unit tests:
- ./gradlew :app:testDebugUnitTest
- Result: BUILD SUCCESSFUL

Combined check:
- ./gradlew :feature-diagnostics:test :app:testDebugUnitTest
- Result: BUILD SUCCESSFUL

Transport/WiFi increment checks:
- ./gradlew :core:test :transport:test :feature-diagnostics:testDebugUnitTest
- Result: BUILD SUCCESSFUL
- ./gradlew :core:test :transport:test :feature-diagnostics:testDebugUnitTest :feature-telemetry:testDebugUnitTest
- Result: BUILD SUCCESSFUL

## Current Pending Work (Priority Order)

1. Promote model/family transport tuples from INFERRED to VALIDATED with live capture parity evidence.
2. Promote debug transport pilots (Bluetooth, USB cable, WiFi) into non-demo hardware-backed validation with reproducible parity evidence.
3. Resolve DTC redistribution/licensing status and update provenance metadata.
4. Define maintainable DTC titleKey i18n workflow before enabling Android resource lookup.
5. Continue phased AGP/Gradle deprecation cleanup for Gradle 10 compatibility.

Parity blocker snapshot:
- `bt-nominal` parity pass confirmed.
- Missing/insufficient captures still block closure for `bt-failure`, `usb-nominal`, and `usb-permission-denied`.
- Capture files without `TransportEvent` JSON entries are non-actionable and must be recaptured while scenario is actively reproduced.

## Safe Resume Plan (Next Session)

1. Execute parity capture closure in TuneECU workspace.
- Capture `bt-failure`, `usb-nominal`, and `usb-permission-denied` with real scenario activity.
- Re-run `run-all-scenarios-parity.ps1` and verify schema/sequence/payload status for each scenario.

2. Promote the next tuple from inferred to validated using collected evidence.
- Candidate prepared: `SIEMENS/SIE-ECU-01 + BLUETOOTH`.
- Update compatibility resource + tests + matrix/status/next-actions in the same cycle.

3. Prepare PR using sprint template.
- Use `docs/SPRINT_PLAN_2026-03-18.md` section `Parity Promotion PR Template`.
- Include problem/scope/risk/validation/rollback explicitly.

4. Keep release safety unchanged.
- No write/flash behavior changes.
- No release adapter enablement in this increment.

## Guardrails Reminder

- Keep increments small and auditable.
- Add tests for every behavior change.
- Avoid unrelated refactors.
- Keep map/write/flash blocked until Phase 4/5 safety gates are complete.

## End-of-Day State

- Branch: `main`
- Working tree: clean
- Latest commit: `0930f65`
