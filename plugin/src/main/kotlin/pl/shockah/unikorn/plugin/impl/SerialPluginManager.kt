package pl.shockah.unikorn.plugin.impl

import pl.shockah.unikorn.plugin.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

class SerialPluginManager<PluginInfoType: PluginInfo>(
		private val infoProvider: PluginInfoProvider<PluginInfoType>,
		private val dependencyResolver: PluginDependencyResolver = PluginDependencyResolverImpl(),
		private val loaderFactory: PluginLoaderFactory<PluginInfoType>,
		private val dependencyInjector: PluginDependencyInjector? = AnnotationPluginDependencyInjector(),
		parameterHandlers: List<PluginConstructorParameterHandler> = emptyList()
): ReloadablePluginManager {
	private class PluginEntry<PluginInfoType: PluginInfo>(
			val info: PluginInfoType,
			val plugin: Plugin,
			var requiredDependencies: Set<Plugin> = emptySet(),
			var optionalDependencies: Set<Plugin> = emptySet()
	)

	private val lock = ReentrantLock()
	private val allParameterHandlers by lazy { listOf(InstancePluginConstructorParameterHandler(this)) + parameterHandlers }
	private var providedInfos: Set<PluginInfoType>? = null
	private var pluginEntries = mutableListOf<PluginEntry<PluginInfoType>>()

	override val allPluginInfos: Set<PluginInfo>
		get() = lock.withLock {
			val providedInfos = providedInfos
			if (providedInfos == null) {
				val newProvidedInfos = infoProvider.getPluginInfos()
				this.providedInfos = newProvidedInfos
				return@withLock newProvidedInfos
			} else {
				return@withLock providedInfos
			}
		}

	override val loadedPluginInfos: Set<PluginInfo>
		get() = lock.withLock { pluginEntries.map { it.info }.toSet() }

	override val unloadedPluginInfos: Set<PluginInfo>
		get() = lock.withLock { allPluginInfos - pluginEntries.map { it.info } }

	override val loadedPlugins: Map<PluginInfo, Plugin>
		get() = lock.withLock { pluginEntries.associate { it.info to it.plugin } }

	override fun loadPlugins(infos: Collection<PluginInfo>) {
		lock.withLock {
			val unknownInfos = infos - allPluginInfos
			if (unknownInfos.isNotEmpty())
				throw PluginLoadException.UnknownPluginInfo(unknownInfos.toSet())

			val alreadyLoadedInfos = infos.intersect(loadedPluginInfos)
			if (alreadyLoadedInfos.isNotEmpty())
				throw PluginLoadException.AlreadyLoaded(alreadyLoadedInfos)

			@Suppress("UNCHECKED_CAST")
			val typedInfos = infos.map { it as PluginInfoType }

			val resolveResult = dependencyResolver.resolvePluginDependencies(typedInfos, pluginEntries.map { it.info })
			if (resolveResult.unresolvableDueToMissingDependencies.isNotEmpty() || resolveResult.unresolvableChains.isNotEmpty())
				throw PluginLoadException.Unresolvable(resolveResult.unresolvableDueToMissingDependencies, resolveResult.unresolvableChains)

			val pluginLoader = loaderFactory.createPluginLoader((pluginEntries.map { it.info } + resolveResult.loadOrder.flatten()).toSet())
			val newlyLoadedPluginEntries = mutableSetOf<PluginEntry<PluginInfoType>>()
			for (loadStep in resolveResult.loadOrder) {
				for (infoToLoad in loadStep) {
					val infoParameterHandler = InstancePluginConstructorParameterHandler(infoToLoad)
					val requiredDependencies = infoToLoad.dependencies
							.mapNotNull { dependency -> pluginEntries.firstOrNull { dependency.matches(it.info) } }
							.map { it.plugin }
							.toMutableSet()
					val dependencyParameterHandler = PluginConstructorParameterHandler { _, parameter ->
						for (pluginEntry in pluginEntries) {
							if ((parameter.type.classifier as? KClass<*>)?.isInstance(pluginEntry.plugin) == true) {
								requiredDependencies.add(pluginEntry.plugin)
								return@PluginConstructorParameterHandler pluginEntry.plugin
							}
						}
						throw PluginConstructorParameterHandler.UnhandledParameter()
					}
					val parameterHandlers = listOf(infoParameterHandler, dependencyParameterHandler) + allParameterHandlers
					val plugin = pluginLoader.loadPlugin(infoToLoad, parameterHandlers)

					if (dependencyInjector != null) {
						requiredDependencies += dependencyInjector.injectRequiredDependenciesIntoPlugin(plugin, loadedPlugins)
						plugin.onRequiredDependenciesInjected()
					}

					val pluginEntry = PluginEntry(infoToLoad, plugin, requiredDependencies = requiredDependencies)
					newlyLoadedPluginEntries.add(pluginEntry)
					pluginEntries.add(pluginEntry)
				}
			}

			val allLoadedPlugins: Map<PluginInfo, Plugin> = loadedPlugins
			if (dependencyInjector != null) {
				newlyLoadedPluginEntries.forEach { it.optionalDependencies += dependencyInjector.injectOptionalDependenciesIntoPlugin(it.plugin, allLoadedPlugins) }
				newlyLoadedPluginEntries.forEach { it.plugin.onDependencyInjectionFinished() }
			}
			val immutableNewlyLoadedPlugins: Map<PluginInfo, Plugin> = newlyLoadedPluginEntries.associate { it.info to it.plugin }
			allLoadedPlugins.values.forEach { it.onPluginLoadCycleFinished(allLoadedPlugins, immutableNewlyLoadedPlugins) }
		}
	}

	override fun unloadPlugins(infos: Collection<PluginInfo>) {
		lock.withLock {
			val unknownInfos = infos - allPluginInfos
			if (unknownInfos.isNotEmpty())
				throw PluginLoadException.UnknownPluginInfo(unknownInfos.toSet())

			val alreadyUnloadedInfos = infos.intersect(unloadedPluginInfos)
			if (alreadyUnloadedInfos.isNotEmpty())
				throw PluginUnloadException.AlreadyUnloaded(alreadyUnloadedInfos)

			val pluginEntriesToUnload = pluginEntries.filter { infos.contains(it.info) }.reversed()
			val pluginsToUnload = pluginEntriesToUnload.map { it.plugin }
			val requirements = pluginEntries
					.filter { it !in pluginEntriesToUnload }
					.associateWith { it.requiredDependencies.intersect(pluginsToUnload) }
					.filterValues { it.isNotEmpty() }
					.mapKeys { it.key.info }
			if (requirements.isNotEmpty()) {
				val pluginToInfo = pluginEntries.associate { it.plugin to it.info }
				throw PluginUnloadException.Required(requirements.mapValues { it.value.map { pluginToInfo[it]!! }.toSet() })
			}

			pluginEntriesToUnload.forEach { unloadPluginEntry(it) }
			val allLoadedPlugins: Map<PluginInfo, Plugin> = loadedPlugins
			val unloadedPluginInfos: Set<PluginInfo> = pluginEntriesToUnload.map { it.info }.toSet()
			allLoadedPlugins.values.forEach { it.onPluginUnloadCycleFinished(allLoadedPlugins, unloadedPluginInfos) }
		}
	}

	override fun loadUnloadedPlugins(infos: Collection<PluginInfo>) {
		lock.withLock {
			val unknownInfos = infos - allPluginInfos
			if (unknownInfos.isNotEmpty())
				throw PluginLoadException.UnknownPluginInfo(unknownInfos.toSet())
			loadPlugins(infos.filter { unloadedPluginInfos.contains(it) })
		}
	}

	override fun loadAllPlugins() {
		lock.withLock {
			loadPlugins(unloadedPluginInfos)
		}
	}

	override fun unloadAllPlugins() {
		lock.withLock {
			pluginEntries.reversed().toList().forEach { unloadPluginEntry(it) }
			// no need to call onPluginUnloadCycleFinished - there are no plugins left to call on
		}
	}

	override fun reloadPluginInfos() {
		lock.withLock {
			providedInfos = infoProvider.getPluginInfos() + pluginEntries.map { it.info }
		}
	}

	private fun unloadPluginEntry(pluginEntry: PluginEntry<PluginInfoType>) {
		lock.withLock {
			pluginEntry.optionalDependencies.forEach { it.willUnloadDependency(pluginEntry.plugin) }
			pluginEntry.plugin.onUnload()
			pluginEntries.forEach { it.optionalDependencies -= pluginEntry.plugin }
			pluginEntries.remove(pluginEntry)
		}
	}
}