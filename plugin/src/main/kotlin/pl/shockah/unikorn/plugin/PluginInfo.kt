package pl.shockah.unikorn.plugin

interface PluginInfo {
	val identifier: String
	val pluginClassName: String
	val version: PluginVersion
	val dependencies: List<DependencyEntry>

	data class DependencyEntry(
			val identifier: String,
			val version: PluginVersion.Filter = PluginVersion.Filter("*")
	) {
		fun matches(info: PluginInfo): Boolean {
			return identifier == info.identifier && version.matches(info.version)
		}
	}

	data class WithReference<Reference>(
			override val identifier: String,
			override val pluginClassName: String,
			override val version: PluginVersion = PluginVersion("1.0"),
			override val dependencies: List<DependencyEntry> = emptyList(),
			val reference: Reference
	): PluginInfo
}