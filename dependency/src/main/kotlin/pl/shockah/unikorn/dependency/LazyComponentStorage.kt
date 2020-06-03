package pl.shockah.unikorn.dependency

class LazyComponentStorage<T>(
		private val resolver: Resolver,
		private val factory: (Resolver) -> T,
		mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED
): ComponentStorage<T> {
	override val component: T by lazy(mode) { factory(resolver) }
}

class LazyComponentStorageFactory(
		val mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED
): ComponentStorageFactory {
	override fun <T> createComponentStorage(resolver: Resolver, factory: (Resolver) -> T): ComponentStorage<T> {
		return LazyComponentStorage(resolver, factory, mode)
	}
}