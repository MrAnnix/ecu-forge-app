# ELM327 Transport Implementation Reference (No External OBD Library)

## Purpose

Provide a single reference for implementing ELM327 communication directly (without an external OBD library) across Bluetooth, USB cable, and WiFi transports.

Scope constraints:
- Read-only diagnostics continuity only.
- No write/flash operations.
- Protocol selection is user-driven based on available hardware.

## Authoritative Sources

Core ELM327 and OBD references:
- ELM327 datasheet (AT commands and behavior): https://www.elmelectronics.com/wp-content/uploads/2016/07/ELM327DS.pdf
- ELM327 command index: https://www.elmelectronics.com/ic/elm327/
- OBD-II PID reference (quick lookup): https://en.wikipedia.org/wiki/OBD-II_PIDs
- ISO-TP overview (multi-frame payloads on CAN): https://en.wikipedia.org/wiki/ISO_15765-2

Android platform references:
- Bluetooth permissions (Android 12+): https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
- Bluetooth device connectivity: https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices
- USB host overview: https://developer.android.com/guide/topics/connectivity/usb
- USB host API: https://developer.android.com/develop/connectivity/usb/host
- Java socket API (WiFi TCP path): https://developer.android.com/reference/java/net/Socket
- Network security config (cleartext policy): https://developer.android.com/privacy-and-security/security-config

## Recommended Internal Architecture

Keep the transport stack layered to minimize protocol bugs and adapter-specific regressions.

1. Transport adapter layer
- One adapter per medium: Bluetooth RFCOMM, USB serial, WiFi TCP.
- Contract responsibilities: connect, read, write, timeout, close.
- No ELM327 parsing in this layer.

2. ELM327 session layer
- Sends AT commands and maintains adapter state.
- Handles initialization, prompt/wait logic, and error token mapping.
- Normalizes raw text responses before they reach OBD parsers.

3. OBD service layer
- Builds service/PID requests (for example, mode 01 PIDs).
- Parses normalized payloads into domain models.
- Applies read-only safety guardrails.

4. Diagnostics/telemetry provider layer
- Uses existing provider contracts and variant gating.
- Keeps release behavior unchanged until validation is complete.

## Common ELM327 Session Sequence

Use a deterministic startup sequence per connection.

1. Reset
- `ATZ`

2. Disable noisy output
- `ATE0` (echo off)
- `ATL0` (linefeeds off)
- `ATS0` (spaces off)
- `ATH0` (headers off; enable only if needed for debugging)

3. Protocol handling
- `ATSP0` for automatic protocol detection, or fixed protocol if vehicle target is known.

4. Optional checks
- `ATI` to capture adapter identity.
- `ATDP` to record active protocol.

Implementation notes:
- Always terminate commands with carriage return (`\r`).
- Read until prompt (`>`), timeout, or a known terminal error token.
- Normalize all responses (trim, uppercase strategy, remove echo artifacts).

## Error and Timeout Model

Map raw adapter output into stable internal errors.

Expected response tokens to classify:
- `NO DATA`
- `STOPPED`
- `?`
- `UNABLE TO CONNECT`
- `ERROR`
- transport disconnect/timeouts at IO level

Guidelines:
- Set per-command timeout and bounded retry policy.
- Keep retries deterministic (fixed count, logged reason).
- Surface user-safe errors that do not suggest write/flash capabilities.

## Transport-Specific Guidance

### Bluetooth (Classic SPP / RFCOMM)

Expected path:
- Discover/pair adapter.
- Connect with SPP UUID `00001101-0000-1000-8000-00805F9B34FB`.
- Open input/output streams and execute ELM327 session sequence.

Android notes:
- Handle runtime permissions for `BLUETOOTH_CONNECT` and related Bluetooth permissions.
- Distinguish pairing failure, connection refusal, and mid-session link drop.

Validation checklist:
- Connection lifecycle is deterministic.
- Session init commands are acknowledged.
- PID request/response round-trip is stable under timeout stress.

### USB Cable (USB Host + Serial)

Expected path:
- Detect device attach events.
- Request and verify USB permission.
- Identify interface/endpoints (or serial class path) and open streams.
- Execute ELM327 session sequence on serial channel.

Android notes:
- Permission denial and detach events must map to explicit, testable failure states.
- Keep endpoint and packet handling isolated from ELM parsing.

Validation checklist:
- Attach/detach is handled without app restart.
- Permission denied path remains deterministic.
- Command timeouts are enforced during cable instability.

### WiFi (TCP Socket)

Expected path:
- Connect to adapter AP/network as required by device.
- Open TCP socket (common adapters use local LAN endpoint, often `35000`).
- Execute ELM327 session sequence over socket stream.

Android notes:
- Handle network transition and socket reconnect behavior.
- If cleartext traffic is required, ensure policy is explicitly declared and justified.

