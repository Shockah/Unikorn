package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass

class MissingComponentException: Exception()

interface Resolver {
	/**
	 * @return requested component if available
	 * @throws MissingComponentException if a component is not available
	 */
	fun <T: Any> resolve(type: KClass<in T>): T
}

/**
 * @return requested component if available
 * @throws MissingComponentException if a component is not available
 */
operator fun <T: Any> Resolver.get(type: KClass<in T>): T {
	return resolve(type)
}

/**
 * @return requested component if available, `null` otherwise
 */
fun <T: Any> Resolver.resolveIfPresent(type: KClass<in T>): T? {
	try {
		return resolve(type)
	} catch (exception: MissingComponentException) {
		return null
	}
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
inline fun <reified T: Any> Resolver.resolveIfPresent(): T? {
	return resolve(T::class)
}