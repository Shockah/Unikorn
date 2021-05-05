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

abstract class BaseFilePluginInfoProvider: PluginInfoProvider<FilePluginInfo> {
	protected fun readPluginInfo(jarFile: File): FilePluginInfo {
		require(jarFile.exists() && jarFile.isFile) { "Plugin JAR file ${jarFile.absoluteFile.normalize().absolutePath} doesn't exist." }

		ZipFile(jarFile).use { zip ->
			val jsonEntry = zip.getEntry("plugin.json").guard { throw FilePluginInfoProvider.MissingPluginJsonException() }
			val json = Klaxon().parseJsonObject(zip.getInputStream(jsonEntry).reader())
			return readPluginInfo(json, jarFile)
		}
	}

	protected fun readPluginInfo(json: JsonObject, jarFile: File): FilePluginInfo {
		return FilePluginInfo(
				json.string("identifier")!!,
				PluginVersion(json.string("version") ?: "1.0"),
				json.array<JsonObject>("dependencies")?.map {
					PluginInfo.DependencyEntry(
							it.string("identifier")!!,
							PluginVersion.Filter(it.string("version") ?: "*")
					)
				}?.toSet() ?: emptySet(),
				jarFile,
				json.string("pluginClassName")!!
		)
	}
}

class FileListPluginInfoProvider(
		private val files: Collection<File>
): BaseFilePluginInfoProvider() {
	class MissingPluginJsonException: Exception()

	override fun getPluginInfos(): Set<FilePluginInfo> {
		return files.mapValid { readPluginInfo(it.absoluteFile.normalize()) }.toSet()
	}
}

class FilePluginInfoProvider(
		private val pluginDirectory: File
): BaseFilePluginInfoProvider() {
	class MissingPluginJsonException: Exception()

	override fun getPluginInfos(): Set<FilePluginInfo> {
		return (pluginDirectory.listFiles() ?: emptyArray()).filter { it.extension == "jar" }.mapValid { readPluginInfo(it.absoluteFile.normalize()) }.toSet()
	}
}