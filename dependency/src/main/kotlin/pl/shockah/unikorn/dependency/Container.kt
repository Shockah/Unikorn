package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.typeOf

class Container(
		private val parent: Resolver? = null,
		var defaultComponentStorageFactory: ComponentStorageFactory = LazyComponentStorageFactory()
): Resolver {
	private val componentTypeToStorage = mutableMapOf<ComponentId<*, *>, ComponentStorage<out Any>>()

	override fun <T: Any, Key> resolve(id: ComponentId<T, Key>, subtypes: Resolver.Subtypes): T {
		@Suppress("UNCHECKED_CAST")
		val exactStorage = componentTypeToStorage[id] as? ComponentStorage<T>
		if (exactStorage != null)
			return exactStorage.component

		if (parent != null) {
			try {
				return parent.resolve(id, Resolver.Subtypes.ExactOnly)
			} catch (_: MissingComponentException) { }
		}

		when (subtypes) {
			Resolver.Subtypes.ExactOnly -> throw MissingComponentException()
			is Resolver.Subtypes.Allow -> {
				if (!subtypes.ignoredTypes.contains(id.type)) {
					for ((mapId, storage) in componentTypeToStorage) {
						if (!id.type.isSupertypeOf(mapId.type))
							continue
						if (id.key != mapId.key)
							continue
						@Suppress("UNCHECKED_CAST")
						return (storage as ComponentStorage<T>).component
					}
				}

				if (parent != null)
					return parent.resolve(id, subtypes)
				throw MissingComponentException()
			}
		}
	}

	fun <T: Any, Key> register(id: ComponentId<T, Key>, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory, factory: (Resolver) -> T): ComponentId<T, Key> {
		componentTypeToStorage[id] = componentStorageFactory.createComponentStorage(this, factory)
		return id
	}

	fun unregister(id: ComponentId<*, *>) {
		componentTypeToStorage.remove(id)
	}
}

fun <T: Any, Key> Container.register(id: ComponentId<T, Key>, component: T, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory): ComponentId<T, Key> {
	return register(id, componentStorageFactory) { component }
}

fun <T: Any> Container.register(type: KType, klass: KClass<T>, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory, factory: (Resolver) -> T): ComponentId<T, Unit> {
	return register(ComponentId(type, klass), componentStorageFactory, factory)
}

fun <T: Any> Container.register(type: KType, klass: KClass<T>, component: T, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory): ComponentId<T, Unit> {
	return register(type, klass, componentStorageFactory) { component }
}

inline fun <reified T: Any> Container.register(componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory, noinline factory: (Resolver) -> T): ComponentId<T, Unit> {
	return register(typeOf<T>(), T::class, componentStorageFactory, factory)
}

inline fun <reified T: Any> Container.register(component: T, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory): ComponentId<T, Unit> {
	return register(typeOf<T>(), T::class, component, componentStorageFactory)
}

fun <T: Any> Container.unregister(type: KType, klass: KClass<T>) {
	unregister(ComponentId(type, klass))
}

inline fun <reified T: Any> Container.unregister() {
	unregister(typeOf<T>(), T::class)
}