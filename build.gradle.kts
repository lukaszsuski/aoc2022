plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.22"
    id("application")
}

repositories {
    mavenCentral()
    mavenLocal()
}

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs = listOf("-Xss2m")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}