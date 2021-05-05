package pl.shockah.unikorn.plugin

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

interface PluginLoaderFactory<PluginInfoType: PluginInfo> {
	fun createPluginLoader(pluginInfos: Set<PluginInfoType>): PluginLoader<PluginInfoType>
}

interface PluginLoader<PluginInfoType: PluginInfo> {
	fun loadPlugin(info: PluginInfoType, parameterHandlers: List<PluginConstructorParameterHandler>): Plugin
}

abstract class ClassPluginLoader<PluginInfoType: PluginInfo>: PluginLoader<PluginInfoType> {
	abstract fun loadPluginClass(info: PluginInfoType): KClass<out Plugin>

	override fun loadPlugin(info: PluginInfoType, parameterHandlers: List<PluginConstructorParameterHandler>): Plugin {
		val pluginKlazz = loadPluginClass(info)
		val satisfiedConstructors = getSatisfiedConstructors(pluginKlazz, parameterHandlers)
		val satisfiedConstructor = satisfiedConstructors.firstOrNull() ?: throw NoSuchMethodException("Missing satisfied plugin constructor for ${info.identifier}.")
		return satisfiedConstructor.first.callBy(satisfiedConstructor.second)
	}

	protected fun getSatisfiedConstructors(klazz: KClass<out Plugin>, parameterHandlers: List<PluginConstructorParameterHandler>): List<Pair<KFunction<Plugin>, Map<KParameter, Any?>>> {
		val results = mutableListOf<Pair<KFunction<Plugin>, Map<KParameter, Any?>>>()
		constructors@ for (constructor in klazz.constructors) {
			val callParameters = mutableMapOf<KParameter, Any?>()
			for (parameter in constructor.parameters) {
				for (handler in parameterHandlers) {
					try {
						callParameters[parameter] = handler.handleConstructorParameter(constructor, parameter)
						break
					} catch (_: PluginConstructorParameterHandler.UnhandledParameter) { }
				}
			}
			results.add(constructor to callParameters)
		}
		return results.sortedByDescending { it.second.size }
	}
}