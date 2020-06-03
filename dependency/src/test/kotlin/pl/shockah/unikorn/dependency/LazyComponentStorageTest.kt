package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class LazyComponentStorageTest {
	private class BlankResolver: Resolver {
		override fun <T: Any> resolve(type: KClass<in T>): T {
			throw MissingComponentException()
		}
	}

	@Test
	fun test() {
		val storageFactory = LazyComponentStorageFactory()
		val storage = storageFactory.createComponentStorage(BlankResolver()) { Object() }

		val firstInstance = storage.component
		val secondInstance = storage.component

		Assertions.assertEquals(secondInstance, firstInstance)
	}
}