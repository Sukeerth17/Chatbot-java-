FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S chatbot && adduser -S chatbot -G chatbot
USER chatbot
WORKDIR /app
COPY --from=build /app/target/*.jar chatbot.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -q -O- http://localhost:8085/health || exit 1
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "chatbot.jar"]