Validation checklist:
- Socket reconnect strategy avoids infinite loops.
- Round-trip timing remains acceptable with local AP jitter.
- IO errors map to the same internal error model as other transports.

## Cross-Transport Test Matrix

Minimum deterministic tests per transport:

1. Nominal
- Session init success.
- At least one read-only PID request success.

2. Failure
- Connection failure.
- Timeout during command execution.
- Unexpected token/error response from adapter.

3. Boundary
- Partial/truncated response handling.
- Prompt missing before timeout.
- Transport disconnect mid-request.

4. Safety
- Verify write/flash code paths are inaccessible in this flow.

## Operational Logging Guidance

For parity and troubleshooting:
- Log command name/category, not sensitive payload dumps by default.
- Include timestamp, transport type, and normalized error class.
- Preserve reproducible evidence paths used by parity scripts.

## Implementation Checklist

- Add/confirm one transport adapter class per medium.
- Keep ELM327 session logic transport-agnostic.
- Add deterministic unit tests for nominal/failure/boundary paths.
- Validate read-only safety constraints remain enforced.
- Update continuity docs with evidence references after each promotion.

## Adapter Selection Guide (Field Use)

There is no protocol priority by product policy. The user chooses the protocol based on the adapter/device they actually have.

### Quick Selection Matrix

| User hardware situation | Recommended transport | Why this path fits | Typical caveats |
| --- | --- | --- | --- |
| Generic ELM327 Bluetooth dongle (SPP) | Bluetooth | Most common consumer adapter type and simple pairing flow | Android Bluetooth permission handling and clone variability |
| ELM327 USB cable/dongle connected to phone/tablet (OTG) | USB cable | Stable serial channel with deterministic physical link | USB host permission flow, detach events, chipset differences |
| ELM327 WiFi adapter exposing local AP/TCP endpoint | WiFi | Useful when Bluetooth pairing is unavailable or unstable | Network switching, cleartext policy, socket reconnection |

### Typical Adapter Profiles

Bluetooth profile:
- Usually exposes Serial Port Profile (SPP / RFCOMM).
- Common channel UUID: `00001101-0000-1000-8000-00805F9B34FB`.

USB cable profile:
- Usually serial bridge over USB Host (for example CH340, CP210x, FTDI, PL2303 families).
- Requires explicit permission grant and robust detach handling.

WiFi profile:
- Usually local AP + TCP socket bridge.
- Common default endpoint pattern is local LAN on adapter-defined host/port (often port `35000`).

### Decision Rules for Support

1. If the adapter is only visible in Bluetooth settings, use Bluetooth transport.
2. If the adapter is physically attached over OTG/cable and appears as USB device, use USB transport.
3. If the adapter provides its own WiFi AP and companion apps use TCP, use WiFi transport.
4. If one transport fails repeatedly on a known-clone adapter, keep the same ELM327 session rules and retry via another available transport.

### Cross-Transport Fallback Strategy

When a user reports unreliable behavior:
- Verify adapter identity with `ATI` and active protocol with `ATDP`.
- Re-run the same startup sequence (`ATZ`, `ATE0`, `ATL0`, `ATS0`, `ATH0`, `ATSP0`).
- Keep identical timeout/retry policy while changing only transport.
- Capture logs with transport type and normalized error class for parity review.

This keeps diagnosis comparable and avoids introducing protocol-level differences while switching media.

## Troubleshooting by Symptom

Use this section as a quick field-support runbook. Keep read-only constraints in place during all checks.

### Symptom: No prompt (`>`) after initialization command

Common causes:
- Wrong transport stream configuration.
- Adapter not fully ready after reset.
- Line termination mismatch.

Actions:
1. Confirm command termination uses carriage return (`\r`).
2. Send `ATZ`, wait for startup banner, then retry `ATE0`.
3. Increase first-command timeout only for initialization phase.
4. Validate raw RX bytes before normalization to confirm data is arriving.

Transport hints:
- Bluetooth: verify RFCOMM socket is connected before first write.
- USB cable: verify endpoint/interface open state and permission still granted.
- WiFi: verify socket connected to adapter endpoint and not redirected by network switch.

### Symptom: `NO DATA` for known-good PID

Common causes:
- Vehicle ignition/session state not ready.
- Incorrect protocol selection.
- PID unsupported on this ECU.

Actions:
1. Run `ATDP` and capture the detected protocol.
2. Retry with `ATSP0` auto protocol and re-check.
3. Test a known baseline PID first (for example, supported mode 01 baseline in your flow).
4. Keep response classification explicit: unsupported PID vs transport failure.

### Symptom: `UNABLE TO CONNECT` or repeated `ERROR`

Common causes:
- Adapter clone instability.
- Protocol negotiation failure.
- Link-layer instability under load.

Actions:
1. Re-run initialization sequence from clean session (`ATZ` then setup commands).
2. Reduce request cadence temporarily and re-check stability.
3. Keep retries bounded and deterministic.
4. Capture adapter identity with `ATI` for issue clustering.

