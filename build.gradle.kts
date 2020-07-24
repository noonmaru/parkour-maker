plugins {
    kotlin("jvm") version "1.3.72"
}

group = requireNotNull(properties["pluginGroup"]) { "Group is undefined in properties" }
version = requireNotNull(properties["pluginVersion"]) { "Version is undefined in properties" }

repositories {
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.destroystokyo.paper:paper-api:1.14-R0.1-SNAPSHOT")
    implementation("com.sk89q.worldedit:worldedit-bukkit:7.1.0")
    implementation("com.github.noonmaru:tap:2.3.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
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