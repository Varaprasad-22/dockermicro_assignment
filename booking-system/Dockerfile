FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B -f ./pom.xml -pl booking-system -am -DskipTests package
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/booking-system/target/*.jar ./app.jar
EXPOSE 8211
ENTRYPOINT ["java","-jar","/app/app.jar"]
