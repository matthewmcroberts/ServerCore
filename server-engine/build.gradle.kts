plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.1.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(project(":server-core"))

    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }
}