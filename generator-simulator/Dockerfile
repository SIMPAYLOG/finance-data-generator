FROM eclipse-temurin:21-jre
WORKDIR /app

# Gradle 빌드 후 jar 파일 복사
COPY build/libs/generator-simulator.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar","--spring.profiles.active=demo"]