@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.codeland.uhc.world.gen

//import jdk.internal.loader.URLClassPath
//import java.security.AccessControlContext
import com.codeland.uhc.UHCPlugin
import com.google.common.io.ByteStreams
import jdk.internal.loader.URLClassPath
import net.minecraft.world.level.levelgen.NoiseRouterWithOnlyNoises
import org.bukkit.plugin.java.PluginClassLoader
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.security.*
import java.util.*
import java.util.jar.*

object ClassInjector {
	fun injectNms(file: File) {
		val jarFile = JarFile(file)
		val jarEntries: Enumeration<JarEntry> = jarFile.entries()

		val ucpField = URLClassLoader::class.java.getDeclaredField("ucp")
		ucpField.isAccessible = true

		val accField = URLClassLoader::class.java.getDeclaredField("acc")
		accField.isAccessible = true

		val nmsLoader = NoiseRouterWithOnlyNoises::class.java.classLoader as URLClassLoader

		ucpField[nmsLoader] = URLClassPath(
			arrayOf(
				*nmsLoader.urLs,
				//URL("jar:file:" + file.absolutePath.replace('\\', '/') + "!/")
				URL("jar:file:/" + file.absolutePath.replace('\\', '/') + "!/net/minecraft/")
			),
			accField[nmsLoader] as AccessControlContext
		)

		val f = 3

		//val defineClassMethod = SecureClassLoader::class.java.getDeclaredMethod(
		//	"defineClass",
		//	String::class.java,
		//	ByteArray::class.java,
		//	Int::class.java,
		//	Int::class.java,
		//	CodeSource::class.java
		//)
		//defineClassMethod.isAccessible = true
//
		//val url = file.toURI().toURL()
//
		//while (jarEntries.hasMoreElements()) {
		//	val entry = jarEntries.nextElement()
//
		//	/* fake nms class to inject */
		//	if (entry.name.startsWith("net/minecraft/") && entry.name.endsWith(".class")) {
		//		val stream = jarFile.getInputStream(entry)
		//		val classBytes = ByteStreams.toByteArray(stream)
//
		//		defineClassMethod.invoke(
		//			nmsLoader,
		//			entry.name.substring(0, entry.name.length - 6).replace('/', '.'),
		//			classBytes,
		//			0,
		//			classBytes.size,
		//			CodeSource(url, entry.codeSigners)
		//		)
		//	}
		//}
	}
}
