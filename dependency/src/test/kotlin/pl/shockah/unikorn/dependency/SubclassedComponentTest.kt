package pl.shockah.unikorn.dependency

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier

class SubclassedComponentTest {
	private val storageFactory = LazyComponentStorageFactory()

	@Test
	@Suppress("UNCHECKED_CAST")
	fun testSubclassing() {
		val container = ContainerImpl(defaultComponentStorageFactory = storageFactory)

		container.register(ArrayList<String>())

		val arrayList = Assertions.assertDoesNotThrow(ThrowingSupplier { container.resolve<ArrayList<String>>() })
		val mutableList = Assertions.assertDoesNotThrow(ThrowingSupplier { container.resolve<MutableList<String>>() })
		val list = Assertions.assertDoesNotThrow(ThrowingSupplier { container.resolve<List<String>>() })

		Assertions.assertSame(arrayList, mutableList)
		Assertions.assertSame(mutableList, list)
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun testNoSubclassing() {
		val container = ContainerImpl(defaultComponentStorageFactory = storageFactory)

		container.register(ArrayList<String>())

		Assertions.assertDoesNotThrow { container.resolve<ArrayList<String>>(Resolver.Subtypes.ExactOnly) }
		Assertions.assertThrows(MissingComponentException::class.java) { container.resolve<MutableList<String>>(Resolver.Subtypes.ExactOnly) }
		Assertions.assertThrows(MissingComponentException::class.java) { container.resolve<List<String>>(Resolver.Subtypes.ExactOnly) }
	}
}