# ECU Forge Roadmap

Execution companion:
- [Next Actions](NEXT_ACTIONS.md)
- [Project Status](PROJECT_STATUS.md)

## Scope and Planning Horizon

This roadmap defines the delivery path for ECU Forge from baseline stabilization to production-readiness.

Planning horizon:
- Near term: 0-3 months
- Mid term: 3-6 months
- Long term: 6-12 months

Guiding constraints:
- Safety and data integrity over feature speed.
- Incremental releases with reversible changes.
- No risky write/flash behavior without explicit validation evidence.

## Product Tracks

The roadmap is split into five tracks that progress in parallel:
- Platform and architecture
- Transport and protocol reliability
- Diagnostics and telemetry UX
- Map safety and integrity
- Quality, security, and delivery pipeline

## Progress Snapshot (2026-03-18)

- Phase 0: complete.
- Phase 1: complete for baseline scope.
- Phase 2: in progress (identification, DTC, telemetry read, DTC catalog selection baseline).
- Phase 3: started (telemetry export schema/policy baseline, pending storage integration).
- Phase 4-5: not started (intentionally blocked by safety gates).

## Phase 0 - Foundation Stabilization (Near Term)

Goal:
- Stabilize Android project structure and build reliability.

Deliverables:
- Clear module boundaries: `app`, `core`, `transport`, `feature-diagnostics`, `feature-telemetry`, `feature-map`.
- Baseline architecture contracts across `presentation -> domain -> data`.
- Environment reproducibility: Gradle wrapper, lint, formatting profile, CI bootstrap.

Acceptance criteria:
- Clean debug build on CI and local developer setup docs validated.
- Static checks run on pull requests.
- No critical architecture violations in module dependencies.

## Phase 1 - Transport and Session Core (Near Term)

Goal:
- Establish safe, testable ECU communication boundaries.

Deliverables:
- Transport interfaces for Bluetooth and USB adapters.
- Session state model with explicit state transitions.
- Timeout/retry policy abstraction with deterministic behavior.
- Structured event logging at transport and session boundaries.

Acceptance criteria:
- Transport simulation tests cover connect/disconnect/retry/failure paths.
- Session state transitions are traceable and validated in tests.
- Reproducible verification plan for both transport types.

## Phase 2 - Read-Only Diagnostics MVP (Near to Mid Term)

Goal:
- Ship first end-to-end user flow with zero destructive operations.

Deliverables:
- ECU identification and compatibility check flow.
- Read-only DTC retrieval and display.
- Read-only live sensor stream with buffered sampling and session logs.
- Session history storage for support diagnostics.

Acceptance criteria:
- End-to-end read-only flow demonstrated on supported ECU targets.
- Negative-path handling for unsupported ECU, transport loss, and timeouts.
- User-visible error states are explicit and actionable.

## Phase 3 - Telemetry and Reliability Hardening (Mid Term)

Goal:
- Improve observability, runtime resilience, and UX quality.

Deliverables:
- Background-safe telemetry capture pipeline.
- Sampling controls and stable chart refresh behavior.
- Structured diagnostic export for troubleshooting.
- Performance and memory budget checks for long sessions.

Acceptance criteria:
- Stable telemetry sessions over extended runtime windows.
- No critical ANR/crash regressions in telemetry workflows.
- Deterministic export format validated in tests.

## Phase 4 - Map Backup and Restore Safety (Mid to Long Term)

Goal:
- Introduce safe map operations with backup-first guarantees.

Deliverables:
- Backup-first workflow enforcement before write operations.
- Integrity checks (size/hash/compatibility) before restore/write.
- Pre-check gate: transport stability, ECU compatibility, battery assumption signal when available.
- Rollback guidance and failure-mode handling paths.

Acceptance criteria:
- All map operations require validated pre-check completion.
- Failure-mode scenarios tested and documented.
- Clear audit trail for critical ECU operations.

## Phase 5 - Controlled Write/Flash Enablement (Long Term)

Goal:
- Enable write/flash features progressively with strict safeguards.

Deliverables:
- Feature flags for staged rollout by ECU family.
- Explicit write safety checklist and operator confirmations.
- Post-write validation flow and recovery guidance.
- Risk-based release policy with rollback strategy.

Acceptance criteria:
- Validation evidence for each supported ECU family.
- No rollout without documented rollback and support playbook.
- High-risk behavior guarded behind controlled enablement.

## Cross-Cutting Quality Plan

For every phase:
- Unit tests for business rules and failure modes.
- Integration tests for transport/session boundaries where feasible.
- PR template enforcement: problem, scope, risk, validation, rollback.
- Security/privacy checks for diagnostic artifacts and permissions.

## Milestone Checklist

M1 - Baseline stable:
- Module boundaries finalized.
- CI checks active.

M2 - Core transport ready:
- Bluetooth and USB contracts validated.
- Session model test coverage established.

M3 - Read-only MVP shipped:
- Identification, DTC read, telemetry read live.

M4 - Map safety baseline:
- Backup/restore safe flow complete.

M5 - Write/flash staged:
- Feature-flagged rollout with full safety evidence.

## Risks and Mitigations

Risk:
- ECU/protocol variability across manufacturers.
Mitigation:
- Per-ECU compatibility matrix and adapter strategy.

Risk:
- Transport instability in field conditions.
Mitigation:
- Explicit retry/timeout policies and diagnostics logging.

Risk:
- Unsafe map operation execution.
Mitigation:
- Mandatory pre-check gates and backup-first enforcement.

Risk:
- Scope creep before core stability.
Mitigation:
- Phase exit criteria and release gates.

## Immediate Next 30 Days

1. Expand transport parity evidence coverage with additional models and live-capture scenarios.
2. Resolve DTC dataset redistribution/licensing status and align provenance metadata.
3. Prepare real transport provider implementation behind the new feature entry contracts.
4. Track AGP/Gradle deprecation cleanup to keep CI future-proof for Gradle 10.
5. Add i18n mapping for DTC `titleKey` resources (English first, additional locales later).
