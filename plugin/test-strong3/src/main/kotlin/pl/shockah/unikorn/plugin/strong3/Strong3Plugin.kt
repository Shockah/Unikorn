package pl.shockah.unikorn.plugin.strong3

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.impl.PluginDependency
import pl.shockah.unikorn.plugin.strong1.Strong1Plugin

class Strong3Plugin: Plugin {
	@PluginDependency.Required
	private lateinit var strong1Plugin: Strong1Plugin

	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onDependencyInjected(plugin: Plugin) {
		println("Loaded dependency ${plugin::class.simpleName} for ${this::class.simpleName}")
	}

	override fun onRequiredDependenciesInjected() {
		println("Loaded required dependencies for ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}