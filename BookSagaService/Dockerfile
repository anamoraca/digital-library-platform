# ===== Build stage =====
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Kopiraj POM pa povuci zavisnosti (cache-friendly)
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# Sada kopiraj izvorni kod i buildaj
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
