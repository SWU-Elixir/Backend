FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY ./ ./
RUN gradle clean -x test build

FROM openjdk:17-jdk-slim AS run
WORKDIR /app
COPY --from=build /app/build/libs/Elixir-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]