package pl.shockah.unikorn.plugin

interface Plugin {
	fun onUnload() { }
	fun onDependencyInjected(plugin: Plugin) { }
	fun onRequiredDependenciesInjected() { }
	fun onDependencyInjectionFinished() { }
	fun willUnloadDependency(plugin: Plugin) { }
	fun onPluginLoadCycleFinished(allLoadedPlugins: Map<PluginInfo, Plugin>, newlyLoadedPlugins: Map<PluginInfo, Plugin>) { }
}