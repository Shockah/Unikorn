package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.typeOf

interface Container: Resolver {
	fun <T: Any, Key> register(id: ComponentId<T, Key>, componentStorageFactory: ComponentStorageFactory, factory: (Resolver) -> T): ComponentId<T, Key>
	fun unregister(id: ComponentId<*, *>)
}

fun <T: Any, Key> Container.register(id: ComponentId<T, Key>, component: T, componentStorageFactory: ComponentStorageFactory): ComponentId<T, Key> {
	return register(id, componentStorageFactory) { component }
}

fun <T: Any> Container.register(type: KType, klass: KClass<T>, componentStorageFactory: ComponentStorageFactory, factory: (Resolver) -> T): ComponentId<T, Unit> {
	return register(ComponentId(type, klass), componentStorageFactory, factory)
}

fun <T: Any> Container.register(type: KType, klass: KClass<T>, component: T, componentStorageFactory: ComponentStorageFactory): ComponentId<T, Unit> {
	return register(type, klass, componentStorageFactory) { component }
}

inline fun <reified T: Any> Container.register(componentStorageFactory: ComponentStorageFactory, noinline factory: (Resolver) -> T): ComponentId<T, Unit> {
	return register(typeOf<T>(), T::class, componentStorageFactory, factory)
}

inline fun <reified T: Any> Container.register(component: T, componentStorageFactory: ComponentStorageFactory): ComponentId<T, Unit> {
	return register(typeOf<T>(), T::class, component, componentStorageFactory)
}

fun <T: Any> Container.unregister(type: KType, klass: KClass<T>) {
	unregister(ComponentId(type, klass))
}

inline fun <reified T: Any> Container.unregister() {
	unregister(typeOf<T>(), T::class)
}