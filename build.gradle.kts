import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.21"
	kotlin("plugin.spring") version "1.4.21"
	kotlin("plugin.jpa") version "1.4.21"

    id("com.github.ben-manes.versions") version "0.36.0"
}

group = "de.digitalfrontiers.axon"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.axonframework:axon-spring-boot-starter:4.4.5")

	runtimeOnly("org.postgresql:postgresql:42.2.18")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23")
    testImplementation("org.axonframework:axon-test:4.4.5")
    testImplementation("org.testcontainers:postgresql:1.15.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

springBoot {
	buildInfo()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootBuildImage> {
    imageName = "dxfrontiers/axon-state-machine"
    targetJavaVersion.set(JavaVersion.VERSION_15)
}
