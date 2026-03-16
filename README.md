# ecu-forge-app

ECU Forge is an open-source Android app for motorcycle ECU diagnostics, real-time data monitoring, and map management, designed to be modern, maintainable, and community-extensible.

## Project Continuity Repository

This repository is the continuity base for the Android Tune eCU app.
The goal is to centralize architecture decisions, implementation milestones, and source code evolution in a clean and auditable way.

## Vision

- Reliable diagnostics and telemetry for supported motorcycles.
- Safe and traceable map handling workflows.
- Modular Android codebase that is easy to maintain and extend.
- Community-driven evolution with clear contribution standards.

## Core Functional Areas

- ECU communication layer (protocol abstraction and transport adapters).
- Real-time data dashboard and logging.
- Read/Write and backup map management flows.
- Diagnostic trouble code (DTC) reading and clearing.
- Device/profile management and session history.

## Technical Direction

- Kotlin-first Android architecture.
- Clear separation of data, domain, and presentation layers.
- Testable modules and CI-friendly project structure.
- Explicit compatibility matrix per bike model/ECU family.

## Initial Roadmap

The canonical roadmap is maintained in:

- [ECU Forge Roadmap](docs/ROADMAP.md)

## Working Agreement

- Use feature branches and small pull requests.
- Document design-impacting decisions in PR descriptions.
- Keep commits focused and reversible.
- Add tests for critical behavior changes when possible.

## Android Bootstrap Status

The repository now includes a baseline Android project with:

- Gradle Kotlin DSL setup.
- `app` module with package `com.ecuforge.app`.
- Starter `MainActivity` and XML layout.
- Basic unit and instrumentation test templates.

## Quick Start

1. Open the repository in Android Studio.
2. Let Android Studio sync Gradle and install required SDK components.
3. Run the `app` configuration on an emulator/device.

## Next Steps

1. Define module boundaries (`app`, `core`, `ecu`, `feature-*`).
2. Add Gradle wrapper files (`gradlew`, `gradlew.bat`, and `gradle/wrapper/*`) from Android Studio sync.
3. Add static analysis, formatting, and CI checks.
4. Create an implementation board from the roadmap.

## Contributing Manuals

- [Contributing Guide](CONTRIBUTING.md)
- [Contributor Rights Assignment](docs/CONTRIBUTOR_RIGHTS_ASSIGNMENT.md)
- [Contribution Workflow Manual](docs/CONTRIBUTION_WORKFLOW.md)
- [Roadmap](docs/ROADMAP.md)
- [Next Actions](docs/NEXT_ACTIONS.md)
- [Project Status](docs/PROJECT_STATUS.md)
- [Module Dependency Rules](docs/MODULE_DEPENDENCY_RULES.md)

## Agent Working Contract

- [Agent Operating Rules](AGENTS.md)

## License

This project is licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later).

Important contributor policy:
- Contributions are accepted only after signing the contributor rights assignment in [docs/CONTRIBUTOR_RIGHTS_ASSIGNMENT.md](docs/CONTRIBUTOR_RIGHTS_ASSIGNMENT.md).
