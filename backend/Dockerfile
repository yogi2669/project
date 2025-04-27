# Use official Maven image to build the app

FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .  # This copies everything into the /app directory
RUN mvn clean package -DskipTests  # Builds the app using Maven

# Use a lighter image to run the app
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar  # Copies the built JAR from the Maven build
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]  # Runs the app when the container starts
