# Multi-stage build for Customer Service
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
COPY microservices/customer-service/pom.xml /app/microservices/customer-service/
COPY microservices/customer-service/src /app/microservices/customer-service/src
# Dependency'leri indir ve build et
RUN mvn dependency:go-offline -f /app/microservices/customer-service/pom.xml -B
RUN mvn clean package -f /app/microservices/customer-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/microservices/customer-service/target/*.jar app.jar
EXPOSE 9017
ENTRYPOINT ["java", "-jar", "app.jar"]

