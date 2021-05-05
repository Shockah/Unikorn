package pl.shockah.unikorn.plugin.strong1

import pl.shockah.unikorn.plugin.Plugin

class Strong1Plugin: Plugin {
	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}