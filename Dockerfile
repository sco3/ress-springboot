FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

COPY ress-srv/target/ress-srv-*.jar app.jar
COPY ress-srv/src/test/resources/server-keystore.jks server-keystore.jks
COPY docker/application.properties application.properties

EXPOSE 8009 8010

ENTRYPOINT ["java", \
  "-Dspring.config.location=file:/app/", \
  "-jar", "app.jar"]
