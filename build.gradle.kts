import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget

plugins {
	`java-library`
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("org.jetbrains.kotlin.jvm") version "1.6.10"
	id("io.papermc.paperweight.userdev") version "1.3.3-LOCAL-SNAPSHOT"
	id("xyz.jpenilla.run-paper") version "1.0.6" // Adds runServer and runMojangMappedServer tasks for testing
	id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
}

group = project.property("pluginGroup")!!
version = project.property("pluginVersion")!!
description = "Runs games of UHC"

java {
	// Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
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
	/* transact-sql driver */
	maven(url = "https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc")
	maven(url = "https://repo.maven.apache.org/maven2")
	/* reflection remapper */
	maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	implementation("net.dv8tion:JDA:5.0.0-alpha.9")
	implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
	implementation("com.microsoft.sqlserver:mssql-jdbc:10.2.0.jre17")
	implementation("xyz.jpenilla:reflection-remapper:0.1.0-SNAPSHOT")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")

	compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
	compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}

val rc = configurations.runtimeClasspath

tasks {
	val nmsJar = register<Jar>("nmsJar") {
		archiveFileName.set("uhc-nms.jar")
		from(sourceSets["main"].output)
	}
	shadowJar {
		exclude("net.minecraft.*")
		dependsOn(nmsJar)
	}
	assemble {
		dependsOn(reobfJar)
	}
	compileKotlin {
		kotlinOptions.jvmTarget = "17"
		kotlinOptions.apiVersion = "1.6"
	}
	compileJava {
		options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
		// Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
		// See https://openjdk.java.net/jeps/247 for more information.
		options.release.set(17)
	}
	javadoc {
		options.encoding = Charsets.UTF_8.name()
	}
	processResources {
		filteringCharset = Charsets.UTF_8.name()
	}
	runServer {
		jvmArgs("--add-exports=java.base/jdk.internal.loader=ALL-UNNAMED")
		jvmArgs("--add-opens=java.base/java.security=ALL-UNNAMED")
		jvmArgs("--add-opens=java.base/java.net=ALL-UNNAMED")
	}
}

bukkit {
	load = BukkitPluginDescription.PluginLoadOrder.STARTUP
	main = "com.codeland.uhc.UHCPlugin"
	apiVersion = "1.18"
	authors = listOf("balduvian")
	depend = listOf("ProtocolLib")
}