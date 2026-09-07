plugins {
    id("java")
}

dependencies {
    implementation("org.jetbrains:annotations:19.0.0")
}

tasks.test {
    useJUnitPlatform()
}
