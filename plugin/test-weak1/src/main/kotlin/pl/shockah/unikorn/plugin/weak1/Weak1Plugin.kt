package pl.shockah.unikorn.plugin.weak1

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.impl.PluginDependency

class Weak1Plugin: Plugin {
	@PluginDependency.Optional("pl.shockah.unikorn.plugin.weak2")
	private var weak2Plugin: Plugin? = null

	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onDependencyInjected(plugin: Plugin) {
		println("Loaded dependency ${plugin::class.simpleName} for ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}