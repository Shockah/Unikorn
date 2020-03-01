package pl.shockah.unikorn.binbuffer

class ReadOnlyArrayBinaryBuffer(
		private val array: ByteArray
): BinaryBuffer.Readable {
	private var offset = 0

	override val readAvailable: Int
		get() = array.size - offset

	override fun readByte(): Byte {
		return array[offset++]
	}
}