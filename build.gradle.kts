java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.example"
version = "1.0-SNAPSHOT"

val junitBomVersion = "6.0.0"
val assertjVersion = "3.27.3"
val vavrVersion = "0.10.6"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.junit:junit-bom:$junitBomVersion")
    }
    dependencies {
        dependency("org.assertj:assertj-core:$assertjVersion")
        dependency("io.vavr:vavr:$vavrVersion")
    }
}

dependencies {
    implementation("io.vavr:vavr")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
