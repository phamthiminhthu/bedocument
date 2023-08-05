FROM eclipse-temurin:17-jdk-jammy

EXPOSE 80
ADD target/beDocuments-0.0.1-SNAPSHOT.jar app.jar
COPY .env ./
ENTRYPOINT ["java", "-jar","/app.jar"]