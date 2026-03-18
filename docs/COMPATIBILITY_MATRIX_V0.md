# ECU Compatibility Matrix v0

## Purpose

Define the initial compatibility baseline for read-only diagnostics and make supported ECU families/models explicit.

## Scope

- Version: `v0`
- Operation scope: read-only identification only
- Data source for family support: `EcuCompatibilityGate.DEFAULT_SUPPORTED_FAMILIES`
- Data source for model evidence: `EcuCompatibilityGate.DEFAULT_SUPPORTED_MODELS_BY_FAMILY`
- Data source for model+transport evidence: `feature-diagnostics/src/main/resources/compatibility/model_transport_parity.v1.json`
- Upstream technical evidence base: `C:/Users/thean/Repos/TuneECU`

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

- This matrix reflects family-level gate support plus initial model-level baseline evidence.
- Transport-aware model checks now require `status=VALIDATED` evidence tuples for runtime support.
- Inferred tuples are tracked for traceability but are not promoted to runtime-supported combinations.
- Write/flash behavior remains out of scope and blocked.

## Model Evidence Baseline

| ECU family | ECU model | Gate evidence | Status | Notes |
| --- | --- | --- | --- | --- |
| `KEIHIN` | `KM601EU` | `EcuCompatibilityGateTest.modelSupportReturnsTrueForValidatedModelEvidence` | SUPPORTED | Used in nominal identification payload fixtures. |
| `KEIHIN` | `KM602EU` | `EcuCompatibilityGateTest.modelSupportIsCaseInsensitiveAndTrimAware` | SUPPORTED | Case/trim normalization validated. |
| `SIEMENS` | `SIE-ECU-01` | `EcuCompatibilityGate.DEFAULT_SUPPORTED_MODELS_BY_FAMILY` constant coverage | SUPPORTED | Baseline evidence; hardware parity pending. |
| `MARELLI` | `IAW5AM` | `EcuCompatibilityGate.DEFAULT_SUPPORTED_MODELS_BY_FAMILY` constant coverage | SUPPORTED | Baseline evidence; hardware parity pending. |
| `WALBRO` | `WB-ECU-01` | `EcuCompatibilityGate.DEFAULT_SUPPORTED_MODELS_BY_FAMILY` constant coverage | SUPPORTED | Baseline evidence; hardware parity pending. |
| Known family, unknown model | Any not listed | `EcuCompatibilityGateTest.modelSupportReturnsFalseForUnknownModelInKnownFamily` | UNSUPPORTED | Explicitly rejected by model-level check. |

## Model + Transport Evidence Baseline

| ECU family | ECU model | Transport | Evidence status | Runtime support | Evidence reference |
| --- | --- | --- | --- | --- | --- |
| `KEIHIN` | `KM601EU` | `BLUETOOTH` | VALIDATED | SUPPORTED | `TuneECU/analysis/fixtures/trace-bt-nominal.json`, `TuneECU/analysis/fixtures/trace-bt-failure.json`, `TuneECU/analysis/PHASE0_VALIDATION_REPORT.md` |
| `KEIHIN` | `KM601EU` | `USB` | VALIDATED | SUPPORTED | `TuneECU/analysis/fixtures/trace-usb-nominal.json`, `TuneECU/analysis/fixtures/trace-usb-permission-denied.json`, `TuneECU/analysis/PHASE0_VALIDATION_REPORT.md` |
| `KEIHIN` | `KM602EU` | `BLUETOOTH` | INFERRED | UNSUPPORTED | `TuneECU/analysis/TRANSPORT_SEQUENCE_MAP.md`, `TuneECU/analysis/TRANSPORT_ENTRYPOINTS.md` |
| `SIEMENS` | `SIE-ECU-01` | `BLUETOOTH` | INFERRED | UNSUPPORTED | `TuneECU/analysis/TRANSPORT_SEQUENCE_MAP.md`, `TuneECU/analysis/TRANSPORT_ENTRYPOINTS.md` |
| `MARELLI` | `IAW5AM` | `USB` | INFERRED | UNSUPPORTED | `TuneECU/analysis/TRANSPORT_SEQUENCE_MAP.md`, `TuneECU/analysis/TRANSPORT_ENTRYPOINTS.md` |
| `WALBRO` | `WB-ECU-01` | `USB` | INFERRED | UNSUPPORTED | `TuneECU/analysis/TRANSPORT_SEQUENCE_MAP.md`, `TuneECU/analysis/TRANSPORT_ENTRYPOINTS.md` |

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

