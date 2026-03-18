# ECU Forge Project Status

Last updated:
- 2026-03-18

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
- Session state model with transition guards and error codes in `core`.
- Unit tests for transport result model and session transitions.

Transport testability:
- Scripted fake transport gateway for deterministic Bluetooth/USB-like scenarios.
- Unit tests for nominal, connection failure, invalid endpoint, and timeout behavior.

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

Telemetry baseline:
- Read-only telemetry snapshot use case added in `feature-telemetry` with input validation and parse error handling.
- Demo transport wiring is isolated by variant (`debug` uses fake transport, `release` returns `DEMO_DISABLED`).
- App wiring added with dedicated telemetry action, UI state rendering, and formatter coverage.
- Telemetry unit tests cover success, timeout, invalid payload, and variant behavior.
- Buffered sampling baseline added (multi-frame read, buffer validation, and signal-set stability checks).
- Telemetry success state now includes buffered frame metadata for auditable read-only traces.

Provider contract baseline:
- Feature entrypoints now expose provider contracts (`DiagnosticsFlowProvider`, `TelemetryFlowProvider`) to replace demo/fake wiring without changing app call sites.
- Default behavior remains variant-safe (`debug` demo provider, `release` demo-disabled provider) and can be swapped through `installProvider(...)`.
- Contract tests were added to verify provider override behavior in both diagnostics and telemetry modules.

## In Progress / Pending

Near-term pending items:
- Add model-level compatibility evidence beyond family-gate baseline.

## Safety Validation Rules

- Validate each milestone increment in read-only mode first.
- Avoid big-bang validation at the end.
- Keep write/flash blocked until dedicated safety validation is completed.

## Risks and Watch Items

- `android.suppressUnsupportedCompileSdk=36` is temporary and should be removed once AGP/toolchain is fully aligned.
- Gradle deprecation warnings remain (`incompatible with Gradle 10`) and need phased cleanup.
- Read-only identification is wired and testable, but still demo-scaffolded pending real transport provider wiring.
- Compatibility matrix baseline exists, but model-level validation evidence is still pending.

## Status Update Policy

- Every relevant change (build, CI, architecture wiring, quality gates, dependencies) must be reflected in this file on the same delivery cycle.

## Recommended Next Step

Execute in this order:
1. Expand compatibility matrix from family-level to model-level evidence.
2. Track AGP/Gradle deprecation cleanup to keep CI future-proof for Gradle 10.
3. Define telemetry export format and retention policy for support diagnostics.

## Resume Pointers

Primary references:
- `docs/NEXT_ACTIONS.md` (execution queue)
- `docs/ROADMAP.md` (phase planning)
- `docs/MODULE_DEPENDENCY_RULES.md` (architecture constraints)
- `AGENTS.md` (quality/safety operating contract)
