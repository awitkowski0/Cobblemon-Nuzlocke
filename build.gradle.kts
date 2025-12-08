buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-metadata-jvm:2.2.0")
    }
}

plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version("2.2.20")

    id("dev.architectury.loom") version("1.11-SNAPSHOT") apply false
    id("architectury-plugin") version("3.4-SNAPSHOT") apply false
    id("me.modmuss50.mod-publish-plugin") version("0.8.4")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    version = project.properties["mod_version"]!!
    group = project.properties["maven_group"]!!

    repositories {
        mavenCentral()
        maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://maven.neoforged.net/releases")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    java {
        withSourcesJar()
    }
}

publishMods {
    changelog.set(
        if (file("CHANGELOG.md").exists()) file("CHANGELOG.md").readText() 
        else "Initial release"
    )
    type.set(STABLE)
    dryRun.set(
        System.getenv("MODRINTH_TOKEN") == null || System.getenv("CURSEFORGE_TOKEN") == null
    )

    val cfOptions = curseforgeOptions {
        accessToken.set(System.getenv("CURSEFORGE_TOKEN"))
        projectId.set(project.properties["curseforge_id"]?.toString() ?: "")
        minecraftVersions.add("1.21.1")
    }

    val mrOptions = modrinthOptions {
        accessToken.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set(project.properties["modrinth_id"]?.toString() ?: "")
        minecraftVersions.add("1.21.1")
    }

    curseforge("curseforgeFabric") {
        from(cfOptions)
        file.set(layout.projectDirectory.file("fabric/build/libs/nuzlocke-fabric-${project.version}.jar"))
        displayName.set("${project.version}")
        modLoaders.add("fabric")
        requires("fabric-api")
        requires("cobblemon")
    }

    modrinth("modrinthFabric") {
        from(mrOptions)
        file.set(layout.projectDirectory.file("fabric/build/libs/nuzlocke-fabric-${project.version}.jar"))
        displayName.set("${project.version}")
        modLoaders.add("fabric")
        requires("fabric-api")
        requires("cobblemon")
    }

    curseforge("curseforgeNeoForge") {
        from(cfOptions)
        file.set(layout.projectDirectory.file("neoforge/build/libs/nuzlocke-neoforge-${project.version}.jar"))
        displayName.set("${project.version}")
        modLoaders.add("neoforge")
        requires("cobblemon")
    }

    modrinth("modrinthNeoForge") {
        from(mrOptions)
        file.set(layout.projectDirectory.file("neoforge/build/libs/nuzlocke-neoforge-${project.version}.jar"))
        displayName.set("${project.version}")
        modLoaders.add("neoforge")
        requires("cobblemon")
    }
}