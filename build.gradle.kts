plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.publish) apply false
}

sourceSets {
    getByName("main") {
        kotlin.setSrcDirs(listOf("app/src/main", "core/src/main"))
    }
}

kotlin {
    jvmToolchain(17)
}

// publish each module
tasks.register<Task>("publish") {
    dependsOn(
        "core:publishToMavenCentral",
        "app:publishToMavenCentral",
    )
}
