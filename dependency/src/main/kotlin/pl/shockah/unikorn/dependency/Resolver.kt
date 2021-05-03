package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class MissingComponentException: Exception()

data class ComponentId<T: Any, Key>(
		val type: KType,
		val klass: KClass<T>,
		val key: Key
) {
	companion object {
		operator fun <T: Any> invoke(type: KType, klass: KClass<T>): ComponentId<T, Unit> {
			return ComponentId(type, klass, Unit)
		}
	}
}

interface Resolver {
	/**
	 * @return requested component if available
	 * @throws MissingComponentException if a component is not available
	 */
	fun <T: Any, Key> resolve(id: ComponentId<T, Key>): T
}

/**
 * @return requested component if available
 * @throws MissingComponentException if a component is not available
 */
fun <T: Any> Resolver.resolve(type: KType, klass: KClass<T>): T {
	return resolve(ComponentId(type, klass))
}

/**
 * @return requested component if available
 * @throws MissingComponentException if a component is not available
 */
inline fun <reified T: Any> Resolver.resolve(): T {
	return resolve(typeOf<T>(), T::class)
}

/**
 * @return requested component if available, `null` otherwise
 */
fun <T: Any, Key> Resolver.resolveIfPresent(id: ComponentId<T, Key>): T? {
	try {
		return resolve(id)
	} catch (exception: MissingComponentException) {
		return null
	}
}

/**
 * @return requested component if available, `null` otherwise
 */
fun <T: Any> Resolver.resolveIfPresent(type: KType, klass: KClass<T>): T? {
	try {
		return resolve(type, klass)
	} catch (exception: MissingComponentException) {
		return null
	}
}

/**
 * @return requested component if available, `null` otherwise
 */
inline fun <reified T: Any> Resolver.resolveIfPresent(): T? {
	return resolveIfPresent(typeOf<T>(), T::class)
}