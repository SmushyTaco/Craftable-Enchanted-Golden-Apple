pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        gradlePluginPortal()
    }
    plugins {
        val loomVersion: String by settings
        id("fabric-loom").version(loomVersion)
    }
}