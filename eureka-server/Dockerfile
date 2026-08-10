FROM eclipse-temurin:21-jdk
#LABEL authors="Mohammad Momensafaie"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
