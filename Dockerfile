# File runs from top down, so stage one comes first, give build instructions
# Also grabs the Gradle version that th running machine needs compatibility wise
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /CST-438_Project_2_API
# Grab everything gradle needs to boot and load proper file dependencies,
# then give execute permissions to the copied files
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet
# Copies the src directory and sub directories, including tests!
COPY src/ src/
RUN ./gradlew bootJar --no-daemon --quiet

# Stage 2, where we run the files after building them
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /CST-438_Project_2_API
# Creates a low priveledge user and group to run, just in case someone tries to inject malicious code 
# through the app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
# Grabs the Jar and only the Jar from what was built, give permission sover it to the low privelege user & group
COPY --from=build --chown=spring:spring /CST-438_Project_2_API/build/libs/brain-worm.jar brain-worm.jar
# This doesn't do anything but give documentation for us and tools to reference where to look for things
EXPOSE 8080
# Checks and verifies that everything is working as hoped (not respected by hosts)
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health || exit 1

# Honestly, the doc around this is a bit jargony, I assume it directs Docker on where the places to look are
ENTRYPOINT ["java", "-jar", "brain-worm.jar"]