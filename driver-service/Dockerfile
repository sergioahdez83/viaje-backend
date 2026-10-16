FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/driver-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]