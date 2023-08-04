FROM eclipse-temurin:17-jdk-jammy

EXPOSE 8080
ADD target/beDocuments-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar","/app.jar"]