package pl.shockah.unikorn.plugin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import pl.shockah.unikorn.plugin.impl.FileListPluginInfoProvider
import pl.shockah.unikorn.plugin.impl.FilePluginLoaderFactory
import pl.shockah.unikorn.plugin.impl.SerialPluginManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class SerialPluginManagerTest {
	companion object {
		private val systemOut = System.out

		private val pluginNames = listOf(
				"basic",
				"strong1", "strong2", "strong3",
				"weak1", "weak2"
		)

		private val gradlePluginNames: List<String>
			get() = pluginNames.map { "plugin:test-$it:jar" }

		@JvmStatic
		@BeforeAll
		fun setup() {
			val isWindows = System.getProperty("os.name").contains("windows", true)
			val processName: String
			val args: List<String>

			if (isWindows) {
				processName = "cmd.exe"
				args = listOf("/c", "gradlew.bat") + gradlePluginNames
			} else {
				processName = "./gradlew"
				args = gradlePluginNames
			}

			val result = ProcessBuilder(processName, *args.toTypedArray())
					.directory(File(System.getProperty("user.dir")).parentFile.absoluteFile)
					.redirectErrorStream(true)
					.start().apply {
						inputStream.reader().forEachLine { println(it) }
					}.waitFor()
			if (result != 0)
				throw IllegalStateException("Expected gradlew result code 0, got $result instead")
		}

		private fun getPathForPlugin(name: String): File {
			return File("../plugin/test-$name/build/libs/test-$name.jar")
		}
	}

	private fun getPluginManager(vararg pluginNames: String): PluginManager {
		return SerialPluginManager(
				infoProvider = FileListPluginInfoProvider(pluginNames.map { getPathForPlugin(it) }),
				loaderFactory = FilePluginLoaderFactory()
		)
	}

	@Test
	fun basicLoadUnload() {
		val baos = ByteArrayOutputStream()
		System.setOut(PrintStream(baos, true, "UTF-8"))

		val manager = getPluginManager("basic")

		assertEquals("", String(baos.toByteArray()).trim())
		assertEquals(1, manager.allPluginInfos.size)
		assertEquals(0, manager.loadedPluginInfos.size)
		assertEquals(1, manager.unloadedPluginInfos.size)
		assertEquals(0, manager.loadedPlugins.size)

		manager.loadAllPlugins()

		assertEquals("""
			Loading BasicTestPlugin
		""".trimIndent(), String(baos.toByteArray()).trim())
		assertEquals(1, manager.allPluginInfos.size)
		assertEquals(1, manager.loadedPluginInfos.size)
		assertEquals(0, manager.unloadedPluginInfos.size)
		assertEquals(1, manager.loadedPlugins.size)

		manager.unloadAllPlugins()

		assertEquals("""
			Loading BasicTestPlugin
			Unloading BasicTestPlugin
		""".trimIndent(), String(baos.toByteArray()).trim())
		assertEquals(1, manager.allPluginInfos.size)
		assertEquals(0, manager.loadedPluginInfos.size)
		assertEquals(1, manager.unloadedPluginInfos.size)
		assertEquals(0, manager.loadedPlugins.size)

		System.setOut(systemOut)
	}

	@Test
	fun strong123LoadUnload() {
		val baos = ByteArrayOutputStream()
		System.setOut(PrintStream(baos, true, "UTF-8"))

		val manager = getPluginManager("strong1", "strong2", "strong3")

		assertEquals("", String(baos.toByteArray()).trim())
		assertEquals(3, manager.allPluginInfos.size)
		assertEquals(0, manager.loadedPluginInfos.size)
		assertEquals(3, manager.unloadedPluginInfos.size)
		assertEquals(0, manager.loadedPlugins.size)

		manager.loadAllPlugins()

		assertEquals("""
			Loading Strong1Plugin
			Loading Strong3Plugin
			Loaded dependency Strong1Plugin for Strong3Plugin
			Loaded required dependencies for Strong3Plugin
			Loading Strong2Plugin
			Loaded dependency Strong3Plugin for Strong2Plugin
			Loaded required dependencies for Strong2Plugin
		""".trimIndent(), String(baos.toByteArray()).trim())
		assertEquals(3, manager.allPluginInfos.size)
		assertEquals(3, manager.loadedPluginInfos.size)
		assertEquals(0, manager.unloadedPluginInfos.size)
		assertEquals(3, manager.loadedPlugins.size)

		manager.unloadAllPlugins()

		assertEquals("""
			Loading Strong1Plugin
			Loading Strong3Plugin
			Loaded dependency Strong1Plugin for Strong3Plugin
			Loaded required dependencies for Strong3Plugin
			Loading Strong2Plugin
			Loaded dependency Strong3Plugin for Strong2Plugin
			Loaded required dependencies for Strong2Plugin
			Unloading Strong2Plugin
			Unloading Strong3Plugin
			Unloading Strong1Plugin
		""".trimIndent(), String(baos.toByteArray()).trim())
		assertEquals(3, manager.allPluginInfos.size)
		assertEquals(0, manager.loadedPluginInfos.size)
		assertEquals(3, manager.unloadedPluginInfos.size)
		assertEquals(0, manager.loadedPlugins.size)

		System.setOut(systemOut)
	}

	@Test
	fun weak12LoadUnload() {
		val baos = ByteArrayOutputStream()
		System.setOut(PrintStream(baos, true, "UTF-8"))

		val manager = getPluginManager("weak1", "weak2")

		assertEquals("", String(baos.toByteArray()).trim())
		assertEquals(2, manager.allPluginInfos.size)
		assertEquals(0, manager.loadedPluginInfos.size)
		assertEquals(2, manager.unloadedPluginInfos.size)
		assertEquals(0, manager.loadedPlugins.size)

		manager.loadAllPlugins()

		run {
			val lines = String(baos.toByteArray()).trim().lines()
			val loading1Index = lines.indexOf("Loading Weak1Plugin")
			val loading2Index = lines.indexOf("Loading Weak2Plugin")
			val loadingOptionalIndex = lines.indexOf("Loaded dependency Weak2Plugin for Weak1Plugin")

			assertEquals(3, lines.size)
			assertNotEquals(-1, loading1Index)
			assertNotEquals(-1, loading2Index)
			assertNotEquals(-1, loadingOptionalIndex)
			assertTrue(loadingOptionalIndex > loading1Index)
		}
		assertEquals(2, manager.allPluginInfos.size)
		assertEquals(2, manager.loadedPluginInfos.size)
		assertEquals(0, manager.unloadedPluginInfos.size)
		assertEquals(2, manager.loadedPlugins.size)

		manager.unloadAllPlugins()

		run {
			val lines = String(baos.toByteArray()).trim().lines()
			val loading1Index = lines.indexOf("Loading Weak1Plugin")
			val loading2Index = lines.indexOf("Loading Weak2Plugin")
			val unloading1Index = lines.indexOf("Unloading Weak1Plugin")
			val unloading2Index = lines.indexOf("Unloading Weak2Plugin")
			val loadingOptionalIndex = lines.indexOf("Loaded dependency Weak2Plugin for Weak1Plugin")

			assertEquals(5, lines.size)
			assertNotEquals(-1, loading1Index)
			assertNotEquals(-1, loading2Index)
			assertNotEquals(-1, unloading1Index)
			assertNotEquals(-1, unloading2Index)
			assertNotEquals(-1, loadingOptionalIndex)
			assertTrue(loadingOptionalIndex > loading1Index)
			assertTrue(loading2Index - loading1Index == unloading1Index - unloading2Index)
		}
		assertEquals(2, manager.allPluginInfos.size)
		assertEquals(0, manager.loadedPluginInfos.size)
		assertEquals(2, manager.unloadedPluginInfos.size)
		assertEquals(0, manager.loadedPlugins.size)

		System.setOut(systemOut)
	}
}