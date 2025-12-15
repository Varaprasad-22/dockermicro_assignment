FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B -f ./pom.xml -pl api-gateways -am -DskipTests package
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/api-gateways/target/*.jar ./app.jar
EXPOSE 8212
ENTRYPOINT ["java","-jar","/app/app.jar"]
