package pl.shockah.unikorn.plugin

interface PluginDependencyInjector {
	fun injectRequiredDependenciesIntoPlugin(plugin: Plugin, loadedPlugins: Map<PluginInfo, Plugin>): Set<Plugin>
	fun injectOptionalDependenciesIntoPlugin(plugin: Plugin, loadedPlugins: Map<PluginInfo, Plugin>): Set<Plugin>
}