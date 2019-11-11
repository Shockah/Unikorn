package pl.shockah.unikorn.plugin.basic

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.PluginInfo
import pl.shockah.unikorn.plugin.PluginManager

class BasicTestPlugin(
		manager: PluginManager,
		info: PluginInfo
): Plugin(manager, info) {
	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}