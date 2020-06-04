package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass

class MissingComponentException: Exception()

data class ComponentId<T: Any, Key>(
		val type: KClass<T>,
		val key: Key
) {
	companion object {
		operator fun <T: Any> invoke(type: KClass<T>): ComponentId<T, Unit> {
			return ComponentId(type, Unit)
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
fun <T: Any> Resolver.resolve(type: KClass<T>): T {
	return resolve(ComponentId(type))
}

/**
 * @return requested component if available
 * @throws MissingComponentException if a component is not available
 */
inline fun <reified T: Any> Resolver.resolve(): T {
	return resolve(T::class)
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
fun <T: Any> Resolver.resolveIfPresent(type: KClass<T>): T? {
	try {
		return resolve(type)
	} catch (exception: MissingComponentException) {
		return null
	}
}

/**
 * @return requested component if available, `null` otherwise
 */
inline fun <reified T: Any> Resolver.resolveIfPresent(): T? {
	return resolve(T::class)
}