package pl.shockah.unikorn.binbuffer

import kotlin.math.max
import kotlin.properties.Delegates.vetoable

class GrowingBinaryBuffer(
		initialCapacity: Int = 8192,
		override val endianness: Endianness = Endianness.LittleEndian
) : BinaryBuffer.Memory, BinaryBuffer.Readable.Data, BinaryBuffer.Writable.Data {
	companion object {
		private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8
	}

	private var array = ByteArray(initialCapacity)

	override var size = 0
		private set

	override val readAvailable: Int
		get() = size - readPosition

	override var readPosition by vetoable(0) { _, _, new ->
		if (new > size)
			throw ArrayIndexOutOfBoundsException()
		else
			return@vetoable true
	}

	override var writePosition by vetoable(0) { _, _, new ->
		if (new > size)
			throw ArrayIndexOutOfBoundsException()
		else
			return@vetoable true
	}

	private fun verifyReadSize(bytes: Int) {
		if (readAvailable < bytes)
			throw ArrayIndexOutOfBoundsException()
	}

	private fun ensureWriteCapacity(bytes: Int = 1) {
		if (bytes > array.size - writePosition) {
			fun hugeCapacity(minCapacity: Int): Int {
				if (minCapacity < 0)
					throw OutOfMemoryError()
				return if (minCapacity > MAX_ARRAY_SIZE)
					Integer.MAX_VALUE
				else
					MAX_ARRAY_SIZE
			}

			fun grow(minCapacity: Int) {
				val oldCapacity = array.size
				var newCapacity = oldCapacity shl 1
				if (newCapacity - minCapacity < 0)
					newCapacity = minCapacity
				if (newCapacity - MAX_ARRAY_SIZE > 0)
					newCapacity = hugeCapacity(minCapacity)
				array = array.copyOf(newCapacity)
			}

			grow(writePosition + bytes)
		}
	}

	override fun clear() {
		readPosition = 0
		writePosition = 0
		size = 0
	}

	override fun readByte(): Byte {
		verifyReadSize(1)
		val value = array[readPosition]
		readPosition++
		return value
	}

	override fun readBytes(array: ByteArray, offset: Int, length: Int) {
		verifyReadSize(length)
		this.array.copyInto(array, offset, readPosition, readPosition + length)
	}

	override fun writeByte(v: Byte) {
		ensureWriteCapacity(1)
		array[writePosition] = v
		size = max(size, writePosition + 1)
		writePosition++
	}

	override fun writeBytes(array: ByteArray, offset: Int, length: Int) {
		ensureWriteCapacity(length)
		array.copyInto(this.array, writePosition, offset, offset + length)
	}
}