package pl.shockah.unikorn.plugin

open class PluginLoadException: Exception {
	constructor(message: String): super(message)
	constructor(cause: Throwable): super(cause)
	constructor(message: String, cause: Throwable): super(message, cause)

	class UnknownPluginInfo(
			val infos: Set<PluginInfo>
	): PluginLoadException("Plugins are unknown: $infos.")

	class Unresolvable(
			val dueToMissingDependencies: Set<PluginDependencyResolveResult.UnresolvableDueToMissingDependencies<*>>,
			val chains: Set<PluginDependencyResolveResult.UnresolvableChain<*>>
	): PluginLoadException("Unresolvable: [missingDependencies: $dueToMissingDependencies, chains: $chains]")
}

open class PluginUnloadException: Exception {
	constructor(message: String): super(message)
	constructor(cause: Throwable): super(cause)
	constructor(message: String, cause: Throwable): super(message, cause)

	class UnknownPluginInfo(
			val infos: Set<PluginInfo>
	): PluginUnloadException("Plugins are unknown: $infos.")

	class Required(
			val requirements: Map<PluginInfo, Set<PluginInfo>>
	): PluginUnloadException("Cannot unload plugins, as they are still required by other plugins: $requirements.")
}

interface PluginManager {
	val loadedPluginInfos: Set<PluginInfo>
	val loadedPlugins: Map<PluginInfo, Plugin>

	interface Dynamic: PluginManager {
		val allPluginInfos: Set<PluginInfo>
		val unloadedPluginInfos: Set<PluginInfo>

		fun loadPlugins(infos: Collection<PluginInfo>)

		fun loadAllPlugins() {
			loadPlugins(unloadedPluginInfos)
		}

		interface FullUnload: Dynamic {
			fun unloadAllPlugins()

			interface Reload: FullUnload {
				fun unloadAllPluginsAndReloadPluginInfos()
			}
		}

		interface PartialUnload: FullUnload {
			override fun unloadAllPlugins() {
				unloadPlugins(loadedPluginInfos)
			}

			fun unloadPlugins(infos: Collection<PluginInfo>)

			interface Reload: PartialUnload {
				fun reloadPluginInfos()
			}
		}
	}
}