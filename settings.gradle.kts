pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://files.minecraftforge.net/maven")
        maven("https://dist.creeper.host/Sponge/maven")
        maven("https://plugins.gradle.org/m2/")
        maven("https://jitpack.io")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://nexus.scarsz.me/content/groups/public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.essentialsx.net/snapshots/")
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://m2.dv8tion.net/releases")
        maven("https://repo1.maven.org/maven2/")
        maven("https://maven.playpro.com")
        maven("https://jitpack.io")
    }
}

rootProject.name = "EmpireItems"
include(
    "api",
    "plugin",
    "enchantements",
    "modelengine",
)
include(
    "models:shared",
    "models:bukkit"
)

include(
    "block-generation:1-19-2",
    "block-generation:1-19-3",
    "block-generation:core"
)
include("desktop")
include("zipper")