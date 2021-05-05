package pl.shockah.unikorn.plugin.impl

import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.PluginDependencyInjector
import pl.shockah.unikorn.plugin.PluginInfo
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

object PluginDependency {
	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
	annotation class Required

	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
	annotation class Optional(val identifier: String)
}

class AnnotationPluginDependencyInjector: PluginDependencyInjector {
	private val <T: Plugin> KClass<T>.possibleDependencyProperties: Collection<KMutableProperty1<T, out Plugin>>
		get() = declaredMemberProperties.filter {
			(it.returnType.classifier as? KClass<*>)?.isSubclassOf(Plugin::class) ?: false
		}.filterIsInstance<KMutableProperty1<T, out Plugin>>()

	private val <T: Plugin> KClass<T>.possibleDependencyFunctions: Collection<KFunction<*>>
		get() = declaredMemberFunctions.filter {
			(it.parameters.singleOrNull()?.type?.classifier as? KClass<*>)?.isSubclassOf(Plugin::class) ?: false
		}

	override fun injectRequiredDependenciesIntoPlugin(plugin: Plugin, loadedPlugins: Map<PluginInfo, Plugin>): Set<Plugin> {
		val results = mutableSetOf<Plugin>()

		for (property in plugin::class.possibleDependencyProperties) {
			if (property.returnType.isMarkedNullable)
				continue
			if (property.findAnnotation<PluginDependency.Required>() == null)
				continue

			val loadedPlugin = loadedPlugins.values.firstOrNull { (property.returnType.classifier as? KClass<*>)?.isInstance(it) == true }
			if (loadedPlugin != null) {
				inject(property, plugin, loadedPlugin)
				results.add(loadedPlugin)
			}
		}

		for (function in plugin::class.possibleDependencyFunctions) {
			val parameter = function.parameters.single()
			if (parameter.type.isMarkedNullable)
				continue
			if (function.findAnnotation<PluginDependency.Required>() == null)
				continue

			val loadedPlugin = loadedPlugins.values.firstOrNull { (parameter.type.classifier as? KClass<*>)?.isInstance(it) == true }
			if (loadedPlugin != null) {
				inject(function, plugin, loadedPlugin)
				results.add(loadedPlugin)
			}
		}

		return results
	}

	override fun injectOptionalDependenciesIntoPlugin(plugin: Plugin, loadedPlugins: Map<PluginInfo, Plugin>): Set<Plugin> {
		val results = mutableSetOf<Plugin>()

		for (property in plugin::class.possibleDependencyProperties) {
			val dependencyAnnotation = property.findAnnotation<PluginDependency.Optional>() ?: continue

			val loadedPlugin = loadedPlugins.entries.firstOrNull { it.key.identifier == dependencyAnnotation.identifier && (property.returnType.classifier as? KClass<*>)?.isInstance(it.value) == true }?.value
			if (loadedPlugin != null) {
				inject(property, plugin, loadedPlugin)
				results.add(loadedPlugin)
			}
		}

		for (function in plugin::class.possibleDependencyFunctions) {
			val parameter = function.parameters.single()
			val dependencyAnnotation = function.findAnnotation<PluginDependency.Optional>() ?: continue

			val loadedPlugin = loadedPlugins.entries.firstOrNull { it.key.identifier == dependencyAnnotation.identifier && (parameter.type.classifier as? KClass<*>)?.isInstance(it.value) == true }?.value
			if (loadedPlugin != null) {
				inject(function, plugin, loadedPlugin)
				results.add(loadedPlugin)
			}
		}

		return results
	}

	private fun inject(property: KMutableProperty1<out Plugin, out Plugin>, newPlugin: Plugin, loadedPlugin: Plugin) {
		property.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		(property as KMutableProperty1<Plugin, Any>).set(newPlugin, loadedPlugin)
		newPlugin.onDependencyInjected(loadedPlugin)
	}

	private fun inject(function: KFunction<*>, newPlugin: Plugin, loadedPlugin: Plugin) {
		function.isAccessible = true
		function.call(loadedPlugin)
		newPlugin.onDependencyInjected(loadedPlugin)
	}
}