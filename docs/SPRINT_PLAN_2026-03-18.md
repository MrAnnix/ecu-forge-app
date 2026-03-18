# ECU Forge Sprint Plan (2026-03-18)

## Purpose

Define the next small, auditable sprint as two focused pull requests that preserve read-only safety constraints.

Planning anchors:
- `docs/PROJECT_STATUS.md`
- `docs/ROADMAP.md`
- `docs/NEXT_ACTIONS.md`
- `docs/COMPATIBILITY_MATRIX_V0.md`
- `docs/MODULE_DEPENDENCY_RULES.md`
- `AGENTS.md`

External continuity anchors (reference only):
- `C:/Users/thean/Repos/TuneECU/analysis/PHASE0_VALIDATION_REPORT.md`
- `C:/Users/thean/Repos/TuneECU/analysis/PHASE0_CONTINUATION_HANDOFF.md`
- `C:/Users/thean/Repos/TuneECU/analysis/phase0/task-0.6/tools/run-all-scenarios-parity.ps1`

## Hard Constraints

- Keep write/flash behavior blocked.
- Keep changes small, reversible, and auditable.
- Include tests for every behavior change.
- Preserve module dependency direction rules.
- Treat ELM327-compatible Bluetooth, USB cable, and WiFi adapters as the transport baseline for read-only diagnostics continuity; protocol selection is user-driven based on available hardware.

## Priority Initiatives for This Sprint

1. Promote one additional model+transport tuple from `INFERRED` to `VALIDATED` with parity evidence.
2. Wire one concrete diagnostics transport adapter path in `debug` only behind existing provider contracts.
3. Keep `release` behavior demo-disabled and unchanged.
4. Record evidence and risk notes in continuity docs during the same cycle.

## PR 1 - Transport Parity Promotion (Evidence-First)

### Goal

Increase compatibility confidence without changing transport behavior by promoting one additional tuple with validated evidence.

### Scope In

- Update compatibility evidence resource for one tuple only.
- Add/adjust compatibility gate tests for promoted tuple behavior.
- Update matrix and status docs with evidence references.

### Scope Out

- No adapter wiring changes.
- No telemetry/provider changes.
- No write/flash behavior changes.

### Candidate tuple

Pick exactly one tuple before coding (recommended whichever has lowest capture friction):
- `KEIHIN/KM602EU + BLUETOOTH`
- `SIEMENS/SIE-ECU-01 + BLUETOOTH`
- `MARELLI/IAW5AM + USB`
- `WALBRO/WB-ECU-01 + USB`

### Acceptance Criteria

Tests:
- Promoted tuple is accepted by transport-aware compatibility checks.
- Non-promoted inferred tuples remain rejected.
- Existing diagnostics tests stay green.

Documentation:
- `docs/COMPATIBILITY_MATRIX_V0.md` updated with tuple status and evidence links.
- `docs/PROJECT_STATUS.md` updated with completion note.
- `docs/NEXT_ACTIONS.md` updated to remove completed item detail and expose next pending tuple.

Evidence:
- Parity run output included in PR notes.
- Referenced trace artifacts and commands are reproducible.

### Validation Commands

- `./gradlew :feature-diagnostics:test`
- `./gradlew :app:testDebugUnitTest` (if app behavior is indirectly touched)
- TuneECU parity (reference environment):
  - `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force`
  - `./analysis/phase0/task-0.6/tools/run-all-scenarios-parity.ps1 -LogcatDirectory ./analysis/phase0/task-0.6/live`

### Risks and Rollback

Risks:
- False confidence if evidence references are stale or not reproducible.

Rollback:
- Revert tuple status from `VALIDATED` to `INFERRED` and revert associated tests/docs.

## PR 2 - Diagnostics Real Adapter Pilot (Debug Only)

Status:
- Completed on 2026-03-18 (debug-only Bluetooth adapter pilot through transport-backed provider).
- Extended on 2026-03-18 with debug-only USB adapter pilot through the same provider contract boundary.

### Goal

Replace demo-only diagnostics path with one concrete read-only adapter in `debug` while preserving safe `release` behavior.

### Scope In

- Wire one concrete transport adapter into diagnostics transport-backed provider for `debug` only.
- Preserve provider contract boundary (`DiagnosticsFlowProvider`).
- Keep deterministic error-code mapping for timeout/failure/unsupported conditions.
- Add unit tests for success and negative paths.

### Scope Out

- No telemetry adapter integration in this sprint.
- No release enablement changes.
- No map/write/flash operations.

### Acceptance Criteria

Tests:
- Debug variant test proves concrete adapter path is selected.
- Negative-path tests cover timeout, connection failure, and unsupported endpoint.
- Release variant test proves demo-disabled behavior remains unchanged.

Documentation:
- `docs/PROJECT_STATUS.md` updated with provider pilot status.
- `docs/NEXT_ACTIONS.md` updated with remaining adapter rollout tasks.
- `docs/ROADMAP.md` phase notes updated if milestone wording changes.

Evidence:
- Reproducible verification plan in PR description.
- CI checks green (`verifyModuleDependencyRules`, quality checks, unit tests).

### Validation Commands

- `./gradlew verifyModuleDependencyRules`
- `./gradlew qualityCheck`
- `./gradlew :feature-diagnostics:test :app:testDebugUnitTest`

### Risks and Rollback

Risks:
- Adapter boundary regressions can surface as error-code drift.

Rollback:
- Revert debug provider wiring to previous demo provider installation while keeping tests and docs aligned.

