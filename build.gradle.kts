plugins {
    kotlin("jvm") version "1.3.72"
}

group = properties["pluginGroup"]!!
version = properties["pluginVersion"]!!

repositories {
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT")
    implementation("com.comphenix.protocol:ProtocolLib:4.5.0")
    implementation("com.sk89q.worldedit:worldedit-bukkit:7.1.0")
    implementation("com.github.noonmaru:tap:2.3.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }

    create<Copy>("distJar") {
        from(jar)
        into("W:\\Servers\\parkour-maker\\plugins")
    }
}