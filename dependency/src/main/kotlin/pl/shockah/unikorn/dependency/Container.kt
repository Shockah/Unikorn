package pl.shockah.unikorn.dependency

import java.util.*
import kotlin.reflect.KClass

class Container(
		private val parent: Resolver? = null,
		var defaultComponentStorageFactory: ComponentStorageFactory = LazyComponentStorageFactory()
): Resolver {
	private val componentHandleToType = mutableMapOf<ComponentHandle<out Any>, KClass<out Any>>()
	private val componentTypeToStorage = mutableMapOf<KClass<out Any>, ComponentStorage<out Any>>()

	override fun <T: Any> resolve(type: KClass<in T>): T {
		@Suppress("UNCHECKED_CAST")
		return (componentTypeToStorage[type] as? ComponentStorage<T>)?.component ?: parent?.resolve(type) ?: throw MissingComponentException()
	}

	fun <T: Any> register(type: KClass<in T>, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory, factory: (Resolver) -> T): ComponentHandle<in T> {
		val handle = ComponentHandle(type, UUID.randomUUID())
		val storage = componentStorageFactory.createComponentStorage(this, factory)
		componentTypeToStorage[type] = storage
		componentHandleToType[handle] = type
		return handle
	}

	fun <T: Any> unregister(handle: ComponentHandle<T>) {
		val type = componentHandleToType[handle]
		if (type != null) {
			componentHandleToType.remove(handle)
			componentTypeToStorage.remove(type)
		}
	}

	data class ComponentHandle<T: Any>(
			val type: KClass<T>,
			private val uuid: UUID
	)
}

inline fun <reified T: Any> Container.register(componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory, noinline factory: (Resolver) -> T): Container.ComponentHandle<in T> {
	return register(T::class, componentStorageFactory, factory)
}

fun <T: Any> Container.register(type: KClass<in T>, component: T, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory): Container.ComponentHandle<in T> {
	return register(type, componentStorageFactory) { component }
}

inline fun <reified T: Any> Container.register(component: T, componentStorageFactory: ComponentStorageFactory = defaultComponentStorageFactory): Container.ComponentHandle<in T> {
	return register(T::class, component, componentStorageFactory)
}