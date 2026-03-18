# ECU Forge Session Handoff (2026-03-18)

## Purpose

Provide a compact continuity snapshot so work can resume quickly without re-discovery.

## Scope Completed In This Session

- Roadmap and execution docs were aligned to current implementation status.
- DTC i18n strategy was clarified and reverted to JSON-catalog single source for now.
- Diagnostics transport-backed provider scaffold was introduced behind existing feature contracts.

## Key Decisions

1. Keep write/flash blocked.
- No write or flash behavior was changed.
- All changes remain in read-only diagnostics and documentation tracks.

2. DTC text source decision.
- Current DTC descriptions in app flows come from JSON catalog/default ECU payload descriptions.
- Android titleKey string lookup is deferred until a maintainable generation/mapping workflow exists.

3. Provider rollout strategy.
- Transport-backed diagnostics provider scaffold exists, but default app behavior remains variant-safe demo wiring.
- Real adapter wiring is pending and must be enabled in controlled increments.

## Commits Created

- feaf2de: feat: add transport-backed diagnostics provider scaffold
- 809021c: chore: align dtc i18n strategy with json catalogs
- 0e881aa: docs: updated roadmap

## Main Artifacts Added/Updated

Diagnostics provider scaffold:
- docs/NEXT_ACTIONS.md
- docs/PROJECT_STATUS.md
- docs/ROADMAP.md
- feature-diagnostics/src/main/java/com/ecuforge/feature/diagnostics/TransportBackedDiagnosticsFlowProvider.kt
- feature-diagnostics/src/test/java/com/ecuforge/feature/diagnostics/TransportBackedDiagnosticsFlowProviderTest.kt

DTC i18n strategy alignment:
- app/src/main/java/com/ecuforge/app/MainActivity.kt
- app/src/main/java/com/ecuforge/app/DtcStatusFormatter.kt
- app/src/main/res/values/strings.xml
- app/src/test/java/com/ecuforge/app/DtcStatusFormatterTest.kt
- feature-diagnostics/src/main/java/com/ecuforge/feature/diagnostics/domain/DtcModels.kt
- feature-diagnostics/src/main/java/com/ecuforge/feature/diagnostics/domain/ReadDtcUseCase.kt
- feature-diagnostics/src/test/java/com/ecuforge/feature/diagnostics/domain/ReadDtcUseCaseTest.kt
- docs/NEXT_ACTIONS.md
- docs/PROJECT_STATUS.md
- docs/ROADMAP.md

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

## Current Pending Work (Priority Order)

1. Promote model/family transport tuples from INFERRED to VALIDATED with live capture parity evidence.
2. Wire concrete Bluetooth and USB adapters into transport-backed providers for non-demo read-only validation.
3. Resolve DTC redistribution/licensing status and update provenance metadata.
4. Define maintainable DTC titleKey i18n workflow before enabling Android resource lookup.
5. Continue phased AGP/Gradle deprecation cleanup for Gradle 10 compatibility.

## Safe Resume Plan (Next Session)

1. Start with transport-backed diagnostics provider integration in debug only.
- Target: one concrete adapter path first (Bluetooth or USB).
- Keep release behavior unchanged and demo-safe.

2. Add tests for adapter integration boundaries.
- Include connection failure, timeout, and unsupported endpoint cases.
- Keep deterministic behavior and explicit error-code assertions.

3. Update continuity docs in the same cycle.
- docs/PROJECT_STATUS.md
- docs/NEXT_ACTIONS.md
- docs/ROADMAP.md

## Guardrails Reminder

- Keep increments small and auditable.
- Add tests for every behavior change.
- Avoid unrelated refactors.
- Keep map/write/flash blocked until Phase 4/5 safety gates are complete.
