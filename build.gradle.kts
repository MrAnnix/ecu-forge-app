import org.gradle.api.artifacts.ProjectDependency

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register("verifyModuleDependencyRules") {
    group = "verification"
    description = "Fails when a module depends on another module outside the allowed architecture rules."

    val allowedDependencies = mapOf(
        ":app" to setOf(":feature-diagnostics", ":feature-telemetry", ":feature-map"),
        ":feature-diagnostics" to setOf(":core", ":transport"),
        ":feature-telemetry" to setOf(":core", ":transport"),
        ":feature-map" to setOf(":core", ":transport"),
        ":transport" to setOf(":core"),
        ":core" to emptySet()
    )

    doLast {
        val declaredModules = allowedDependencies.keys
        val existingModules = subprojects.map { subproject -> subproject.path }.toSet()

        val undeclaredModules = existingModules - declaredModules
        val staleRuleEntries = declaredModules - existingModules

        if (undeclaredModules.isNotEmpty() || staleRuleEntries.isNotEmpty()) {
            val undeclaredMessage = if (undeclaredModules.isEmpty()) {
                ""
            } else {
                "Undeclared modules: ${undeclaredModules.sorted().joinToString(", ")}\n"
            }
            val staleMessage = if (staleRuleEntries.isEmpty()) {
                ""
            } else {
                "Rules for missing modules: ${staleRuleEntries.sorted().joinToString(", ")}\n"
            }

            throw GradleException(
                "Module dependency rules are out of sync.\n" +
                    undeclaredMessage +
                    staleMessage +
                    "Update verifyModuleDependencyRules in build.gradle.kts and docs/MODULE_DEPENDENCY_RULES.md"
            )
        }

        val violations = mutableListOf<String>()

        allowedDependencies.forEach { (modulePath, allowedTargets) ->
            val module = project(modulePath)
            val dependencyConfigurations = module.configurations.filter { configuration ->
                val name = configuration.name
                val isProductionConfiguration =
                    name.endsWith("Implementation") ||
                        name.endsWith("Api") ||
                        name.endsWith("CompileOnly") ||
                        name.endsWith("RuntimeOnly")

                isProductionConfiguration && !name.contains("test", ignoreCase = true)
            }

            val projectDependencies: Set<String> = dependencyConfigurations
                .flatMap { configuration ->
                    configuration.dependencies
                        .filterIsInstance<ProjectDependency>()
                        .map { dependency: ProjectDependency -> dependency.path }
                }
                .toSet()

            projectDependencies.forEach { targetPath: String ->
                if (targetPath !in allowedTargets) {
                    violations.add("$modulePath -> $targetPath is forbidden")
                }
            }
        }

        if (violations.isNotEmpty()) {
            val details = violations.sorted().joinToString(separator = "\n")
            throw GradleException(
                "Module dependency rule violations detected:\n$details\n" +
                    "See docs/MODULE_DEPENDENCY_RULES.md"
            )
        }
    }
}
