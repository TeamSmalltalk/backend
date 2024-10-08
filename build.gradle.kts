import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.1"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.21"
	kotlin("plugin.spring") version "1.9.21"
	kotlin("plugin.serialization") version "2.0.20"
}

group = "smalltalk"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val kotlinLoggingVersion = "6.0.3"
val coroutinesVersion = "1.8.0-RC2"
val kotestRunnerVersion = "5.9.1"
val kotestExtensionsVersion = "1.3.0"
val mockkVersion = "4.0.2"
val krossbowVersion = "7.0.0"
val standaloneClientVersion = "2.1.5"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
	implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")
	implementation("org.hildan.krossbow:krossbow-stomp-kxserialization:$krossbowVersion")
	implementation("org.hildan.krossbow:krossbow-stomp-kxserialization-json:$krossbowVersion")
	implementation("org.hildan.krossbow:krossbow-websocket-spring:$krossbowVersion")
	implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client-jdk:$standaloneClientVersion")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("io.kotest:kotest-runner-junit5:$kotestRunnerVersion")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestExtensionsVersion")
	testImplementation("com.ninja-squad:springmockk:$mockkVersion")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
