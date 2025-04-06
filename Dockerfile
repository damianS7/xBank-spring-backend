# Recuerda construir el .jar antes con mvn clean package o ./mvnw clean package.
# Usa imagen oficial de Eclipse Temurin con Java 21
FROM eclipse-temurin:21-jdk-alpine

# Crea directorio para la app
WORKDIR /app

# Copia el jar generado por Spring Boot
COPY target/*.jar app.jar

# Expone el puerto en el que corre la app
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]