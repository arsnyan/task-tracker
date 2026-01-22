FROM eclipse-temurin:21-jre-alpine
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Xms64m", "-Xmx256m", "-jar", "app.jar"]