FROM mcr.microsoft.com/playwright/java:v1.50.0-noble

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y npm python3 python3-pip python3-venv

# Create and activate virtual environment
RUN python3 -m venv /venv
ENV PATH="/venv/bin:$PATH"

# Install PyTorch and transformers in virtual environment
RUN pip install --upgrade pip && pip install torch transformers

WORKDIR /app/ui
RUN npm i && npm run build

WORKDIR /app
RUN ./gradlew -i shadowJar 

CMD ["java", "-jar", "build/libs/articles.jar"]
