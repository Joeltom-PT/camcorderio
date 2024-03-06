FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/Camcorderio-0.0.1-SNAPSHOT.jar /app/Camcorderio.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "Camcorderio.jar"]
