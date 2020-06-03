package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ContainerTest {
	private val storageFactory = LazyComponentStorageFactory()

	@Test
	fun testRegisterAndUnregister() {
		val container = Container(defaultComponentStorageFactory = storageFactory)

		container.register(Object())
		Assertions.assertDoesNotThrow { container[Object::class] }

		container.unregister(Object::class)
		Assertions.assertThrows(MissingComponentException::class.java) { container[Object::class] }
	}
}