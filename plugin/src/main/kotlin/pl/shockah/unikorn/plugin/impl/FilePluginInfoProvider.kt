package pl.shockah.unikorn.plugin.impl

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import pl.shockah.unikorn.collection.mapValid
import pl.shockah.unikorn.plugin.PluginInfoProvider
import java.io.File
import java.util.zip.ZipFile

abstract class BaseFilePluginInfoProvider: PluginInfoProvider<FilePluginInfo> {
	protected fun readPluginInfo(jarFile: File): FilePluginInfo {
		require(jarFile.exists() && jarFile.isFile) { "Plugin JAR file ${jarFile.absoluteFile.normalize().absolutePath} doesn't exist." }

		ZipFile(jarFile).use { zip ->
			val handlers: List<Pair<List<String>, (String) -> FilePluginInfo.Base?>> = listOf(
					listOf("yml", "yaml") to { Yaml.default.decodeFromString(it) },
					listOf("json") to { Json.Default.decodeFromString(it) }
			)
			for ((extensions, handler) in handlers) {
				for (extension in extensions) {
					val zipEntry = zip.getEntry("plugin.$extension") ?: continue
					val content = zip.getInputStream(zipEntry).bufferedReader().use { it.readText() }
					return readPluginInfo(handler(content) ?: continue, jarFile)
				}
			}
			throw FilePluginInfoProvider.MissingPluginDefinitonException()
		}
	}

	protected fun readPluginInfo(basePluginInfo: FilePluginInfo.Base, jarFile: File): FilePluginInfo {
		return FilePluginInfo(
				basePluginInfo.identifier,
				basePluginInfo.version,
				basePluginInfo.dependencies,
				jarFile,
				basePluginInfo.pluginClassName
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
	class MissingPluginDefinitonException: Exception()

	override fun getPluginInfos(): Set<FilePluginInfo> {
		return (pluginDirectory.listFiles() ?: emptyArray()).filter { it.extension == "jar" }.mapValid { readPluginInfo(it.absoluteFile.normalize()) }.toSet()
	}
}