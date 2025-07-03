import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("kapt") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0-beta17"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"

    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.reposilite.com/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("articles")
    archiveClassifier.set("")
    archiveVersion.set("")

    manifest {
        attributes["Main-Class"] = "articles.Main"
    }

    exclude("**/ui/node_modules/**")
    exclude("**/node_modules/**")
    exclude("**/ui/build/**")
    exclude("**/.cache/**")
    exclude("**/.bin/**")
//    exclude("**/driver/linux/**")
//    exclude("**/driver/linux-arm64/**")
    exclude("**/driver/mac/**")
//    exclude("**/driver/mac-arm64/**")
    exclude("**/driver/win32_x64/**")

    from("ui/dist") {
        into("public")
        exclude("**/node_modules/**")
        exclude("**/*.map")
    }
}

dependencies {
    // Javalin and OpenAPI
    implementation("io.javalin:javalin:6.6.0")
    val openapi = "6.7.0"
    annotationProcessor("io.javalin.community.openapi:openapi-annotation-processor:$openapi")
    kapt("io.javalin.community.openapi:openapi-annotation-processor:$openapi")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:$openapi")
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:$openapi")
    implementation("io.javalin.community.openapi:javalin-redoc-plugin:$openapi")

    // DJL and Machine Learning
    implementation(platform("ai.djl:bom:0.33.0"))
    implementation("ai.djl:api")
    implementation("ai.djl.pytorch:pytorch-engine")
    implementation("ai.djl.huggingface:tokenizers")

    // Playwright and scraping
    implementation("com.microsoft.playwright:playwright:1.52.0")

    // Jackson and serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")

    // Logging and caching
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("io.mockk:mockk:1.13.10")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("articles.Main")
}
