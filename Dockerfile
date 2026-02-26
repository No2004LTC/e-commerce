# Multi-stage Dockerfile: build with Maven, run with JRE 17
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
# Install maven to run the build (we'll use apt to install maven inside this stage)
RUN microdnf install -y maven || dnf install -y maven || apk add --no-cache maven || true
# copy mvnw and maven wrapper metadata first for better cache
COPY mvnw pom.xml ./
COPY .mvn .mvn
# copy sources
COPY src ./src


# Ensure wrapper is executable and build the project
RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
