package pl.shockah.unikorn.dependency

class TransientComponentStorage<T>(
		private val resolver: Resolver,
		private val factory: (Resolver) -> T
): ComponentStorage<T> {
	override val component: T
		get() = factory(resolver)
}

class TransientComponentStorageFactory: ComponentStorageFactory {
	override fun <T> createComponentStorage(resolver: Resolver, factory: (Resolver) -> T): ComponentStorage<T> {
		return TransientComponentStorage(resolver, factory)
	}
}