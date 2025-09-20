# Verwende ein offizielles Java 17 Image (passt zu Spring Boot 3)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/LibroCatalogueApplication.jar app.jar
ENTRYPOINT ["java", "-Xmx128m","-Xss512k", "-jar", "app.jar"]