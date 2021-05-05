package pl.shockah.unikorn.plugin

open class PluginLoadException: Exception {
	constructor(message: String): super(message)
	constructor(cause: Throwable): super(cause)
	constructor(message: String, cause: Throwable): super(message, cause)

	class UnknownPluginInfo(
			val infos: Set<PluginInfo>
	): PluginLoadException("Plugins are unknown: $infos.")

	class AlreadyLoaded(
			val infos: Set<PluginInfo>
	): PluginLoadException("Plugins are already loaded: $infos.")

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

	class AlreadyUnloaded(
			val infos: Set<PluginInfo>
	): PluginUnloadException("Plugins are already unloaded: $infos.")

	class Required(
			val requirements: Map<PluginInfo, Set<PluginInfo>>
	): PluginUnloadException("Cannot unload plugins, as they are still required by other plugins: $requirements.")
}

interface PluginManager {
	val allPluginInfos: Set<PluginInfo>
	val loadedPluginInfos: Set<PluginInfo>
	val unloadedPluginInfos: Set<PluginInfo>

	val loadedPlugins: Map<PluginInfo, Plugin>

	fun loadPlugins(infos: Collection<PluginInfo>)
	fun unloadPlugins(infos: Collection<PluginInfo>)

	fun loadUnloadedPlugins(infos: Collection<PluginInfo>) {
		val unknownInfos = infos - allPluginInfos
		if (unknownInfos.isNotEmpty())
			throw PluginLoadException.UnknownPluginInfo(unknownInfos.toSet())
		loadPlugins(infos.intersect(unloadedPluginInfos))
	}

	fun loadAllPlugins() {
		loadPlugins(unloadedPluginInfos)
	}

	fun unloadAllPlugins() {
		unloadPlugins(loadedPluginInfos)
	}
}

interface ReloadablePluginManager: PluginManager {
	fun reloadPluginInfos()
}