## Suggested Branches and Commit Shape

PR 1:
- Branch: `feature/compatibility-parity-promotion`
- Commits:
  - `feat: promote validated compatibility tuple`
  - `test: add compatibility tuple validation coverage`
  - `docs: update compatibility and status references`

PR 2:
- Branch: `feature/diagnostics-debug-adapter-pilot`
- Commits:
  - `feat: wire debug diagnostics adapter pilot`
  - `test: cover diagnostics adapter boundary behavior`
  - `docs: update roadmap status and next actions`

## Evidence Checklist Template (For Each PR)

- Problem statement
- Scope in/out
- Risk assessment
- Validation evidence (commands + outcomes)
- Rollback strategy

## Open Questions Before Execution

1. Which tuple has the highest-confidence live capture evidence ready for immediate promotion?
2. Which adapter path should be piloted first in debug (`BLUETOOTH` or `USB`)?
3. Who owns DTC redistribution/license sign-off timing for packaging readiness?
4. Should AGP/Gradle cleanup remain deferred to a dedicated stability PR after these two PRs?

## Follow-on Increment

After diagnostics Bluetooth and USB debug pilots, the next incremental adapter task is telemetry provider wiring in debug only.

Status:
- Completed on 2026-03-18 with `TransportBackedTelemetryFlowProvider` and `DebugUsbTelemetryTransportGateway`.

Next:
- Expand parity evidence for remaining inferred tuples and close DTC redistribution/licensing status before broader packaging.

Verified blocker snapshot (2026-03-18):
- TuneECU parity batch run currently passes only `bt-nominal`.
- ADB diagnostics are healthy, but there is no attached/authorized device in `adb devices` output.
- Missing TuneECU log captures prevent closure for:
  - `bt-failure`
  - `usb-nominal`
  - `usb-permission-denied`

Capture quality note:
- Even when a `logcat-<scenario>.txt` file exists, parity still fails if it contains no `TransportEvent` JSON entries (scenario was not actually exercised during capture).

Immediate follow-up commands (TuneECU workspace):
- `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force`
- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario bt-failure`
- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario usb-nominal`
- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario usb-permission-denied`
- `./analysis/phase0/task-0.6/tools/run-all-scenarios-parity.ps1 -LogcatDirectory ./analysis/phase0/task-0.6/live`

## Parity Promotion PR Template

Use this template for the next tuple-promotion PR once missing captures are collected.

### Problem Statement

- Remaining tuples are blocked at `INFERRED` due to missing live parity captures.
- Promotion requires reproducible validation evidence and explicit rollback path.

### Scope

In:
- Promote exactly one tuple from `INFERRED` to `VALIDATED`.
- Update compatibility evidence resource and transport-aware tests.
- Update continuity docs (`PROJECT_STATUS`, `NEXT_ACTIONS`, `COMPATIBILITY_MATRIX_V0`).

Out:
- No write/flash behavior changes.
- No release enablement changes.
- No unrelated refactors.

### Tuple Under Promotion

- Family: `SIEMENS`
- Model: `SIE-ECU-01`
- Transport: `BLUETOOTH`

Rationale:
- Keeps promotion incremental after KEIHIN tuple updates.
- Reuses the already partially validated parity workflow (`bt-nominal` pass) while keeping protocol choice user-driven by available hardware.

### Validation Evidence

| Scenario | Log file | Observed trace | Schema | Sequence | Payload parity | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| bt-nominal | analysis/phase0/task-0.6/live/logcat-bt-nominal.txt | analysis/phase0/task-0.6/live/trace-bt-nominal-observed.json | pass | pass | pass | Verified on 2026-03-18 batch run. |
| bt-failure | analysis/phase0/task-0.6/live/logcat-bt-failure.txt | analysis/phase0/task-0.6/live/trace-bt-failure-observed.json | pending | pending | pending | Missing capture file in current TuneECU live folder. |
| usb-nominal | analysis/phase0/task-0.6/live/logcat-usb-nominal.txt | analysis/phase0/task-0.6/live/trace-usb-nominal-observed.json | pending | pending | pending | Missing capture file in current TuneECU live folder. |
| usb-permission-denied | analysis/phase0/task-0.6/live/logcat-usb-permission-denied.txt | analysis/phase0/task-0.6/live/trace-usb-permission-denied-observed.json | pending | pending | pending | Missing capture file in current TuneECU live folder. |

Commands run:
- `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force`
- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario bt-failure`
- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario usb-nominal`
- `./analysis/phase0/task-0.6/tools/capture-scenario-logcat.ps1 -Scenario usb-permission-denied`
- `./analysis/phase0/task-0.6/tools/run-all-scenarios-parity.ps1 -LogcatDirectory ./analysis/phase0/task-0.6/live`
- `./gradlew :feature-diagnostics:test`
- `./gradlew :app:testDebugUnitTest`

### Code/Test Changes

- Updated tuple status in `feature-diagnostics/src/main/resources/compatibility/model_transport_parity.v1.json`.
- Added/updated compatibility gate tests for promoted tuple acceptance.
- Preserved at least one inferred tuple rejection test.

### Risk Assessment

- Primary risk: stale or partial parity evidence causing false promotion confidence.
- Mitigation: include raw capture paths and parity command output in PR notes.

### Rollback Strategy

- Revert tuple status to `INFERRED`.
- Revert tests and docs tied to the promotion.
- Keep captures and parity notes for audit history.
