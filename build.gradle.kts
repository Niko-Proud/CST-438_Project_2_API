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
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	// Validates Google OAuth2 bearer JWTs before protected controllers run.
	// The frontend obtains the token; this backend acts as the OAuth2 resource
	// server and checks the token's signature, issuer, and timestamps.
	// Official documentation:
	// https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	// Supplies Spring Data JPA, Hibernate, and Spring's database integration.
	// Official documentation:
	// https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.jpa-and-spring-data
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Provides the PostgreSQL JDBC driver at runtime. The actual connection URL,
	// username, and password are supplied by Compose or the deployment host.
	// Official documentation: https://jdbc.postgresql.org/documentation/use/
	runtimeOnly("org.postgresql:postgresql")

	// Enables Spring Boot's Flyway auto-configuration. At startup, Flyway
	// discovers versioned migrations such as V1__initial_schema.sql under
	// src/main/resources/db/migration and applies them to the configured
	// database before JPA accesses the tables.
	// Official Spring Boot documentation:
	// https://docs.spring.io/spring-boot/how-to/data-initialization.html#howto.data-initialization.migration
	implementation("org.springframework.boot:spring-boot-starter-flyway")

	// PostgreSQL support is a separate Flyway module and is required for Flyway
	// to understand and migrate PostgreSQL databases.
	// Official documentation:
	// https://documentation.red-gate.com/fd/postgresql-database-277579325.html
	implementation("org.flywaydb:flyway-database-postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

	// Supplies MockMvc request helpers such as jwt() for testing authenticated
	// requests without contacting Google. These tokens are test identities only.
	// Official documentation:
	// https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/jwt.html
	testImplementation("org.springframework.security:spring-security-test")

	// Connects Spring Boot's test support to Testcontainers-managed services.
	// Official documentation:
	// https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	// Imports one consistent Testcontainers version for all test modules below.
	// Official Gradle documentation:
	// https://github.com/testcontainers/testcontainers-java/blob/main/docs/index.md
	testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))

	// Provides JUnit 5 lifecycle support and the PostgreSQL container module.
	// These containers are temporary test infrastructure, not the development
	// database created by docker-compose.yml.
	// Official PostgreSQL module documentation:
	// https://java.testcontainers.org/modules/databases/postgres/
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
