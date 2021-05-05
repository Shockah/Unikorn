package pl.shockah.unikorn.plugin.impl

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import pl.shockah.unikorn.collection.mapValid
import pl.shockah.unikorn.guard
import pl.shockah.unikorn.plugin.PluginInfo
import pl.shockah.unikorn.plugin.PluginInfoProvider
import pl.shockah.unikorn.plugin.PluginVersion
import java.io.File
import java.util.zip.ZipFile

abstract class BaseFilePluginInfoProvider: PluginInfoProvider<File> {
	protected fun readPluginInfo(jarFile: File): PluginInfo.WithReference<File> {
		require(jarFile.exists() && jarFile.isFile) { "Plugin JAR file ${jarFile.absoluteFile.normalize().absolutePath} doesn't exist." }

		ZipFile(jarFile).use { zip ->
			val jsonEntry = zip.getEntry("plugin.json").guard { throw FilePluginInfoProvider.MissingPluginJsonException() }
			val json = Klaxon().parseJsonObject(zip.getInputStream(jsonEntry).reader())
			return readPluginInfo(json, jarFile)
		}
	}

	protected fun readPluginInfo(json: JsonObject, reference: File): PluginInfo.WithReference<File> {
		return PluginInfo.WithReference(
				json.string("identifier")!!,
				json.string("pluginClassName")!!,
				PluginVersion(json.string("version") ?: "1.0"),
				json.array<JsonObject>("dependencies")?.map {
					PluginInfo.DependencyEntry(
							it.string("identifier")!!,
							PluginVersion.Filter(it.string("version") ?: "*")
					)
				} ?: emptyList(),
				reference
		)
	}
}

class FileListPluginInfoProvider(
		private val files: Collection<File>
): BaseFilePluginInfoProvider() {
	class MissingPluginJsonException: Exception()

	override fun getPluginInfos(): Set<PluginInfo.WithReference<File>> {
		return files.mapValid { readPluginInfo(it.absoluteFile.normalize()) }.toSet()
	}
}

class FilePluginInfoProvider(
		private val pluginDirectory: File
): BaseFilePluginInfoProvider() {
	class MissingPluginJsonException: Exception()

	override fun getPluginInfos(): Set<PluginInfo.WithReference<File>> {
		return (pluginDirectory.listFiles() ?: emptyArray()).filter { it.extension == "jar" }.mapValid { readPluginInfo(it.absoluteFile.normalize()) }.toSet()
	}
}