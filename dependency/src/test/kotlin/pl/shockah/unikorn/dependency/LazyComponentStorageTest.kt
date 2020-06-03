package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LazyComponentStorageTest {
	private class BlankResolver: Resolver {
		override fun <T: Any, Key> resolve(id: ComponentId<T, Key>): T {
			throw MissingComponentException()
		}
	}

	@Test
	fun test() {
		val storageFactory = LazyComponentStorageFactory(LazyThreadSafetyMode.NONE)
		val storage = storageFactory.createComponentStorage(BlankResolver()) { Object() }

		val firstInstance = storage.component
		val secondInstance = storage.component

		Assertions.assertEquals(secondInstance, firstInstance)
	}
}