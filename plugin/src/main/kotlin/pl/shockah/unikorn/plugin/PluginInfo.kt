package pl.shockah.unikorn.plugin

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import pl.shockah.unikorn.guard
import pl.shockah.unikorn.mapValid
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.zip.ZipFile

data class PluginInfo(
		val identifier: String,
		val pluginClassName: String?,
		val version: Plugin.Version,
		val dependencies: List<DependencyEntry>,
		val url: URL
) {
	companion object {
		operator fun invoke(jarFile: File): PluginInfo {
			require(!(!jarFile.exists() || !jarFile.isFile)) { "Plugin JAR file ${jarFile.absoluteFile.normalize().absolutePath} doesn't exist." }

			ZipFile(jarFile).use { zip ->
				val jsonEntry = zip.getEntry("plugin.json").guard { throw IOException() } // TODO: throw a better exception
				val json = Klaxon().parseJsonObject(zip.getInputStream(jsonEntry).reader())
				return PluginInfo(json, jarFile.toURI().toURL())
			}
		}

		operator fun invoke(json: JsonObject, url: URL): PluginInfo {
			return PluginInfo(
					json.string("identifier")!!,
					json.string("pluginClassName"),
					Plugin.Version(json.string("version") ?: "1.0"),
					json.array<JsonObject>("dependencies")?.map {
						DependencyEntry(
								it.string("identifier")!!,
								Plugin.Version.Filter(it.string("version") ?: "*")
						)
					} ?: emptyList(),
					url
			)
		}
	}

	data class DependencyEntry(
			val identifier: String,
			val version: Plugin.Version.Filter
	) {
		fun matches(info: PluginInfo): Boolean {
			return identifier == info.identifier && version.matches(info.version)
		}
	}

	interface Provider {
		fun provide(): List<PluginInfo>

		class Default(
				val pluginsDirectory: File
		) : Provider {
			override fun provide(): List<PluginInfo> {
				return (pluginsDirectory.listFiles() ?: emptyArray()).filter { it.extension == "jar" }.mapValid { PluginInfo(it.absoluteFile.normalize()) }
			}
		}
	}
}