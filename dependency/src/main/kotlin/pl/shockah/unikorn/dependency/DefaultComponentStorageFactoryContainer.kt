package pl.shockah.unikorn.dependency

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DefaultComponentStorageFactoryContainer: Container {
    var defaultComponentStorageFactory: ComponentStorageFactory

    fun <T: Any, Key> register(id: ComponentId<T, Key>, factory: (Resolver) -> T): ComponentId<T, Key> {
        return register(id, defaultComponentStorageFactory, factory)
    }
}

fun <T: Any, Key> DefaultComponentStorageFactoryContainer.register(id: ComponentId<T, Key>, component: T): ComponentId<T, Key> {
    return register(id, defaultComponentStorageFactory) { component }
}

fun <T: Any> DefaultComponentStorageFactoryContainer.register(type: KType, klass: KClass<T>, factory: (Resolver) -> T): ComponentId<T, Unit> {
    return register(ComponentId(type, klass), defaultComponentStorageFactory, factory)
}

fun <T: Any> DefaultComponentStorageFactoryContainer.register(type: KType, klass: KClass<T>, component: T): ComponentId<T, Unit> {
    return register(type, klass, defaultComponentStorageFactory) { component }
}

inline fun <reified T: Any> DefaultComponentStorageFactoryContainer.register(noinline factory: (Resolver) -> T): ComponentId<T, Unit> {
    return register(typeOf<T>(), T::class, defaultComponentStorageFactory, factory)
}

inline fun <reified T: Any> DefaultComponentStorageFactoryContainer.register(component: T): ComponentId<T, Unit> {
    return register(typeOf<T>(), T::class, component, defaultComponentStorageFactory)
}