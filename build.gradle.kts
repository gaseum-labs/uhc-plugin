import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
	`java-library`
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("org.jetbrains.kotlin.jvm") version "1.6.10"
	id("io.papermc.paperweight.userdev") version "1.3.7-LOCAL-SNAPSHOT"
	id("xyz.jpenilla.run-paper") version "1.0.6" // Adds runServer and runMojangMappedServer tasks for testing
	id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
}

group = project.property("pluginGroup")!!
version = project.property("pluginVersion")!!
description = "Runs games of UHC"

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
	mavenLocal()
	/* paper api */
	maven(url = "https://papermc.io/repo/repository/maven-public/")
	/* commands api */
	maven(url = "https://repo.aikar.co/content/groups/aikar/")
	/* discord */
	maven(url = "https://m2.dv8tion.net/releases")
	/* ? */
	maven(url = "https://repo.maven.apache.org/maven2")
	/* reflection remapper */
	maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
	paperDevBundle(
		"1.18.2-R0.1-SNAPSHOT",
		"org.gaseumlabs.uhcpaper"
	)

	implementation("net.dv8tion:JDA:5.0.0-alpha.9")
	implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
	implementation("xyz.jpenilla:reflection-remapper:0.1.0-SNAPSHOT")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")

	compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
	compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}

abstract class WslIpTask : DefaultTask() {
	@org.gradle.api.tasks.TaskAction
	fun getIp() {
		val command = """powershell.exe wsl -- ip -o -4 -json addr list eth0 `
			| ConvertFrom-Json `
			| %{ ${"$"}_.addr_info.local } `
			| ?{ ${"$"}_ }"""
		val powerShellProcess: Process = Runtime.getRuntime().exec(command)
		powerShellProcess.outputStream.close()

		var output = ""
		val stdout = BufferedReader(InputStreamReader(powerShellProcess.inputStream))
		while (stdout.readLine().also { output += it } != null) {
		}
		println(output)
		stdout.close()

		var errLine: String?
		val stderr = BufferedReader(InputStreamReader(powerShellProcess.errorStream))
		while (stderr.readLine().also { errLine = it } != null) {
			println(errLine)
		}
		stderr.close()
	}
}

tasks.register<WslIpTask>("wslIp")

tasks {
	jar {
		enabled = false
	}
	assemble {
		dependsOn(reobfJar)
	}
	compileKotlin {
		kotlinOptions.jvmTarget = "17"
		kotlinOptions.apiVersion = "1.6"
	}
	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.release.set(17)
	}
	javadoc {
		options.encoding = Charsets.UTF_8.name()
	}
	processResources {
		filteringCharset = Charsets.UTF_8.name()
	}
	runServer {
		serverJar(File("run/server.jar"))
	}
}

bukkit {
	load = BukkitPluginDescription.PluginLoadOrder.STARTUP
	main = "org.gaseumlabs.uhc.UHCPlugin"
	apiVersion = "1.18"
	authors = listOf("balduvian")
	depend = listOf("ProtocolLib")
}