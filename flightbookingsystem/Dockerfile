FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B -f ./pom.xml -pl flightbookingsystem -am -DskipTests package
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/flightbookingsystem/target/*.jar ./app.jar
EXPOSE 8210
ENTRYPOINT ["java","-jar","/app/app.jar"]
