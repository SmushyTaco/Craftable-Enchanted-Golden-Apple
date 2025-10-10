import net.darkhax.curseforgegradle.TaskPublishCurseForge
plugins {
    id("fabric-loom")
    id("com.modrinth.minotaur")
    id("net.darkhax.curseforgegradle")
    id("co.uzzu.dotenv.gradle")
}
val archivesBaseName = providers.gradleProperty("archives_base_name")
val modVersion = providers.gradleProperty("mod_version")
val mavenGroup = providers.gradleProperty("maven_group")
val minecraftVersion = providers.gradleProperty("minecraft_version")
val yarnMappings = providers.gradleProperty("yarn_mappings")
val loaderVersion = providers.gradleProperty("loader_version")
val fabricVersion = providers.gradleProperty("fabric_version")
val javaVersion = providers.gradleProperty("java_version")
base.archivesName = archivesBaseName.get()
version = modVersion.get()
group = mavenGroup.get()
dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion.get()}")
    mappings("net.fabricmc:yarn:${yarnMappings.get()}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion.get()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion.get()}")
}
tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.get()
        targetCompatibility = javaVersion.get()
        options.release = javaVersion.get().toInt()
    }
    withType<JavaExec>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<Javadoc>().configureEach { options.encoding = "UTF-8" }
    withType<Test>().configureEach { defaultCharacterEncoding = "UTF-8" }
    named<Jar>("jar") {
        val rootLicense = layout.projectDirectory.file("LICENSE")
        val parentLicense = layout.projectDirectory.file("../LICENSE")
        val licenseFile = when {
            rootLicense.asFile.exists() -> {
                logger.lifecycle("Using LICENSE from project root: ${rootLicense.asFile}")
                rootLicense
            }
            parentLicense.asFile.exists() -> {
                logger.lifecycle("Using LICENSE from parent directory: ${parentLicense.asFile}")
                parentLicense
            }
            else -> {
                logger.warn("No LICENSE file found in project or parent directory.")
                null
            }
        }
        licenseFile?.let {
            from(it) {
                rename { original -> "${original}_${archiveBaseName.get()}" }
            }
        }
    }
    processResources {
        val stringModVersion = modVersion.get()
        val stringLoaderVersion = loaderVersion.get()
        val stringFabricVersion = fabricVersion.get()
        val stringMinecraftVersion = minecraftVersion.get()
        val stringJavaVersion = javaVersion.get()
        inputs.property("modVersion", stringModVersion)
        inputs.property("loaderVersion", stringLoaderVersion)
        inputs.property("fabricVersion", stringFabricVersion)
        inputs.property("minecraftVersion", stringMinecraftVersion)
        inputs.property("javaVersion", stringJavaVersion)
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to stringModVersion,
                    "fabricloader" to stringLoaderVersion,
                    "fabric_api" to stringFabricVersion,
                    "minecraft" to stringMinecraftVersion,
                    "java" to stringJavaVersion
                )
            )
        }
    }
    java {
        toolchain.languageVersion = JavaLanguageVersion.of(javaVersion.get())
        sourceCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        targetCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        withSourcesJar()
    }
    register<TaskPublishCurseForge>("publishCurseForge") {
        disableVersionDetection()
        apiToken = env.fetch("CURSEFORGE_TOKEN", "")
        val file = upload(335918, remapJar)
        file.displayName = "[${minecraftVersion.get()}] Craftable Enchanted Golden Apple"
        file.addEnvironment("Client", "Server")
        file.changelog = ""
        file.releaseType = "release"
        file.addModLoader("Fabric")
        file.addGameVersion(minecraftVersion.get())
    }
}
modrinth {
    token.set(env.fetch("MODRINTH_TOKEN", ""))
    projectId.set("craftable-enchanted-golden-apple")
    uploadFile.set(tasks.remapJar)
    gameVersions.addAll(minecraftVersion.get())
    versionName.set("[${minecraftVersion.get()}] Craftable Enchanted Golden Apple")
    dependencies { required.project("fabric-api") }
}