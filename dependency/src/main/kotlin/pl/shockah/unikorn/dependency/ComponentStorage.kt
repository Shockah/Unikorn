package pl.shockah.unikorn.dependency

interface ComponentStorage<T> {
	val component: T
}

interface ComponentStorageFactory {
	fun <T> createComponentStorage(resolver: Resolver, factory: (Resolver) -> T): ComponentStorage<T>
}