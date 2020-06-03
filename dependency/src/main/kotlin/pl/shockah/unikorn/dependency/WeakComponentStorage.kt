package pl.shockah.unikorn.dependency

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

class WeakComponentStorage<T>(
		private val resolver: Resolver,
		private val factory: (Resolver) -> T,
		private val referenceQueue: ReferenceQueue<Any>? = null
): ComponentStorage<T> {
	private var reference: WeakReference<T>? = null

	override val component: T
		get() {
			val value = reference?.get()
			if (value != null)
				return value

			val newValue = factory(resolver)
			@Suppress("UNCHECKED_CAST")
			reference = if (referenceQueue == null) WeakReference(newValue) else WeakReference(newValue, referenceQueue as ReferenceQueue<in T>)
			return newValue
		}
}

class WeakComponentStorageFactory(
		private val referenceQueue: ReferenceQueue<Any>? = null
): ComponentStorageFactory {
	override fun <T> createComponentStorage(resolver: Resolver, factory: (Resolver) -> T): ComponentStorage<T> {
		return WeakComponentStorage(resolver, factory, referenceQueue)
	}
}