package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass

class Container(
		private val parent: Resolver? = null,
		var defaultComponentStorageFactory: ComponentStorageFactory = LazyComponentStorageFactory()
): Resolver {
	private val componentTypeToStorage = mutableMapOf<ComponentId<*, *>, ComponentStorage<out Any>>()

	override fun <T: Any, Key> resolve(id: ComponentId<T, Key>): T {
		@Suppress("UNCHECKED_CAST")
		return (componentTypeToStorage[id] as? ComponentStorage<T>)?.component ?: parent?.resolve(id) ?: throw MissingComponentException()
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

fun <T: Any> Container.register(type: KClass<T>, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory, factory: (Resolver) -> T): ComponentId<T, Unit> {
	return register(ComponentId(type), componentStorageFactory, factory)
}

fun <T: Any> Container.register(type: KClass<T>, component: T, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory): ComponentId<T, Unit> {
	return register(type, componentStorageFactory) { component }
}

inline fun <reified T: Any> Container.register(componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory, noinline factory: (Resolver) -> T): ComponentId<T, Unit> {
	return register(T::class, componentStorageFactory, factory)
}

inline fun <reified T: Any> Container.register(component: T, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory): ComponentId<T, Unit> {
	return register(T::class, component, componentStorageFactory)
}

fun <T: Any> Container.unregister(type: KClass<T>) {
	unregister(ComponentId(type))
}