package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WeakComponentStorageTest {
	private class BlankResolver: Resolver {
		override fun <T: Any, Key> resolve(id: ComponentId<T, Key>): T {
			throw MissingComponentException()
		}
	}

	private class Component {
		companion object {
			private var nextValue = 0
		}

		val value = nextValue++
	}

	@Test
	fun test() {
		val storageFactory = WeakComponentStorageFactory()
		val storage = storageFactory.createComponentStorage(BlankResolver()) { Component() }

		var instance: Component? = storage.component
		val firstValue = instance!!.value
		instance = storage.component
		val secondValue = instance.value
		Assertions.assertEquals(secondValue, firstValue)

		@Suppress("UNUSED_VALUE")
		instance = null
		System.gc()

		instance = storage.component
		val thirdValue = instance.value
		Assertions.assertNotEquals(thirdValue, firstValue)
	}
}