package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class TransientComponentStorageTest {
	private class BlankResolver: Resolver {
		override fun <T: Any> resolve(type: KClass<in T>): T {
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