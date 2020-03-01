package pl.shockah.unikorn.binbuffer

import java.nio.ByteBuffer
import java.nio.charset.Charset

fun BinaryBuffer.Readable.asData(endianness: Endianness = Endianness.LittleEndian): BinaryBuffer.Readable.Data {
	return object: BinaryBuffer.Readable.Data {
		override val readAvailable: Int
			get() = this@asData.readAvailable

		override val endianness: Endianness
			get() = endianness

		override fun readByte(): Byte {
			return this@asData.readByte()
		}
	}
}

fun BinaryBuffer.Writable.asData(endianness: Endianness = Endianness.LittleEndian): BinaryBuffer.Writable.Data {
	return object: BinaryBuffer.Writable.Data {
		override val endianness: Endianness
			get() = endianness

		override fun writeByte(v: Byte) {
			this@asData.writeByte(v)
		}
	}
}

interface BinaryBuffer {
	interface Readable: BinaryBuffer {
		val readAvailable: Int

		fun readByte(): Byte

		fun readUByte(): UByte {
			return readByte().toUByte()
		}

		fun readBytes(array: ByteArray, offset: Int, length: Int) {
			(offset until (offset + length)).forEach { array[it] = readByte() }
		}

		fun readBytes(length: Int): ByteArray {
			val array = ByteArray(length)
			readBytes(array, 0, length)
			return array
		}

		interface Data: Readable {
			val endianness: Endianness

			fun readInt(bytes: Int): Int {
				if (readAvailable < bytes)
					throw ArrayIndexOutOfBoundsException()
				var shift = if (endianness == Endianness.BigEndian) ((bytes - 1) * 8) else 0
				val shiftIncrement = if (endianness == Endianness.BigEndian) -8 else 8

				var value = 0
				repeat(bytes) {
					value = value or ((readByte().toInt() and 0xFF) shl shift)
					shift += shiftIncrement
				}
				return value
			}

			fun readLong(bytes: Int): Long {
				if (readAvailable < bytes)
					throw ArrayIndexOutOfBoundsException()
				var shift = if (endianness == Endianness.BigEndian) ((bytes - 1) * 8) else 0
				val shiftIncrement = if (endianness == Endianness.BigEndian) -8 else 8

				var value = 0L
				repeat(bytes) {
					value = value or ((readByte().toInt() and 0xFF).toLong() shl shift)
					shift += shiftIncrement
				}
				return value
			}

			fun readShort(): Short {
				return readInt(2).toShort()
			}

			fun readUShort(): UShort {
				return readInt(2).toUShort()
			}

			fun readInt(): Int {
				return readInt(4)
			}

			fun readUInt(): UInt {
				return readInt(4).toUInt()
			}

			fun readLong(): Long {
				return readLong(8)
			}

			fun readULong(): ULong {
				return readLong(8).toULong()
			}

			fun readFloat(): Float {
				return java.lang.Float.intBitsToFloat(readInt())
			}

			fun readDouble(): Double {
				return java.lang.Double.longBitsToDouble(readLong())
			}

			fun readByteArray(): ByteArray {
				return readBytes(readInt())
			}

			fun readString(charset: Charset = Charsets.UTF_8): String {
				return String(readByteArray(), charset)
			}
		}
	}

	interface Writable: BinaryBuffer {
		fun writeByte(v: Byte)

		fun writeUByte(v: UByte) {
			writeByte(v.toByte())
		}

		fun writeBytes(array: ByteArray, offset: Int, length: Int) {
			(offset until (offset + length)).forEach { writeByte(array[it]) }
		}

		fun writeBytes(array: ByteArray, copyRequired: Boolean = true) {
			writeBytes(array, 0, array.size)
		}

		fun writeBytes(buffer: ByteBuffer, length: Int = buffer.remaining()) {
			val bytes = ByteArray(length)
			buffer.get(bytes)
			writeBytes(bytes, false)
		}

		interface Data: Writable {
			val endianness: Endianness

