# Dockerfile per Spring Boot (JAR)
FROM eclipse-temurin:17-jre-alpine

# Cartella di lavoro
WORKDIR /app

# Copia il jar generato da Maven
COPY target/demo-mail-0.0.1-SNAPSHOT.jar app.jar

# Espone la porta 8080 (default Spring Boot)
EXPOSE 8080

# Comando di avvio
ENTRYPOINT ["java", "-jar", "app.jar"]
