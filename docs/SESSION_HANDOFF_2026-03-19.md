# Session Handoff - 2026-03-19

## Scope Delivered

This session completed two read-only UX safety increments in `app`:
- read-only action availability gating (disable DTC/telemetry actions until identification succeeds), and
- Bluetooth transport runtime-permission precheck gating before identification/DTC/telemetry execution.

Both increments preserve existing deterministic preflight checks as a second barrier.

## Code Changes

App runtime behavior:
- `app/src/main/java/com/ecuforge/app/MainActivity.kt`
  - Added action-availability rendering based on identification success.
  - Added transport-permission precheck before read-only actions.
  - Requests missing runtime permissions and maps deterministic precheck errors.
- `app/src/main/java/com/ecuforge/app/ReadOnlyActionAvailability.kt`
  - New pure policy object for DTC/telemetry enabled state.
- `app/src/main/java/com/ecuforge/app/ReadOnlyTransportPermissionPolicy.kt`
  - New pure policy object for Bluetooth permission requirements by SDK.
- `app/src/main/res/values/strings.xml`
  - Added transport permission precheck and permission-label UX copy.
- `app/src/main/AndroidManifest.xml`
  - Added Bluetooth and legacy location permission declarations.

Tests:
- `app/src/test/java/com/ecuforge/app/ReadOnlyActionAvailabilityTest.kt`
- `app/src/test/java/com/ecuforge/app/ReadOnlyTransportPermissionPolicyTest.kt`

## Validation Evidence

Executed during session:

```powershell
Set-Location "C:\Users\thean\Repos\ecu-forge-app"
.\gradlew.bat :app:testDebugUnitTest --tests "*ReadOnlyActionAvailabilityTest"
.\gradlew.bat :app:testDebugUnitTest --tests "*ReadOnlyTransportPermissionPolicyTest" --tests "*ReadOnlyActionAvailabilityTest"
```

Observed result:
- Both test runs completed with `BUILD SUCCESSFUL`.

## Risks / Known Gaps

- Runtime permission request result flow is not yet explicitly handled in UX (`onRequestPermissionsResult` path for deny/permanently-deny messaging).
- Bluetooth parity/hardware validation evidence remains pending for broader tuple promotion.
- Existing Gradle deprecation warnings remain and should be addressed incrementally.

## Recommended First Task Tomorrow

Implement permission-result UX completion for Bluetooth read-only flow:
1. Handle permission callback outcomes (granted/denied/permanently denied).
2. Show deterministic user guidance for denied-permanently path (open app settings).
3. Add pure policy tests for permission outcome-to-UX mapping.
4. Re-run targeted tests and then app module unit tests.

## Resume Commands

```powershell
Set-Location "C:\Users\thean\Repos\ecu-forge-app"
git --no-pager status --short
.\gradlew.bat :app:testDebugUnitTest --tests "*ReadOnlyTransportPermissionPolicyTest" --tests "*ReadOnlyActionAvailabilityTest"
```

## Commit Intent

Suggested commit split:
- `feat: gate read-only actions and transport permissions`
- `docs: update roadmap status next actions and session handoff`

