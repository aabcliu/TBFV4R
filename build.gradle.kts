import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.3"
}

group = "org.TBFV4R"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

tasks.named<JavaExec>("run") {
    jvmArgs("-Dfile.encoding=UTF-8")
}

// application {
//     mainClass.set("org.TBFV4R.Main")
// }

// tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
//     enabled = false
//     duplicatesStrategy = DuplicatesStrategy.EXCLUDE
// }

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    mainClass.set("org.TBFV4R.api.WebAPI")
    archiveFileName.set("TBFV4R-web-1.0-SNAPSHOT-web.jar")
    // 修复：为 bootJar 任务设置去重策略，以解决 MANIFEST.MF 冲突
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// shadowJar for main entry
val mainJar by tasks.registering(ShadowJar::class) {
    archiveBaseName.set("TBFV4R-main")
    archiveClassifier.set("")
    archiveVersion.set("1.0-SNAPSHOT")
    manifest {
        attributes["Main-Class"] = "org.TBFV4R.Main"
    }
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// val webJar by tasks.registering(ShadowJar::class) { ... }

// build task depends on bootJar and mainJar
tasks.named("build") {
    dependsOn(mainJar)
    dependsOn(tasks.named("bootJar"))
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("org.json:json:20230227")
    implementation("com.github.javaparser:javaparser-core:3.25.1")
    implementation("org.mvel:mvel2:2.4.14.Final")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}