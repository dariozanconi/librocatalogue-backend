# Verwende ein offizielles Java 17 Image (passt zu Spring Boot 3)
FROM eclipse-temurin:21-jdk

# Setze das Arbeitsverzeichnis
WORKDIR /app

# Kopiere dein Jar in das Image
COPY target/LibroCatalogueApplication.jar app.jar

# Startkommando
ENTRYPOINT ["java", "-Xmx128m","-Xss512k", "-jar", "app.jar"]