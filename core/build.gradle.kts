plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.jetbrains.annotation)
}

tasks.test {
    useJUnitPlatform()
}
