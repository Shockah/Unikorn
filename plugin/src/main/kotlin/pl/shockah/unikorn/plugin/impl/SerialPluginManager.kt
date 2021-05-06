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
): PluginManager.Dynamic.FullUnload.Reload {
	private class PluginEntry<PluginInfoType: PluginInfo>(
			val info: PluginInfoType,
			val plugin: Plugin,
			var requiredDependencies: Set<Plugin> = emptySet(),
			var optionalDependencies: Set<Plugin> = emptySet()
	)

	private val lock = ReentrantLock()
	private val allParameterHandlers by lazy { listOf(InstancePluginConstructorParameterHandler(this)) + parameterHandlers }
	private var allPluginInfosStorage: Set<PluginInfoType>? = null
	private var pluginLoaderStorage: PluginLoader<PluginInfoType>? = null
	private var pluginEntries = mutableListOf<PluginEntry<PluginInfoType>>()

	override val allPluginInfos: Set<PluginInfo>
		get() = lock.withLock {
			val allPluginInfos = allPluginInfosStorage
			if (allPluginInfos == null) {
				val newAllPluginInfos = infoProvider.getPluginInfos()
				allPluginInfosStorage = newAllPluginInfos
				return@withLock newAllPluginInfos
			} else {
				return@withLock allPluginInfos
			}
		}

	override val loadedPluginInfos: Set<PluginInfo>
		get() = lock.withLock { pluginEntries.map { it.info }.toSet() }

	override val unloadedPluginInfos: Set<PluginInfo>
		get() = lock.withLock { allPluginInfos - pluginEntries.map { it.info } }

	override val loadedPlugins: Map<PluginInfo, Plugin>
		get() = lock.withLock { pluginEntries.associate { it.info to it.plugin } }

	private val pluginLoader: PluginLoader<PluginInfoType>
		get() = lock.withLock {
			val pluginLoader = pluginLoaderStorage
			if (pluginLoader == null) {
				@Suppress("UNCHECKED_CAST")
				val typedInfos = allPluginInfos.map { it as PluginInfoType }

				val newPluginLoader = loaderFactory.createPluginLoader(typedInfos)
				pluginLoaderStorage = newPluginLoader
				return@withLock newPluginLoader
			} else {
				return@withLock pluginLoader
			}
		}

	override fun loadPlugins(infos: Collection<PluginInfo>) {
		lock.withLock {
			val unknownInfos = infos - allPluginInfos
			if (unknownInfos.isNotEmpty())
				throw PluginLoadException.UnknownPluginInfo(unknownInfos.toSet())

			@Suppress("UNCHECKED_CAST")
			val typedInfos = (infos - loadedPluginInfos).map { it as PluginInfoType }

			val resolveResult = dependencyResolver.resolvePluginDependencies(typedInfos, pluginEntries.map { it.info })
			if (resolveResult.unresolvableDueToMissingDependencies.isNotEmpty() || resolveResult.unresolvableChains.isNotEmpty())
				throw PluginLoadException.Unresolvable(resolveResult.unresolvableDueToMissingDependencies, resolveResult.unresolvableChains)

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

			if (newlyLoadedPluginEntries.isEmpty())
				return@withLock

			val allLoadedPlugins: Map<PluginInfo, Plugin> = loadedPlugins
			if (dependencyInjector != null) {
				newlyLoadedPluginEntries.forEach { it.optionalDependencies += dependencyInjector.injectOptionalDependenciesIntoPlugin(it.plugin, allLoadedPlugins) }
				newlyLoadedPluginEntries.forEach { it.plugin.onDependencyInjectionFinished() }
			}
			val immutableNewlyLoadedPlugins: Map<PluginInfo, Plugin> = newlyLoadedPluginEntries.associate { it.info to it.plugin }
			allLoadedPlugins.values.forEach { it.onPluginLoadCycleFinished(allLoadedPlugins, immutableNewlyLoadedPlugins) }
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

	override fun unloadAllPluginsAndReloadPluginInfos() {
		lock.withLock {
			unloadAllPlugins()
			allPluginInfosStorage = infoProvider.getPluginInfos() + pluginEntries.map { it.info }
			pluginLoaderStorage = null
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