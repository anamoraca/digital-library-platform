FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /workspace/app

COPY . .
RUN mvn install -DskipTests

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8761

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "rs.ac.uns.acs.nais.EurekaService.EurekaServiceApplication"]
