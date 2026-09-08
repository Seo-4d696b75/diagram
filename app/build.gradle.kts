plugins {
    id("java-library")
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "com.seo4d696b75.diagram",
        artifactId = "tool-station",
        version = libs.versions.publish.get(),
    )

    pom {
        name.set("2d Diagram Calculation")
        description.set("Tools to calculate delaunay and voronoi diagram of station coordinates.")
        url.set("https://github.com/Seo-4d696b75/diagram")

        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "seo4d696b75"
                name = "Seo-4d696b75"
                email = "s.kaoru509@gmail.com"
            }
        }

        scm {
            url.set("https://github.com/Seo-4d696b75/diagram")
        }
    }
}
