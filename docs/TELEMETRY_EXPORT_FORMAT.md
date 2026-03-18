# Telemetry Export Format v1

## Purpose

Define a deterministic export contract for read-only telemetry snapshots used in support diagnostics.

## Scope

- Schema version: `telemetry-export.v1`
- Data source: `TelemetryUiState.Success`
- Format: JSON object with stable key ordering
- Retention policy defaults:
  - `maxAgeDays = 30`
  - `maxExportCount = 100`

## JSON Schema (Logical)

```json
{
  "schemaVersion": "telemetry-export.v1",
  "exportedAtEpochMillis": 1710000000000,
  "capturedFrameCount": 3,
  "sampleCount": 4,
  "samples": [
    {
      "signal": "RPM",
      "value": 1450,
      "unit": "rpm"
    }
  ]
}
```

## Field Semantics

- `schemaVersion`: export schema identifier.
- `exportedAtEpochMillis`: UTC epoch millis when export is generated.
- `capturedFrameCount`: buffered telemetry frame count used in consolidation.
- `sampleCount`: number of consolidated telemetry samples in `samples`.
- `samples`: normalized signal list sorted by signal name.

## Determinism Rules

- Signal entries are sorted by `signal`.
- String values are trimmed before export.
- Double values are serialized without trailing zeros when possible.
- String escaping is JSON-safe (`\` and `"`).

## Retention Policy

Retention decisions use `TelemetryRetentionPolicy.shouldRetain(...)`:

- Keep export only if:
  - age is within `maxAgeDays`, and
  - `newerExportCount < maxExportCount`.

## Storage Integration Baseline

- Storage target: app-private directory `filesDir/telemetry-exports`.
- Export artifact naming: `telemetry-<exportedAtEpochMillis>.json`.
- Persistence flow:
  - validate `TelemetryUiState.Success` contains at least one sample,
  - serialize deterministic JSON payload,
  - write export file,
  - list existing exports,
  - apply retention cleanup by age/count,
  - report non-fatal cleanup issues as warnings.

## Out of Scope

- ECU write/flash workflows.
- Encryption/signing of exported artifacts.
- Shared/external user-selectable storage paths.

