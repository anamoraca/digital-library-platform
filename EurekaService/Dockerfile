# ---------- build stage ----------
FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /workspace/app

# prvo sve (ako hoćeš, možeš optimizovati sa dependency:go-offline kasnije)
COPY . .
RUN mvn -B -DskipTests install

# raspakuj jar da dobiješ BOOT-INF/ i META-INF/
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre

# treba nam wget za healthcheck iz docker-compose-a
RUN apt-get update \
 && apt-get install -y --no-install-recommends bash  wget \
 && rm -rf /var/lib/apt/lists/*

VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8761
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "rs.ac.uns.acs.nais.EurekaService.EurekaServiceApplication"]