### Symptom: Frequent timeouts after a few successful requests

Common causes:
- Buffer pressure or delayed prompt delivery.
- Intermittent transport drops.
- Polling interval too aggressive.

Actions:
1. Add a small inter-command delay for diagnostic sampling path.
2. Confirm reader consumes until prompt (`>`) and does not leave residual bytes.
3. Enforce reconnect after consecutive timeout threshold.
4. Log command latency histogram by transport.

### Symptom: Bluetooth pairs but does not exchange data

Common causes:
- Wrong UUID/socket path.
- Permission issue on Android 12+.
- Adapter accepts pairing but rejects active session.

Actions:
1. Validate SPP UUID `00001101-0000-1000-8000-00805F9B34FB` path.
2. Re-check runtime permissions for Bluetooth connect operations.
3. Close and recreate socket/session fully after pairing success.
4. Validate with `ATI` immediately after connection.

### Symptom: USB adapter detected but commands fail immediately

Common causes:
- Permission revoked/expired.
- Wrong interface or endpoint selection.
- Serial bridge parameter mismatch.

Actions:
1. Re-confirm USB permission token before each open.
2. Verify interface/endpoints and direction mapping.
3. Reopen channel after detach/attach events.
4. Keep detach handling explicit and testable.

### Symptom: WiFi adapter connects but requests are unstable

Common causes:
- AP handoff or network switch.
- Socket idle timeout.
- Cleartext/network policy mismatch.

Actions:
1. Pin connection to adapter network for session duration when possible.
2. Recreate socket on network-change callbacks.
3. Verify cleartext policy for local adapter endpoint.
4. Keep identical ELM327 command sequence and compare logs against Bluetooth/USB runs.

### Escalation Criteria

Escalate as parity blocker when any of the following is true:
- Same symptom reproduced across two different adapters on same transport.
- Same adapter fails identically across two transports.
- Symptom persists after clean initialization + bounded retry policy.

Escalation package should include:
- Transport type and adapter identity (`ATI`).
- Initialization transcript (sanitized).
- Normalized error class and timeout values.
- Reference to parity evidence files/paths.

## L1/L2 Operational Checklist

Use this checklist for fast, repeatable field diagnosis without changing read-only safety constraints.

### L1 (First Response, Target: 10-15 minutes)

1. Collect context
- Adapter type reported by user: Bluetooth, USB cable, or WiFi.
- Device model and Android version.
- Symptom class from this guide.

2. Validate transport prerequisites
- Bluetooth: pairing state, required Bluetooth permissions.
- USB cable: OTG path, USB permission grant, device attach visibility.
- WiFi: connection to adapter AP/network and reachable endpoint.

3. Run minimal session probe
- Execute `ATZ` then `ATI`.
- Execute init sequence: `ATE0`, `ATL0`, `ATS0`, `ATH0`, `ATSP0`.
- Execute one known baseline read-only PID request.

4. Classify outcome
- Success: connection and one PID response are stable.
- Soft failure: recoverable timeout/token error with retry path.
- Hard failure: no prompt/data path after deterministic retries.

5. Apply one fallback attempt
- Keep command sequence and timeouts identical.
- Change only transport if user has another adapter path available.

L1 exit criteria:
- Resolved with reproducible steps, or
- Escalated to L2 with required evidence bundle.

### L2 (Deep Analysis, Target: 30-60 minutes)

1. Reproduce with deterministic runbook
- Repeat exact L1 steps with command-level timing capture.
- Verify prompt handling and residual buffer behavior.

2. Compare by transport
- Run same probe across available transports for same adapter/device.
- Confirm whether failure is transport-specific or adapter-wide.

3. Validate parser and normalization assumptions
- Check echo/spacing/header normalization pipeline.
- Confirm token classification (`NO DATA`, `ERROR`, `UNABLE TO CONNECT`, timeout).

4. Confirm boundary behavior
- Mid-request disconnect handling.
- Retry cap and reconnect trigger thresholds.
- No write/flash operation exposure.

5. Produce escalation report
- Root-cause hypothesis (transport, adapter, protocol, or parser).
- Reproducibility rating (always/intermittent/not reproducible).
- Proposed fix scope and rollback notes.

### Evidence Bundle Template

Include these artifacts in every unresolved case:
- Session metadata: timestamp, app variant, transport type.
- Adapter metadata: `ATI`, detected protocol (`ATDP` if available).
- Command transcript: sanitized request/response timeline.
- Error summary: normalized class, timeout values, retry count.
- Parity references: links/paths to captured logs and observed traces.

### SLA Guidance (Internal)

- L1 acknowledgement: under 15 minutes.
- L1 to L2 handoff when unresolved: under 20 minutes.
- L2 first technical assessment: under 60 minutes.
- If parity blocker persists, log as continuity blocker in sprint/next-actions docs.
