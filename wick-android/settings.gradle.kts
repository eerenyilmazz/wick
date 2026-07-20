pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Wick"

include(":app")
include(":core:designsystem")
include(":core:model")
include(":core:network")
include(":core:data")
include(":core:billing")
include(":feature:auth")
include(":feature:profile")
include(":feature:discover")
include(":feature:matches")
include(":feature:chat")
