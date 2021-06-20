FROM adoptopenjdk/openjdk11:alpine-slim
EXPOSE 8080
ADD tax-calculator-api/build/libs/tax-calculator-api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
