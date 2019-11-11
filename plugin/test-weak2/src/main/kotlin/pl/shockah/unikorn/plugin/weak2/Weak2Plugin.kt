package pl.shockah.unikorn.plugin.weak2

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.PluginInfo
import pl.shockah.unikorn.plugin.PluginManager

class Weak2Plugin(
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