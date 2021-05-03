package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TransientComponentStorageTest {
	private class BlankResolver: Resolver {
		override fun <T: Any, Key> resolve(id: ComponentId<T, Key>, subtypes: Resolver.Subtypes): T {
			throw MissingComponentException()
		}
	}

	@Test
	fun test() {
		val storageFactory = TransientComponentStorageFactory()
		val storage = storageFactory.createComponentStorage(BlankResolver()) { Object() }

		val firstInstance = storage.component
		val secondInstance = storage.component

		Assertions.assertNotEquals(secondInstance, firstInstance)
	}
}