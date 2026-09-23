plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "io.github.khram0v"
version = "0.0.1-SNAPSHOT"
description = "gym-crm"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

val jjwtVersion = "0.13.0"
val springdocVersion = "3.0.3"
val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val cucumberVersion = "7.34.8"
val awaitilityVersion = "4.3.0"

sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

configurations {
    named("integrationTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }

    named("integrationTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

val integrationTestImplementation =
    configurations.named("integrationTestImplementation")

val integrationTestRuntimeOnly =
    configurations.named("integrationTestRuntimeOnly")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-activemq")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    implementation("com.github.ben-manes.caffeine:caffeine")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.junit.platform:junit-platform-suite")

    integrationTestImplementation("org.testcontainers:testcontainers-mongodb")

    testCompileOnly("org.projectlombok:lombok")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.register<Test>("integrationTest") {
    description = "Cross-service integration tests (requires Docker; expects " +
            "trainer-workload-service checked out as a sibling directory, override via " +
            "-DtrainerWorkloadServiceDir=<path>)"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    systemProperty(
        "trainerWorkloadServiceDir",
        System.getProperty("trainerWorkloadServiceDir", "../trainer-workload-service")
    )
    shouldRunAfter(tasks.test)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

val coverageExclusions = listOf(
    "io/github/khram0v/gymcrm/GymCrmApplication.class",
    "io/github/khram0v/gymcrm/model/**",
    "io/github/khram0v/gymcrm/repository/**",
    "io/github/khram0v/gymcrm/exception/**",
    "io/github/khram0v/gymcrm/dto/**",
    "io/github/khram0v/gymcrm/config/**",
    "io/github/khram0v/gymcrm/api/**",
    "io/github/khram0v/gymcrm/filter/**",
    "io/github/khram0v/gymcrm/security/config/**",
    "io/github/khram0v/gymcrm/migration/**",
    "io/github/khram0v/gymcrm/client/dto/**"
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(coverageExclusions)
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(coverageExclusions)
            }
        })
    )
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
