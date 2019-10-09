package pl.shockah.unikorn.plugin.strong3

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.PluginInfo
import pl.shockah.unikorn.plugin.PluginManager
import pl.shockah.unikorn.plugin.strong1.Strong1Plugin

class Strong3Plugin(
		manager: PluginManager,
		info: PluginInfo
) : Plugin(manager, info) {
	@RequiredDependency
	private lateinit var strong1Plugin: Strong1Plugin

	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onRequiredDependenciesLoaded() {
		println("Loaded required dependencies for ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}