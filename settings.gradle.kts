rootProject.name = "PyraxBot"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                gradle.rootProject.extra["kotlin.version"]?.let { useVersion(it as String) }
            } else if (requested.id.id.startsWith("org.springframework.boot")) {
                gradle.rootProject.extra["springboot.version"]?.let { useVersion(it as String) }
            }
        }
    }
}

include("common", "leader", "worker", "globalrouter", "shardcoordinator")