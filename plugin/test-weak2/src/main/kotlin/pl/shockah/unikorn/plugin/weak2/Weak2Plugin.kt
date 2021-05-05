package pl.shockah.unikorn.plugin.weak2

import pl.shockah.unikorn.plugin.Plugin

class Weak2Plugin: Plugin {
	init {
		println("Loading ${this::class.simpleName}")
	}

	override fun onUnload() {
		println("Unloading ${this::class.simpleName}")
	}
}