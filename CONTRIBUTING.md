# Contributing to ECU Forge

Thank you for contributing to ECU Forge.
This repository continues the TuneECU Android path with a strict focus on safety, maintainability, and production-ready code.

## Core Contribution Standard

All submitted code must be:

- Functional and runnable in the intended flow.
- Production-ready (no placeholder paths or partial implementations).
- Structured with clear responsibilities (no spaghetti code).
- Written with meaningful names for variables, methods, and classes.

## Coding Expectations

- Prefer functional and declarative style where possible.
- Avoid unnecessary loops and deeply nested conditionals.
- Keep methods short, focused, and single-purpose.
- Keep side effects explicit and localized.
- Prefer immutability and avoid broad shared mutable state.
- Add concise comments only for non-obvious logic.

## Validation and Error Handling

- Every feature must include explicit input validation.
- Error handling must be predictable and behaviorally clear.
- Public APIs should express intent via clear names and parameters.

## Testing Policy

Testing is mandatory for behavior changes.

- Add or update unit tests for every behavior change.
- Validate business behavior, rules, and failure modes.
- Include negative-path and boundary-condition tests for critical logic.
- Keep tests deterministic, isolated, and readable.
- Do not treat coverage alone as proof of quality.

For transport-related changes:

- Include a reproducible verification plan.

For map/write-related changes:

- Include rollback notes and failure-mode validation.

## Branch and Commit Conventions

Branch naming:

- `feature/<topic>`
- `fix/<topic>`
- `chore/<topic>`
- `docs/<topic>`

Commit style:

- `<type>: <short summary>`
- Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`

## Pull Request Requirements

Every PR must include:

- Problem statement.
- Scope (in/out).
- Risk assessment.
- Validation evidence.
- Rollback strategy for risky changes.

## Security and Data Handling

- Never commit secrets, personal data, or proprietary ECU dumps.
- Treat diagnostics and map files as sensitive artifacts.
- Document any new permission usage and the rationale.

## Need Alignment Before Coding?

For medium/large changes, open an issue or draft PR first to align scope and acceptance criteria before implementation.
