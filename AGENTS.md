# AGENTS.md

## Purpose

This repository hosts the new ECU Forge Android app and is the long-term continuation path for TuneECU capabilities.

Use this file as the default working contract for all coding agents and contributors.

## Product Direction

- Build a maintainable Android app for ECU diagnostics, telemetry, and map operations.
- Prioritize safety and data integrity over feature speed.
- Evolve in small, auditable increments.

## Delivery Principles

- Make small, focused changes with clear intent.
- Prefer reversible implementations and low-risk rollout paths.
- Keep behavioral changes explicit in pull request notes.

## Architecture Rules

- Kotlin-first Android code.
- Layered design: `presentation` -> `domain` -> `data`.
- Keep ECU transport logic isolated from UI.
- Add adapters for transport types (Bluetooth, USB) behind stable interfaces.
- Avoid global mutable state for session-critical behavior.

## ECU Safety Rules

- Never modify write/flash behavior without validation notes.
- Add pre-checks before write operations (connection state, compatibility, battery assumptions where available).
- Prefer backup-first workflows before destructive operations.
- Log critical ECU operations with timestamped context.

## Coding Conventions

- Use meaningful names; avoid ambiguous one-letter names.
- Keep functions short and single-purpose.
- Prefer functional and declarative style over imperative control flow.
- Avoid unnecessary loops and deeply nested conditionals; use collection operators, guards, and clear composition when possible.
- Split responsibilities into focused methods/classes to avoid spaghetti code.
- Require production-ready code paths; avoid placeholder logic and partial implementations.
- Document all production code with KDoc/JavaDoc (tests excluded).
- Document every public property (`val`/`var`) with KDoc (`@property` tags or property-level KDoc) in production code.
- Add concise comments only for non-obvious logic.
- Do not introduce unrelated refactors in a feature fix.

## Code Quality Standard

- All generated code must be functional and ready to run in the intended flow.
- Every feature must have explicit input validation and predictable error handling.
- Public methods should express intent clearly through names, parameters, and return types.
- Keep side effects explicit and localized.
- Prefer immutability and limit shared mutable state.

## Testing Requirements

- Add or update tests for every behavior change.
- Prioritize unit tests that validate real business behavior, rules, and failure modes.
- Do not rely on coverage as a success criterion by itself.
- Include negative-path tests and boundary-condition tests for critical logic.
- Keep tests deterministic, isolated, and readable.
- Use AssertJ assertions only in tests; do not use JUnit assertions, Kotlin test assertions, or Truth.
- Prefer verbose AssertJ assertions with `describedAs(...)` so each validation intent is explicit.
- For transport changes, include a reproducible verification plan.
- For map/write paths, include rollback and failure-mode notes.

## Pull Request Requirements

Every PR should include:

- Problem statement.
- Scope (in/out).
- Risk assessment.
- Validation evidence.
- Rollback strategy for risky changes.

## Branch and Commit Policy

- Branch naming:
  - `feature/<topic>`
  - `fix/<topic>`
  - `chore/<topic>`
  - `docs/<topic>`
- Commit style: `<type>: <short summary>`
  - `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`

## Security and Privacy

- Never commit secrets, personal data, or proprietary ECU dumps.
- Treat diagnostics and map files as sensitive artifacts.
- Document any new permission usage and why it is required.

## Agent Workflow

When an agent works on a task, it should:

1. Confirm scope and constraints from the request.
2. Inspect relevant files and dependencies first.
3. Implement the smallest complete solution.
4. Validate changes (build/tests/lint where possible).
5. Report outcomes, risks, and next steps.

## Initial Milestones

1. Stabilize Android baseline modules (`app`, `core`, `transport`, `feature-*`).
2. Define transport contracts and session state model.
3. Implement first end-to-end read-only diagnostic flow.
4. Add telemetry logging and persistence strategy.
5. Introduce CI checks for build, tests, and static analysis.
