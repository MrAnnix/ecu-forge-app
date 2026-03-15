# Contribution Workflow Manual

This manual defines the expected path from issue to merge.

## 1. Define the Work

- Describe the problem or enhancement clearly.
- State expected behavior and constraints.
- For medium/large work, align scope before coding.

Scope alignment should define:

- In-scope items.
- Out-of-scope items.
- Acceptance criteria.

## 2. Implement with High Standards

Implementation must follow project quality rules:

- Deliver functional, production-ready code.
- Use meaningful names and clear method/class responsibilities.
- Avoid spaghetti structures and unnecessary imperative complexity.
- Keep side effects explicit and localized.
- Include validation and predictable error handling.

## 3. Test Behavior, Not Only Coverage

Before opening a PR:

- Add/update unit tests for every behavior change.
- Validate business rules and realistic failure modes.
- Add negative-path and boundary-condition tests where relevant.
- Ensure tests are deterministic and isolated.

Coverage can support confidence, but it does not replace behavior verification.

## 4. Open the Pull Request

PR descriptions must include:

- Problem statement.
- Scope (in/out).
- Risk assessment.
- Validation evidence.
- Rollback strategy for risky changes.

For transport changes:

- Add reproducible verification steps.

For map/write changes:

- Add rollback and failure-mode notes.

## 5. Review and Iterate

- Address review comments with focused follow-up commits.
- Do not introduce unrelated refactors during review.
- Keep discussion centered on behavior, safety, and maintainability.

## 6. Merge Gate

A PR is merge-ready only when:

- Required checks pass.
- Reviewer concerns are resolved.
- Documentation is updated when behavior changed.
- Risky-path validation evidence is present.

## 7. Post-Merge Follow-Up

- Confirm acceptance criteria were met.
- Monitor for regressions.
- Open follow-up issues for deferred work.

## Definition of Done

A contribution is complete when:

- Functional behavior is correct.
- Code quality standards are met.
- Tests validate real behavior and failure modes.
- Documentation and risk notes are complete.
