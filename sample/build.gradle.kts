plugins {
    id("application")
    alias(libs.plugins.kotlin.jvm)
}

application {
    mainClass.set("com.seo4d696b75.diagram.sample.MainKt")
}

dependencies {
    implementation(libs.diagram.station)

    // when only core impl needed
    // implementation(libs.diagram.core)
}
