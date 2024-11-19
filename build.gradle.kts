import org.jetbrains.kotlin.com.google.gson.internal.bind.util.ISO8601Utils
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import java.util.Date

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

repositories {
    mavenCentral()
    mavenLocal()
}

configure(subprojects - project(":common")) {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    tasks.withType<BootBuildImage> {
        imageName = "${rootProject.name.lowercase()}/${project.name}:${properties["pyraxbot.version"]}"
        createdDate = ISO8601Utils.format(Date())
        environment.putAll(mapOf(
            "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
            "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:MaxDirectMemorySize=1000M"
        ))
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")

    repositories {
        mavenCentral()
        mavenLocal()

    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:${properties["kotlin.version"]}")

        implementation("org.springframework.boot:spring-boot-starter:${properties["springboot.version"]}")
        implementation("com.discord4j:discord4j-core:${properties["discord4j.version"]}")
        implementation("com.discord4j:connect-common:${properties["discord4j.connect.version"]}")
        implementation("com.discord4j:connect-rabbitmq:${properties["discord4j.connect.version"]}")
        implementation("com.discord4j:connect-rsocket:${properties["discord4j.connect.version"]}") {
            exclude(group = "io.rsocket", module = "rsocket-core")
            exclude(group = "io.rsocket", module = "rsocket-transport-netty")
        }
        implementation("io.rsocket:rsocket-core:${properties["rsocket.version"]}")
        implementation("io.rsocket:rsocket-transport-netty:${properties["rsocket.version"]}")
        implementation("com.discord4j:stores-redis:${properties["discord4j.stores.version"]}") {
            exclude(group = "io.lettuce", module = "lettuce-core")
        }
        implementation("io.lettuce:lettuce-core:${properties["lettuce.version"]}")
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }
}