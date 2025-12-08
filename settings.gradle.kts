rootProject.name = "CobblemonNuzlocke"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.modmuss50.me/") // for mod-publish-plugin
        gradlePluginPortal()
    }
}

listOf(
    "common",
    "neoforge",
    "fabric"
).forEach { include(it)}
