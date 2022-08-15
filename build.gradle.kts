import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id ("idea")
    id ("com.github.johnrengelman.shadow") version "7.1.1"
    id ("org.jetbrains.kotlin.jvm") version "1.7.0"
    id ("xyz.jpenilla.run-paper") version "1.0.6"
    id ("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "building-game-plugin"
version = "0.9.0"

repositories {
    mavenLocal()
    maven(url="https://papermc.io/repo/repository/maven-public/")
    maven(url="https://repo.aikar.co/content/groups/aikar/")
    mavenCentral()
}

dependencies {
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
}

tasks {
    shadowJar {
        archiveFileName.set("BuildingGame.jar")
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.apiVersion = "1.7"
    }
    runServer {
        minecraftVersion("1.19.2")
    }
}

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "BuildingGamePlugin"
    apiVersion = "1.19"
    authors = listOf("balduvian")
}