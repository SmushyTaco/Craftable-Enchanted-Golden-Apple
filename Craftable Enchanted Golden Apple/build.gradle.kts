plugins {
	id("fabric-loom")
}
base {
	val archivesBaseNameTwo: String by project
	archivesBaseName = archivesBaseNameTwo
}
val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup
minecraft {}
repositories {}
dependencies {
	val minecraftVersion: String by project
	minecraft("com.mojang:minecraft:$minecraftVersion")
	val yarnMappings: String by project
	mappings("net.fabricmc:yarn:$yarnMappings:v2")
	val loaderVersion: String by project
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
	val fabricVersion: String by project
	modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
}
tasks {
	val javaVersion = JavaVersion.VERSION_16
	withType<JavaCompile> {
		options.encoding = "UTF-8"
		sourceCompatibility = javaVersion.toString()
		targetCompatibility = javaVersion.toString()
		options.release.set(javaVersion.toString().toInt())
	}
	jar {
		from("LICENSE") {
			rename { "${it}_${base.archivesBaseName}" }
		}
	}
	processResources {
		inputs.property("version", project.version)
		filesMatching("fabric.mod.json") {
			expand(mutableMapOf("version" to project.version))
		}
	}
	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
		}
		sourceCompatibility = javaVersion
		targetCompatibility = javaVersion
		withSourcesJar()
	}
}