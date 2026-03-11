plugins {
    java
}

group = "dev.ua.leavepulse"
version = property("version") as String

val velocityApiVersion = property("velocityVersion") as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroupByRegex("com\\.velocitypowered(\\..*)?")
        }
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:$velocityApiVersion")
    annotationProcessor("com.velocitypowered:velocity-api:$velocityApiVersion")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("velocity-plugin.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.jar {
    archiveBaseName.set("velocity-ping-fix")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.build {
    dependsOn(tasks.jar)
}
