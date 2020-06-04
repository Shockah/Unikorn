package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParentContainerTest {
	private val storageFactory = LazyComponentStorageFactory()

	@Test
	fun testResolvingFromParent() {
		val parentContainer = Container(defaultComponentStorageFactory = storageFactory)
		val childContainer = Container(parentContainer, storageFactory)

		parentContainer.register(Object())

		val parentInstance = parentContainer.resolve(Object::class)
		val childInstance = childContainer.resolve(Object::class)

		Assertions.assertEquals(parentInstance, childInstance)
	}

	@Test
	fun testResolvingFromChild() {
		val parentContainer = Container(defaultComponentStorageFactory = storageFactory)
		val childContainer = Container(parentContainer, storageFactory)

		childContainer.register(Object())

		Assertions.assertDoesNotThrow { childContainer.resolve(Object::class) }
		Assertions.assertThrows(MissingComponentException::class.java) { parentContainer.resolve(Object::class) }
	}

	@Test
	fun testOverridenComponent() {
		val parentContainer = Container(defaultComponentStorageFactory = storageFactory)
		val childContainer = Container(parentContainer, storageFactory)

		parentContainer.register(Object())
		childContainer.register(Object())

		val parentInstance = parentContainer.resolve(Object::class)
		val childInstance = childContainer.resolve(Object::class)

		Assertions.assertNotEquals(parentInstance, childInstance)
	}
}