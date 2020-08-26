plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = properties["pluginGroup"]!!
version = properties["pluginVersion"]!!

repositories {
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/") //paper
    maven(url = "https://repo.dmulloy2.net/nexus/repository/public/") //protocollib
    maven(url = "https://jitpack.io/") //tap, psychic
    maven(url = "https://maven.enginehub.org/repo/") //worldedit
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8")) //kotlin
    compileOnly("junit:junit:4.12") //junit
    compileOnly("com.destroystokyo.paper:paper-api:1.16.1-R0.1-SNAPSHOT") //paper
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT") //protocollib
    compileOnly("com.github.noonmaru:tap:2.8.8") //tap
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.1.0") //worldedit
    implementation("com.github.noonmaru:kommand:0.1.9")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
    shadowJar {
        archiveClassifier.set("dist")
    }
    create<Copy>("distJar") {
        from(shadowJar)
        into("W:\\Servers\\parkour-maker\\plugins")
    }
}

if (!hasProperty("debug")) {
    tasks {
        shadowJar {
            relocate("com.github.noonmaru.kommand", "com.github.noonmaru.parkourmaker.shaded")
        }
    }
}