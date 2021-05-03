package pl.shockah.unikorn.plugin

import mu.KotlinLogging
import pl.shockah.unikorn.guard
import java.io.File
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

private val <T: Plugin> KClass<T>.possibleDependencyProperties: Collection<KMutableProperty1<T, out Plugin>>
	get() = declaredMemberProperties.filter {
		(it.returnType.classifier as? KClass<*>)?.isSubclassOf(Plugin::class) ?: false
	}.filterIsInstance<KMutableProperty1<T, out Plugin>>()

class PluginManager(
		infoProvider: PluginInfo.Provider,
		additionalPluginConstructorParameterHandlers: List<PluginConstructorParameterHandler> = emptyList()
) {
	private val logger = KotlinLogging.logger { }

	constructor(pluginDirectory: File): this(PluginInfo.Provider.Default(pluginDirectory))

	private val lock = ReentrantLock()
	private val pluginInfos = infoProvider.provide()
	private val plugins = mutableListOf<Plugin>()
	private val loadQueue = LinkedList<PluginInfo>()

	private val pluginConstructorParameterHandlers: List<PluginConstructorParameterHandler> = listOf(
			PluginManagerPluginConstructorParameterHandler(),
			PluginInfoPluginConstructorParameterHandler(),
			PluginDependencyPluginConstructorParameterHandler()
	) + additionalPluginConstructorParameterHandlers

	init {
		pluginInfos.forEach {
			logger.info { "Loaded plugin info: ${it.identifier}" }
		}
	}

	operator fun get(info: PluginInfo): Plugin? {
		lock.withLock {
			return plugins.firstOrNull { it.info == info }
		}
	}

	operator fun get(identifier: String): Plugin? {
		lock.withLock {
			return plugins.firstOrNull { it.info.identifier == identifier }
		}
	}

	fun loadAll(): List<Plugin> {
		lock.withLock {
			loadQueue += pluginInfos
			logger.debug { "Added to load queue: $pluginInfos" }
			loadSweep()
			return plugins.toList()
		}
	}

	fun load(info: PluginInfo): Plugin {
		return load(listOf(info)).values.first()
	}

	fun load(infos: List<PluginInfo>): Map<PluginInfo, Plugin> {
		lock.withLock {
			loadQueue += infos
			logger.debug { "Added to load queue: $infos" }
			loadSweep()
			return infos.associateWith { this[it]!! }
		}
	}

	private fun loadSweep() {
		lock.withLock {
			loadQueue += plugins.map { it.info }
			unloadAll()
			val classLoader = URLClassLoader(loadQueue.map { it.url }.toTypedArray())
			Thread.currentThread().contextClassLoader = classLoader

			fun PluginInfo.DependencyEntry.dependencyLoaded(): Boolean {
				return plugins.any { matches(it.info) }
			}

			fun PluginInfo.allDependenciesLoaded(): Boolean {
				return dependencies.all { it.dependencyLoaded() }
			}

			while (!loadQueue.isEmpty()) {
				val oldSize = loadQueue.size

				val iterator = loadQueue.iterator()
				while (iterator.hasNext()) {
					val info = iterator.next()
					logger.trace { "Trying to load $info" }
					if (!info.allDependenciesLoaded())
						continue

					logger.trace { "Dependencies already loaded, proceeding to load $info" }
					val plugin = loadInternal(classLoader, info)
					iterator.remove()
					logger.info { "Loaded plugin $plugin" }
				}

				check(oldSize != loadQueue.size) { "Couldn't load plugins: ${loadQueue.joinToString(", ") { "${it.identifier} ${it.version}" }}" }
			}

			plugins.forEach {
				it.onAllPluginsLoaded()
			}
		}
	}

	private fun loadInternal(classLoader: ClassLoader, info: PluginInfo): Plugin {
		lock.withLock {
			logger.debug { "Loading $info" }
			val plugin = loadInstance(classLoader, info)
			logger.debug { "Loaded instance $plugin" }
			setupRequiredDependencies(plugin)
			logger.debug { "Set up required dependencies of $plugin" }
			plugins += plugin
			setupOptionalDependencies(plugin)
			logger.debug { "Set up optional dependencies of $plugin" }
			return plugin
		}
	}

	private fun loadInstance(classLoader: ClassLoader, info: PluginInfo): Plugin {
		if (info.pluginClassName == null) {
			return Plugin(this, info)
		} else {
			@Suppress("UNCHECKED_CAST")
			val clazz = classLoader.loadClass(info.pluginClassName).kotlin as KClass<out Plugin>

			clazz.constructors.forEach { ctor ->
				val callParameters = mutableMapOf<KParameter, Any?>()
				for (parameter in ctor.parameters) {
					for (handler in pluginConstructorParameterHandlers) {
						try {
							callParameters[parameter] = handler.handleConstructorParameter(info, ctor, parameter)
							break
						} catch (_: PluginConstructorParameterHandler.UnhandledParameter) { }
					}
				}
				return ctor.callBy(callParameters)
			}

			throw NoSuchMethodException("Missing plugin constructor for ${info.identifier}")
		}
	}

	private fun setupRequiredDependencies(newPlugin: Plugin) {
		lock.withLock {
			newPlugin::class.possibleDependencyProperties.forEach { prop ->
				if (prop.returnType.isMarkedNullable)
					return@forEach
				prop.findAnnotation<Plugin.RequiredDependency>().guard { return@forEach }

				plugins.firstOrNull { (prop.returnType.classifier as KClass<*>).isInstance(it) }?.let { loadedPlugin ->
					prop.isAccessible = true
					@Suppress("UNCHECKED_CAST")
					(prop as KMutableProperty1<Plugin, Any>).set(newPlugin, loadedPlugin)
					return@forEach
				}
			}
			newPlugin.onRequiredDependenciesLoaded()
		}
	}

	private fun setupOptionalDependencies(newPlugin: Plugin) {
		lock.withLock {
			newPlugin::class.possibleDependencyProperties.forEach { prop ->
				if (!prop.returnType.isMarkedNullable)
					return@forEach
				val dependencyAnnotation = prop.findAnnotation<Plugin.OptionalDependency>().guard { return@forEach }

				plugins.firstOrNull { it.info.identifier == dependencyAnnotation.value }?.let { loadedPlugin ->
					@Suppress("UNCHECKED_CAST")
					(prop as KMutableProperty1<Plugin, Any>).set(newPlugin, loadedPlugin)
					newPlugin.onDependencyLoaded(loadedPlugin)
					return@forEach
				}
			}

			plugins.forEach { plugin ->
				plugin::class.possibleDependencyProperties.forEach properties@ { prop ->
					if (!prop.returnType.isMarkedNullable)
						return@forEach
					val dependencyAnnotation = prop.findAnnotation<Plugin.OptionalDependency>().guard { return@properties }

					if (dependencyAnnotation.value == newPlugin.info.identifier) {
						prop.isAccessible = true
						@Suppress("UNCHECKED_CAST")
						(prop as KMutableProperty1<Plugin, Any>).set(plugin, newPlugin)
						plugin.onDependencyLoaded(newPlugin)
					}
				}
			}
		}
	}

	fun unloadAll() {
		lock.withLock {
			plugins.asReversed().forEach {
				it.onUnload()
				logger.info { "Unloaded $it" }
			}
			plugins.clear()
		}
	}

	private inner class PluginManagerPluginConstructorParameterHandler: PluginConstructorParameterHandler {
		override fun handleConstructorParameter(pluginInfo: PluginInfo, constructor: KFunction<Plugin>, parameter: KParameter): Any {
			if (parameter.type.classifier == PluginManager::class)
				return this@PluginManager
			throw PluginConstructorParameterHandler.UnhandledParameter()
		}
	}

	private class PluginInfoPluginConstructorParameterHandler: PluginConstructorParameterHandler {
		override fun handleConstructorParameter(pluginInfo: PluginInfo, constructor: KFunction<Plugin>, parameter: KParameter): Any {
			if (parameter.type.classifier == PluginInfo::class)
				return pluginInfo
			throw PluginConstructorParameterHandler.UnhandledParameter()
		}
	}

	private inner class PluginDependencyPluginConstructorParameterHandler: PluginConstructorParameterHandler {
		override fun handleConstructorParameter(pluginInfo: PluginInfo, constructor: KFunction<Plugin>, parameter: KParameter): Any? {
			val classifier = parameter.type.classifier
			if (classifier is KClass<*>) {
				for (plugin in plugins) {
					if (classifier.isInstance(plugin))
						return plugin
				}
				if (parameter.type.isMarkedNullable && classifier.isSubclassOf(Plugin::class))
					return null
			}
			throw PluginConstructorParameterHandler.UnhandledParameter()
		}
	}
}