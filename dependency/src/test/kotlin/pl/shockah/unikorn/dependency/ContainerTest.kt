package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ContainerTest {
	private val storageFactory = LazyComponentStorageFactory()

	@Test
	@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
	fun testRegisterAndUnregister() {
		val container = ContainerImpl(defaultComponentStorageFactory = storageFactory)

		container.register(Object())
		Assertions.assertDoesNotThrow { container.resolve<Object>() }

		container.unregister<Object>()
		Assertions.assertThrows(MissingComponentException::class.java) { container.resolve<Object>() }
	}
}