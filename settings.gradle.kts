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
        maven { url = uri("https://www.crosswire.org/maven/") }
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "BibliaKoine"
include(":app")
include(":benchmark")
