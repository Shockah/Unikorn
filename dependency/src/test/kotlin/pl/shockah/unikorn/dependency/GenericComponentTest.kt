package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GenericComponentTest {
	private val storageFactory = LazyComponentStorageFactory()

	@Test
	fun testDifferentGenerics() {
		val container = Container(defaultComponentStorageFactory = storageFactory)

		container.register<List<String>>(mutableListOf())
		container.register<List<Int>>(mutableListOf())

		val stringList = container.resolve<List<String>>()
		val intList = container.resolve<List<Int>>()
		Assertions.assertNotSame(stringList, intList)
	}
}