package pl.shockah.unikorn

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.shockah.unikorn.io.Endianness
import pl.shockah.unikorn.io.GrowingBinaryBuffer

class TestGrowingBinaryBuffer {
	@Test
	fun readWriteByte() {
		GrowingBinaryBuffer().run {
			assertEquals(0, size)
			assertEquals(0, readAvailable)
			assertEquals(0, readPosition)
			assertEquals(0, writePosition)

			writeByte(1)
			assertEquals(1, size)
			assertEquals(1, readAvailable)
			assertEquals(0, readPosition)
			assertEquals(1, writePosition)

			val b = readByte()
			assertEquals(1, b)
			assertEquals(0, readAvailable)
			assertEquals(1, readPosition)
		}
	}

	@Test
	fun readWriteInt() {
		fun GrowingBinaryBuffer.test() {
			writeInt(123)
			writeInt(257)
			writeInt(Int.MAX_VALUE)
			writeInt(Int.MIN_VALUE)

			assertEquals(16, size)
			assertEquals(16, readAvailable)
			assertEquals(16, writePosition)

			assertEquals(123, readInt())
			assertEquals(257, readInt())
			assertEquals(Int.MAX_VALUE, readInt())
			assertEquals(Int.MIN_VALUE, readInt())

			assertEquals(0, readAvailable)
			assertEquals(16, readPosition)
		}

		GrowingBinaryBuffer(endianness = Endianness.LittleEndian).test()
		GrowingBinaryBuffer(endianness = Endianness.BigEndian).test()
	}
}