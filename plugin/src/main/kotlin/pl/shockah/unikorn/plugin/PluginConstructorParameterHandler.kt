package pl.shockah.unikorn.plugin

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

fun interface PluginConstructorParameterHandler {
	class UnhandledParameter: Exception()

	/**
	 * @throws UnhandledParameter if this handler cannot handle this type of parameter
	 */
	fun handleConstructorParameter(constructor: KFunction<Plugin>, parameter: KParameter): Any?
}

class InstancePluginConstructorParameterHandler<T: Any>(
		private val instance: T
): PluginConstructorParameterHandler {
	override fun handleConstructorParameter(constructor: KFunction<Plugin>, parameter: KParameter): Any {
		return instance.takeIf { (parameter.type.classifier as? KClass<*>)?.isInstance(it) == true } ?: throw PluginConstructorParameterHandler.UnhandledParameter()
	}
}