# Module Dependency Rules

## Purpose

Define allowed dependency directions to keep architecture maintainable and prevent coupling drift.

## Modules

- `:app`
- `:core`
- `:transport`
- `:feature-diagnostics`
- `:feature-telemetry`
- `:feature-map`

## Allowed Dependencies

- `:app` -> `:feature-diagnostics`, `:feature-telemetry`, `:feature-map`
- `:feature-diagnostics` -> `:core`, `:transport`
- `:feature-telemetry` -> `:core`, `:transport`
- `:feature-map` -> `:core`, `:transport`
- `:transport` -> `:core`
- `:core` -> no internal project dependencies

## Forbidden Dependencies

- Features must not depend directly on `:app`.
- `:core` must not depend on feature or app modules.
- `:transport` must not depend on feature or app modules.
- Feature modules must not depend on each other.
- UI code in `:app` must not import transport implementation details directly.

## Architectural Notes

- Keep transport implementation details behind interfaces.
- Keep domain contracts stable in `:core` and/or dedicated domain packages.
- Feature modules own presentation/domain integration for their use cases.

## Review Checklist

For every PR affecting module dependencies:
- Verify no new forbidden project dependency was introduced.
- Verify dependency change is reflected in this document.
- Verify rationale is included in PR risk/scope notes.

## Next Enforcement Step

Add a CI check to parse Gradle project dependencies and fail on forbidden edges.
