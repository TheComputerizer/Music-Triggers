import wtf.gofancy.fancygradle.patch.Patch
import wtf.gofancy.fancygradle.script.extensions.createDebugLoggingRunConfig
import wtf.gofancy.fancygradle.script.extensions.curse
import wtf.gofancy.fancygradle.script.extensions.curseForge
import wtf.gofancy.fancygradle.script.extensions.deobf

import java.time.format.DateTimeFormatter
import java.time.Instant

plugins {
    java
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "4.1.10"
    id("wtf.gofancy.fancygradle") version "1.0.0"
}

version = "3.4-EX"
group = "mods.thecomputerizer.musictriggers"

minecraft {
    mappings("stable", "39-1.12")

    runs {
        createDebugLoggingRunConfig("client")
        createDebugLoggingRunConfig("server") { args("nogui") }
    }
}

fancyGradle {
    patches {
        patch(Patch.CODE_CHICKEN_LIB, Patch.RESOURCES, Patch.COREMODS, Patch.ASM)
    }
}

idea.module.inheritOutputDirs = true

repositories {
    mavenCentral()
    curseForge()
    maven {
        name = "Progwml6"
        url = uri("https://dvs1.progwml6.com/files/maven/")
    }
    maven {
        name = "ModMaven"
        url = uri("https://modmaven.k-4u.nl")
    }
    maven {
        url = uri("https://maven.mcmoddev.com")
    }
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.12.2-14.23.5.2855")

    implementation(fg.deobf(curse(mod = "codechicken-lib", projectId = 242818L, fileId = 2779848L)))
    implementation(fg.deobf(curse(mod = "bloodmoon", projectId = 226321L, fileId = 2537917L)))
    implementation(fg.deobf(curse(mod = "nyx", projectId = 349214L, fileId = 3161738L)))
    implementation(fg.deobf(curse(mod = "dynamic-surroundings", projectId = 238891L, fileId = 3497269L)))
    implementation(fg.deobf(curse(mod = "quark", projectId = 243121L, fileId = 2924091L)))
    implementation(fg.deobf(curse(mod = "weather-storms-tornadoes", projectId = 237746L, fileId = 2596867L)))
    implementation(fg.deobf(curse(mod = "coroutil", projectId = 237749L, fileId = 2902920L)))
    implementation(fg.deobf(curse(mod = "atomicstrykers-infernal-mobs", projectId = 227875L, fileId = 3431758L)))
    implementation(fg.deobf(group = "net.darkhax.gamestages", name = "GameStages-1.12.2", version = "2.0.98"))
    runtimeOnly(fg.deobf(group = "mezz.jei", name = "jei_1.12.2", version = "4.16.1.302"))
}

sourceSets.main {
    resources.outputDir = java.outputDir
}

tasks {
    withType<Jar> {
        archiveBaseName.set("music-triggers")
        finalizedBy("reobfJar")

        manifest {
            attributes(
                    "Specification-Title" to project.name,
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "TheComputerizer",
                    "Implementation-Title" to "${project.group}.${project.name.toLowerCase().replace(' ', '_')}",
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "TheComputerizer",
                    "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    withType<Wrapper> {
        gradleVersion = "6.8.3"
        distributionType = Wrapper.DistributionType.ALL
    }
}