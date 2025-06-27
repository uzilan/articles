FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew npmInstall npm_run_build shadowJar && ./gradlew --no-daemon playwrightInstallDeps

CMD ["java", "-jar", "build/libs/articles.jar"]
