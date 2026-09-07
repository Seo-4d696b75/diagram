plugins {
    id("maven-publish")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.github.seo4d696b75"

version = System.getenv("GRADLE_PUBLISH_VERSION") ?: "1.0-SNAPSHOT"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Seo-4d696b75/diagram")
            credentials {
                username = System.getenv("GITHUB_PACKAGE_USERNAME")
                password = System.getenv("GITHUB_PACKAGE_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation(libs.jetbrains.annotation)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)
}

sourceSets {
    getByName("main") {
        kotlin.setSrcDirs(listOf("app/src/main", "core/src/main"))
    }
}

kotlin {
    jvmToolchain(17)
}
