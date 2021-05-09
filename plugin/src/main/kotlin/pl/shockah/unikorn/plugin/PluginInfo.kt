package pl.shockah.unikorn.plugin

import kotlinx.serialization.Serializable

interface PluginInfo {
	val identifier: String
	val version: PluginVersion
	val dependencies: Set<DependencyEntry>

	@Serializable
	data class DependencyEntry(
			val identifier: String,
			val version: PluginVersion.Filter = PluginVersion.Filter("*")
	) {
		fun matches(info: PluginInfo): Boolean {
			return identifier == info.identifier && version.matches(info.version)
		}
	}
}