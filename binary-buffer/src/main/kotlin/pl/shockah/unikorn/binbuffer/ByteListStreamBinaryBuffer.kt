package pl.shockah.unikorn.binbuffer

import kotlin.math.min

class ByteListStreamBinaryBuffer(
		private val chunkSize: Int = 8192
): BinaryBuffer.Readable, BinaryBuffer.Writable {
	private val arrays = mutableListOf<ByteArray>()
	private var readOffset = 0
	private var buildingArray: ByteArray? = null
	private var writeOffset = 0

	override val readAvailable: Int
		get() = arrays.sumOf { it.size } - readOffset + writeOffset

	override fun readByte(): Byte {
		val array = arrays.firstOrNull() ?: buildingArray ?: throw ArrayIndexOutOfBoundsException()
		if (readAvailable == 0)
			throw ArrayIndexOutOfBoundsException()

		val value = array[readOffset++]
		if (readOffset >= array.size) {
			arrays.removeAt(0)
			readOffset = 0
		}
		return value
	}

	override fun writeByte(v: Byte) {
		if (buildingArray == null)
			buildingArray = ByteArray(chunkSize)

		buildingArray!![writeOffset++] = v
		if (writeOffset >= buildingArray!!.size) {
			arrays += buildingArray!!
			buildingArray = null
			writeOffset = 0
		}
	}

	override fun writeBytes(array: ByteArray, offset: Int, length: Int) {
		val endIndex = offset + length
		var currentOffset = offset
		while (currentOffset < endIndex) {
			if (buildingArray == null)
				buildingArray = ByteArray(chunkSize)

			val toWrite = min(endIndex - currentOffset, buildingArray!!.size - writeOffset)
			array.copyInto(buildingArray!!, writeOffset, currentOffset, currentOffset + toWrite)
			writeOffset += toWrite
			currentOffset += toWrite

			if (writeOffset >= buildingArray!!.size) {
				arrays += buildingArray!!
				buildingArray = null
				writeOffset = 0
			}
		}
	}

	override fun writeBytes(array: ByteArray, copyRequired: Boolean) {
		if (copyRequired) {
			super.writeBytes(array, copyRequired)
		} else {
			if (buildingArray != null) {
				if (arrays.isEmpty() && readOffset > 0) {
					arrays += buildingArray!!.copyOfRange(readOffset, writeOffset)
					readOffset = 0
				} else {
					arrays += buildingArray!!.copyOf(writeOffset)
				}
				buildingArray = null
				writeOffset = 0
			}
			arrays += array
		}
	}
}