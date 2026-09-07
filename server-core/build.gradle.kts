plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.22"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")

//    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    implementation("org.springframework:spring-websocket:7.0.0-M9")
    implementation("org.springframework:spring-messaging:7.0.0-M9")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")

    implementation("jakarta.websocket:jakarta.websocket-api:2.2.0")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.2.0")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}