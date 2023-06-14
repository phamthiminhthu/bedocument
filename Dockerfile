FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/beDocuments-0.0.1-SNAPSHOT.jar app.jar
COPY .env ./
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
