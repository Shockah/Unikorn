package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
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
	sealed class Subtypes {
		object ExactOnly: Subtypes()

		data class Allow(
				val ignoredTypes: Set<KType> = defaultIgnoredTypes
		): Subtypes() {
			companion object {
				val defaultIgnoredTypes = setOf(typeOf<Any>())
			}
		}
	}

	/**
	 * @return requested component if available
	 * @throws MissingComponentException if a component is not available
	 */
	fun <T: Any, Key> resolve(id: ComponentId<T, Key>, subtypes: Subtypes = Subtypes.Allow()): T
}

inline operator fun <reified T> Resolver.getValue(thisRef: Any?, property: KProperty<*>): T {
	return resolve()
}

fun Resolver.delegate(subtypes: Resolver.Subtypes = Resolver.Subtypes.Allow()): ResolverDelegate {
	return ResolverDelegate(this, subtypes)
}

class ResolverDelegate(
		@PublishedApi internal val resolver: Resolver,
		@PublishedApi internal val subtypes: Resolver.Subtypes
) {
	inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
		return resolver.resolve(subtypes)
	}
}

/**
 * @return requested component if available
 * @throws MissingComponentException if a component is not available
 */
fun <T: Any> Resolver.resolve(type: KType, klass: KClass<T>, subtypes: Resolver.Subtypes = Resolver.Subtypes.Allow()): T {
	return resolve(ComponentId(type, klass), subtypes)
}

/**
 * @return requested component if available
 * @throws MissingComponentException if a component is not available
 */
inline fun <reified T: Any> Resolver.resolve(subtypes: Resolver.Subtypes = Resolver.Subtypes.Allow()): T {
	return resolve(typeOf<T>(), T::class, subtypes)
}

/**
 * @return requested component if available, `null` otherwise
 */
fun <T: Any, Key> Resolver.resolveIfPresent(id: ComponentId<T, Key>, subtypes: Resolver.Subtypes = Resolver.Subtypes.Allow()): T? {
	try {
		return resolve(id, subtypes)
	} catch (exception: MissingComponentException) {
		return null
	}
}

/**
 * @return requested component if available, `null` otherwise
 */
fun <T: Any> Resolver.resolveIfPresent(type: KType, klass: KClass<T>, subtypes: Resolver.Subtypes = Resolver.Subtypes.Allow()): T? {
	try {
		return resolve(type, klass, subtypes)
	} catch (exception: MissingComponentException) {
		return null
	}
}

/**
 * @return requested component if available, `null` otherwise
 */
inline fun <reified T: Any> Resolver.resolveIfPresent(subtypes: Resolver.Subtypes = Resolver.Subtypes.Allow()): T? {
	return resolveIfPresent(typeOf<T>(), T::class, subtypes)
}