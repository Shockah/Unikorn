package pl.shockah.unikorn.plugin.basic

import pl.shockah.unikorn.plugin.Plugin

class BasicTestPlugin: Plugin {
	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}