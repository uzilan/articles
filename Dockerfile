FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update
RUN apt-get install -y libglib2.0-0t64 \
    libnss3 \
    libnspr4 \
    libdbus-1-3 \
    libatk1.0-0t64 \
    libatk-bridge2.0-0t64 \
    libatspi2.0-0t64 \
    libx11-6 \
    libxcomposite1 \
    libxdamage1 \
    libxext6 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libxcb1 \
    libxkbcommon0 \
    libasound2t64 \
    libcups2t64 \
    libpango-1.0-0 \
    libcairo2

COPY . .

RUN ./gradlew npmInstall npm_run_build shadowJar

CMD ["java", "-jar", "build/libs/articles.jar"]
