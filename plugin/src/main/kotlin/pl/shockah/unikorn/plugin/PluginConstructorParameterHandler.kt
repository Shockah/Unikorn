package pl.shockah.unikorn.plugin

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

interface PluginConstructorParameterHandler {
	class UnhandledParameter: Exception()

	/**
	 * @throws UnhandledParameter if this handler cannot handle this type of parameter
	 */
	fun handleConstructorParameter(pluginInfo: PluginInfo, constructor: KFunction<Plugin>, parameter: KParameter): Any? {
		throw UnhandledParameter()
	}
}