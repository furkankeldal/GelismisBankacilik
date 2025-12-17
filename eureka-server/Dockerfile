# Multi-stage build for Eureka Server
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
COPY eureka-server/pom.xml /app/eureka-server/
COPY eureka-server/src /app/eureka-server/src
# Dependency'leri indir ve build et
RUN mvn dependency:go-offline -f /app/eureka-server/pom.xml -B
RUN mvn clean package -f /app/eureka-server/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/eureka-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]

