FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

COPY ress-srv/target/ress-srv-*.jar app.jar

EXPOSE 8009

ENTRYPOINT ["java", "-jar", "app.jar"]
