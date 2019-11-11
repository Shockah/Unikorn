package pl.shockah.unikorn.plugin.weak1

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.PluginInfo
import pl.shockah.unikorn.plugin.PluginManager

class Weak1Plugin(
		manager: PluginManager,
		info: PluginInfo
): Plugin(manager, info) {
	@OptionalDependency("pl.shockah.unikorn.plugin.weak2")
	private var weak2Plugin: Plugin? = null

	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onDependencyLoaded(plugin: Plugin) {
		println("Loaded optional dependency ${plugin::class.simpleName} for ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}