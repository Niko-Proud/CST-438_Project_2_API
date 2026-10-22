plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	pmd
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

pmd {
	isConsoleOutput = true
	toolVersion = "7.16.0"
	rulesMinimumPriority = 5
	ruleSets = listOf("category/java/errorprone.xml", "category/java/bestpractices.xml")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	// Note: spring-boot-starter-webmvc was removed as it is already included transitively by spring-boot-starter-web.

	// Validates Google OAuth2 bearer JWTs before protected controllers run.
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	// Supplies Spring Data JPA, Hibernate, and Spring's database integration.
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Enables Spring Boot's Flyway auto-configuration.
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")

	// Provides the PostgreSQL JDBC driver at runtime.
	runtimeOnly("org.postgresql:postgresql")

	// Tests
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("io.rest-assured:rest-assured:5.4.0")
	testImplementation("org.springframework.security:spring-security-test")

	// Testcontainers
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
	// The BOM supplies the versions for Testcontainers' JUnit and PostgreSQL modules.
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Setting this up so that theres one consistent call for Docker, as outlined in the tutorial/guide
tasks.bootJar {
	archiveFileName = "brain-worm.jar"
}
