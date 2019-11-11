package pl.shockah.unikorn.plugin.strong2

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.PluginInfo
import pl.shockah.unikorn.plugin.PluginManager
import pl.shockah.unikorn.plugin.strong3.Strong3Plugin

class Strong2Plugin(
		manager: PluginManager,
		info: PluginInfo
): Plugin(manager, info) {
	@RequiredDependency
	private lateinit var strong3Plugin: Strong3Plugin

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