package pl.shockah.unikorn.plugin.impl

import pl.shockah.unikorn.plugin.PluginInfo
import pl.shockah.unikorn.plugin.PluginVersion
import java.io.File

data class FilePluginInfo(
		override val identifier: String,
		override val version: PluginVersion = PluginVersion("1.0"),
		override val dependencies: Set<PluginInfo.DependencyEntry> = emptySet(),
		val jarFile: File,
		val pluginClassName: String
): PluginInfo