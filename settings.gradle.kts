rootProject.name = "diagram"
include(":station")
include(":core")
include(":sample")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            // only for snapshots while development
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}
