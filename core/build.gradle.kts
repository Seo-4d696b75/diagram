plugins {
    id("java-library")
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.jetbrains.annotation)
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "com.seo4d696b75.diagram",
        artifactId = "core",
        version = libs.versions.publish.get(),
    )

    pom {
        name.set("2d Diagram Calculation")
        description.set("Simple implementations to calculate delaunay and voronoi diagram in 2-dimensional orthonormal system.")
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
