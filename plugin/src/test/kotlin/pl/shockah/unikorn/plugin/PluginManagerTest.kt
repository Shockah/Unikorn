package pl.shockah.unikorn.plugin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class PluginManagerTest {
	companion object {
		private val systemOut = System.out

		private val pluginNames = listOf(
				"basic",
				"strong1", "strong2", "strong3",
				"weak1", "weak2"
		)

		private lateinit var pluginInfos: Map<String, PluginInfo>

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

			pluginInfos = pluginNames.map { it to PluginInfo(getPathForPlugin(it)) }.toMap()
		}

		private fun getPathForPlugin(name: String): File {
			return File("../plugin/test-$name/build/libs/test-$name.jar")
		}
	}

	private fun getPluginManager(vararg pluginNames: String): PluginManager {
		return PluginManager(object: PluginInfo.Provider {
			override fun provide(): List<PluginInfo> {
				return pluginNames.map { pluginInfos[it]!! }
			}
		})
	}

	@Test
	fun basicLoadUnload() {
		val baos = ByteArrayOutputStream()
		val manager = getPluginManager("basic")

		System.setOut(PrintStream(baos, true, "UTF-8"))
		manager.loadAll()
		manager.unloadAll()
		System.setOut(systemOut)

		val result = String(baos.toByteArray()).trim().lines()
		assertEquals("""
			Loading BasicTestPlugin
			Unloading BasicTestPlugin
		""".trimIndent().lines(), result)
	}

	@Test
	fun strong123LoadUnload() {
		val baos = ByteArrayOutputStream()
		val manager = getPluginManager("strong1", "strong2", "strong3")

		System.setOut(PrintStream(baos, true, "UTF-8"))
		manager.loadAll()
		manager.unloadAll()
		System.setOut(systemOut)

		val result = String(baos.toByteArray()).trim().lines()
		assertEquals("""
			Loading Strong1Plugin
			Loading Strong3Plugin
			Loaded required dependencies for Strong3Plugin
			Loading Strong2Plugin
			Loaded required dependencies for Strong2Plugin
			Unloading Strong2Plugin
			Unloading Strong3Plugin
			Unloading Strong1Plugin
		""".trimIndent().lines(), result)
	}

	@Test
	fun weak1LoadUnload() {
		val baos = ByteArrayOutputStream()
		System.setOut(PrintStream(baos, true, "UTF-8"))

		val manager = getPluginManager("weak1", "weak2")
		manager.loadAll()
		manager.unloadAll()
		System.setOut(systemOut)

		val result = String(baos.toByteArray()).trim().lines()

		val indexLoading1 = result.indexOf("Loading Weak1Plugin")
		val indexLoading2 = result.indexOf("Loading Weak2Plugin")
		val indexUnloading1 = result.indexOf("Unloading Weak1Plugin")
		val indexUnloading2 = result.indexOf("Unloading Weak2Plugin")
		val indexLoadedOptional = result.indexOf("Loaded optional dependency Weak2Plugin for Weak1Plugin")

		assertNotEquals(indexLoading1, -1)
		assertNotEquals(indexUnloading1, -1)
		assertNotEquals(indexLoading2, -1)
		assertNotEquals(indexUnloading2, -1)
		assertNotEquals(indexLoadedOptional, -1)

		assertTrue(indexLoading1 < indexUnloading1)
		assertTrue(indexLoading2 < indexUnloading2)
		assertTrue(indexLoadedOptional < indexUnloading1)
		assertTrue(indexLoadedOptional < indexUnloading2)
	}
}