# Multi-stage build for Config Server
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Maven'i yükle
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
# Parent POM'u kopyala ve install et
COPY pom-parent.xml /app/pom-parent.xml
RUN mvn install -N -f /app/pom-parent.xml -B
# Child POM ve source'ları kopyala
COPY config-server/pom.xml /app/config-server/
COPY config-server/src /app/config-server/src
# Build et (dependency'ler otomatik indirilir)
RUN mvn clean package -f /app/config-server/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]

