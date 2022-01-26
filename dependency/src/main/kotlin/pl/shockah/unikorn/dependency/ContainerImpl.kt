package pl.shockah.unikorn.dependency

import kotlin.reflect.full.isSupertypeOf

class ContainerImpl(
    private val parent: Resolver? = null,
    override var defaultComponentStorageFactory: ComponentStorageFactory = LazyComponentStorageFactory()
): DefaultComponentStorageFactoryContainer {
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

    override fun <T: Any, Key> register(id: ComponentId<T, Key>, componentStorageFactory: ComponentStorageFactory, factory: (Resolver) -> T): ComponentId<T, Key> {
        componentTypeToStorage[id] = componentStorageFactory.createComponentStorage(this, factory)
        return id
    }

    override fun unregister(id: ComponentId<*, *>) {
        componentTypeToStorage.remove(id)
    }
}