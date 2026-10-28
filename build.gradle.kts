plugins {
    `java-library`
}

group = "de.niktr"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
}

tasks.jar {
    archiveBaseName.set("NT-exptobottles")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")
}
