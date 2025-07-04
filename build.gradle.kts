import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.github.node-gradle.node") version "7.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta17"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

node {
    download = true
    version = "24.2.0"
    npmVersion = "11.3.0"
    nodeProjectDir = file("${project.projectDir}/ui")
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
    exclude("**/driver/mac-arm64/**")
    exclude("**/driver/win32_x64/**")

    from("ui/dist") {
        into("public")
        exclude("**/node_modules/**")
        exclude("**/*.map")
    }
}

tasks.register<JavaExec>("playwrightInstall") {
    group       = "playwright"
    description = "Install Linux browser dependencies for Playwright"

    // Where the CLI lives
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets["main"].runtimeClasspath

    // Pass the CLI sub-command
    args("install")
}

tasks.register<JavaExec>("playwrightInstallDeps") {
    group       = "playwright"
    description = "Install Linux browser dependencies for Playwright"

    // Where the CLI lives
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets["main"].runtimeClasspath

    // Pass the CLI sub-command
    args("install-deps")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
    implementation("com.microsoft.playwright:playwright:1.52.0")
    implementation("io.javalin:javalin:6.6.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.27.3")
}

tasks.test {
    useJUnitPlatform()
}