			fun writeInt(v: Int, bytes: Int) {
				var shift = if (endianness == Endianness.BigEndian) ((bytes - 1) * 8) else 0
				val shiftIncrement = if (endianness == Endianness.BigEndian) -8 else 8

				repeat(bytes) {
					writeByte((v ushr shift).toByte())
					shift += shiftIncrement
				}
			}

			fun writeLong(v: Long, bytes: Int) {
				var shift = if (endianness == Endianness.BigEndian) ((bytes - 1) * 8) else 0
				val shiftIncrement = if (endianness == Endianness.BigEndian) -8 else 8

				repeat(bytes) {
					writeByte((v ushr shift).toByte())
					shift += shiftIncrement
				}
			}

			fun writeShort(v: Short) {
				writeInt(v.toInt(), 2)
			}

			fun writeUShort(v: UShort) {
				writeInt(v.toInt(), 2)
			}

			fun writeInt(v: Int) {
				writeInt(v, 4)
			}

			fun writeUInt(v: UInt) {
				writeInt(v.toInt(), 4)
			}

			fun writeLong(v: Long) {
				writeLong(v, 8)
			}

			fun writeULong(v: ULong) {
				writeLong(v.toLong(), 8)
			}

			fun writeFloat(v: Float) {
				writeInt(java.lang.Float.floatToIntBits(v))
			}

			fun writeDouble(v: Double) {
				writeLong(java.lang.Double.doubleToLongBits(v))
			}

			fun writeByteArray(v: ByteArray) {
				writeInt(v.size)
				writeBytes(v)
			}

			fun writeString(v: String, charset: Charset = Charsets.UTF_8) {
				writeByteArray(v.toByteArray(charset))
			}

			private class Wrapper(
					private val wrapped: Writable,
					override val endianness: Endianness
			): Writable by wrapped, Data
		}
	}

	interface Memory: Readable, Writable {
		var readPosition: Int

		var writePosition: Int

		val size: Int

		fun clear()
	}
}

private class ReadableWrapper(
		private val wrapped: BinaryBuffer.Readable,
		override val endianness: Endianness = Endianness.LittleEndian
): BinaryBuffer.Readable by wrapped, BinaryBuffer.Readable.Data

private class WritableWrapper(
		private val wrapped: BinaryBuffer.Writable,
		override val endianness: Endianness = Endianness.LittleEndian
): BinaryBuffer.Writable by wrapped, BinaryBuffer.Writable.Data

class ReadableWritableBinaryBufferDataWrapper internal constructor(
		private val wrappedReadable: BinaryBuffer.Readable,
		private val wrappedWritable: BinaryBuffer.Writable,
		override val endianness: Endianness = Endianness.LittleEndian
): BinaryBuffer.Readable by wrappedReadable, BinaryBuffer.Writable by wrappedWritable, BinaryBuffer.Readable.Data, BinaryBuffer.Writable.Data {
	companion object {
		operator fun <B> invoke(
				wrapped: B,
				endianness: Endianness = Endianness.LittleEndian
		): ReadableWritableBinaryBufferDataWrapper where B: BinaryBuffer.Readable, B: BinaryBuffer.Writable {
			return ReadableWritableBinaryBufferDataWrapper(wrapped, wrapped, endianness)
		}
	}
}

fun BinaryBuffer.Readable.toData(endianness: Endianness = Endianness.LittleEndian): BinaryBuffer.Readable.Data {
	if (this is BinaryBuffer.Readable.Data)
		return this
	else
		return ReadableWrapper(this, endianness)
}

fun BinaryBuffer.Writable.toData(endianness: Endianness = Endianness.LittleEndian): BinaryBuffer.Writable.Data {
	if (this is BinaryBuffer.Writable.Data)
		return this
	else
		return WritableWrapper(this, endianness)
}

fun <B> B.toData(endianness: Endianness = Endianness.LittleEndian): ReadableWritableBinaryBufferDataWrapper where B: BinaryBuffer.Readable, B: BinaryBuffer.Writable {
	return ReadableWritableBinaryBufferDataWrapper(this, endianness)
}