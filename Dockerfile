# 1: Build (full JDK + Maven)
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
# tests already ran in the Jenkins 'Test' stage
RUN ./mvnw clean package -DskipTests

# 2: Runtime (JRE only, smaller image)
FROM eclipse-temurin:25-jre
WORKDIR /app
# unprivileged user: a compromised app does not get root inside the container
RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 7772
ENTRYPOINT ["java", "-jar", "app.jar"]
