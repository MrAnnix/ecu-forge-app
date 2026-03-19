# Transport Configuration Profile (Read-Only First)

## Purpose

Define which transport parameters must be configurable by the user in ECU Forge, and which parameters should stay optional/advanced for troubleshooting.

This document is scoped to read-only diagnostics and telemetry flows.

Write/flash behavior remains out of scope.

## UX Goal

Users should be able to:

1. Choose a transport type (`Bluetooth`, `USB`, `WiFi`).
2. Configure transport-specific connection settings.
3. Validate connection settings before running ECU identification, DTC retrieval, or telemetry read.
4. Save and reuse profiles (global and optionally per vehicle selection).

## Transport Profiles

## Bluetooth (ELM327-compatible)

### Required (MVP)

- `macAddress`: paired adapter MAC address.
- `connectTimeoutMs`: connection timeout.
- `readTimeoutMs`: read timeout.

### Optional (Advanced)

- `autoReconnectEnabled`: retry on disconnect.
- `retryCount`: max reconnect attempts.
- `retryBackoffMs`: delay between retries.
- `sppUuidOverride`: custom SPP UUID for non-standard adapters.

### Validation Rules

- `macAddress` must match canonical MAC format (`XX:XX:XX:XX:XX:XX`).
- `connectTimeoutMs` and `readTimeoutMs` must be bounded (`500..30000`).
- `retryCount` must be bounded (`0..5`).

## USB (ELM327-compatible cable/OTG)

### Required (MVP)

- `vendorId`: USB vendor id.
- `productId`: USB product id.
- `connectTimeoutMs`: connection timeout.
- `readTimeoutMs`: read timeout.

### Optional (Advanced)

- `baudRate`: serial baud rate override.
- `dataBits`: serial data bits override.
- `stopBits`: serial stop bits override.
- `parity`: serial parity override.
- `flowControl`: serial flow control override.
- `retryCount`: reconnect attempts.
- `retryBackoffMs`: delay between retries.

### Validation Rules

- `vendorId` and `productId` must be positive 16-bit values.
- Serial overrides should default to adapter-safe values unless user explicitly enables advanced mode.
- Timeout/retry limits follow global bounded rules.

## WiFi (ELM327-compatible TCP adapters)

### Required (MVP)

- `host`: adapter IP or hostname.
- `port`: adapter TCP port.
- `connectTimeoutMs`: connection timeout.
- `readTimeoutMs`: read timeout.

### Optional (Advanced)

- `keepAliveEnabled`: socket keep-alive.
- `tcpNoDelayEnabled`: disable Nagle for low-latency requests.
- `retryCount`: reconnect attempts.
- `retryBackoffMs`: delay between retries.

### Validation Rules

- `host` must be non-blank and pass hostname/IP format checks.
- `port` must be in range (`1..65535`).
- Timeout/retry limits follow global bounded rules.

## Shared Configuration Rules

### Common Fields

- `transportType`: Bluetooth/USB/WiFi.
- `connectTimeoutMs`.
- `readTimeoutMs`.
- `retryCount`.
- `retryBackoffMs`.

### Safety and Predictability

- Invalid configuration must fail fast before transport connect.
- Validation errors must be user-visible and actionable.
- Last known good profile should be available as a one-tap fallback.
- Transport profile changes must be auditable in diagnostics logs (no sensitive payload data).

## Persistence Strategy

## MVP

- Persist the active profile in app-private storage.
- Persist one profile per transport type.
- Load profile on app start and pre-fill transport form.

## Follow-up

- Optional profile per vehicle selection (`make`, `model`, `year`).
- Optional profile import/export for support diagnostics.

## Proposed Incremental Delivery

1. Add typed transport profile models and validators.
2. Add app-level profile persistence repository.
3. Add transport setup form for required MVP fields only.
4. Add connection test action and validation messaging.
5. Add advanced fields behind an explicit toggle.

## Out of Scope

- Map/write/flash configuration.
- Security hardening beyond app-private storage baseline.
- Cloud sync of profile settings.

