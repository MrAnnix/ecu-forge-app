# ECU Compatibility Matrix v0

## Purpose

Define the initial compatibility baseline for read-only diagnostics and make supported ECU families explicit.

## Scope

- Version: `v0`
- Operation scope: read-only identification only
- Data source for family support: `EcuCompatibilityGate.DEFAULT_SUPPORTED_FAMILIES`
- Transport scope in this baseline: fake transport scenarios used by unit tests

## Support Status Legend

- `SUPPORTED`: allowed by current compatibility gate and validated in current read-only flow baseline.
- `PLANNED`: intentionally not enabled yet; requires implementation and validation evidence.
- `UNSUPPORTED`: explicitly outside current baseline.

## Read-Only Identification Baseline

| ECU family | Identification (read-only) | Transport evidence | Status | Notes |
| --- | --- | --- | --- | --- |
| `KEIHIN` | Yes | Fake Bluetooth endpoint path in diagnostics tests | SUPPORTED | Included in `DEFAULT_SUPPORTED_FAMILIES`. |
| `SIEMENS` | Yes | Gate-level support; no family-specific payload fixture yet | SUPPORTED | Included in `DEFAULT_SUPPORTED_FAMILIES`. |
| `MARELLI` | Yes | Gate-level support; no family-specific payload fixture yet | SUPPORTED | Included in `DEFAULT_SUPPORTED_FAMILIES`. |
| `WALBRO` | Yes | Gate-level support; no family-specific payload fixture yet | SUPPORTED | Included in `DEFAULT_SUPPORTED_FAMILIES`. |
| Other families | No | N/A | UNSUPPORTED | Rejected by compatibility gate in current baseline. |

## Validation Notes

- This matrix reflects family-level gate support, not full model-level validation.
- Current evidence is domain/test baseline; hardware parity validation is pending.
- Write/flash behavior remains out of scope and blocked.

## Promotion Criteria for Next Matrix Revision

Before marking a family/model as validated beyond baseline:

1. Add deterministic negative-path and boundary tests for identification payload parsing.
2. Add transport verification evidence for the target path (Bluetooth and/or USB).
3. Record failure-mode behavior and operator-visible error outcomes.
4. Update this document with reproducible evidence references.

## Traceability

- Compatibility gate source: `feature-diagnostics/src/main/java/com/ecuforge/feature/diagnostics/domain/EcuCompatibilityGate.kt`
- Project status snapshot: `docs/PROJECT_STATUS.md`
- Execution queue: `docs/NEXT_ACTIONS.md`

