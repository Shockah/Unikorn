package pl.shockah.unikorn.binbuffer

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PipeBinaryBuffer private constructor(
		underlyingBuffer: BinaryBuffer
) : BinaryBuffer.Readable, BinaryBuffer.Writable, Closeable {
	private val readable = underlyingBuffer as BinaryBuffer.Readable

	private val writable = underlyingBuffer as BinaryBuffer.Writable

	private val lock = ReentrantLock()

	private val dataAvailableCondition = lock.newCondition()

	private var closed = false

	override val readAvailable: Int
		get() = lock.withLock { return readable.readAvailable }

	val inputStream by lazy { object : InputStream() {
		override fun read(): Int {
			lock.withLock {
				while (readAvailable == 0) {
					if (closed)
						return -1
					dataAvailableCondition.await()
				}

				return readByte().toInt() and 0xFF
			}
		}

		override fun close() {
			super.close()
			this@PipeBinaryBuffer.close()
		}
	} }

	val outputStream by lazy { object : OutputStream() {
		override fun write(b: Int) {
			writeByte(b.toByte())
		}

		override fun write(b: ByteArray, off: Int, len: Int) {
			writeBytes(b, off, len)
		}

		override fun write(b: ByteArray) {
			writeBytes(b)
		}

		override fun close() {
			super.close()
			this@PipeBinaryBuffer.close()
		}
	} }

	companion object {
		operator fun <T> invoke(underlyingBuffer: T): PipeBinaryBuffer where T : BinaryBuffer.Readable, T : BinaryBuffer.Writable {
			return PipeBinaryBuffer(underlyingBuffer)
		}
	}

	override fun close() {
		lock.withLock {
			closed = true
			dataAvailableCondition.signal()
		}
	}

	override fun readByte(): Byte {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			return readable.readByte()
		}
	}

	override fun readUByte(): UByte {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			return readable.readUByte()
		}
	}

	override fun readBytes(length: Int): ByteArray {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			return readable.readBytes(length)
		}
	}

	override fun readBytes(array: ByteArray, offset: Int, length: Int) {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			return readable.readBytes(array, offset, length)
		}
	}

	override fun writeByte(v: Byte) {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			writable.writeByte(v)
			dataAvailableCondition.signal()
		}
	}

	override fun writeUByte(v: UByte) {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			writable.writeUByte(v)
			dataAvailableCondition.signal()
		}
	}

	override fun writeBytes(array: ByteArray, offset: Int, length: Int) {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			writable.writeBytes(array, offset, length)
			dataAvailableCondition.signal()
		}
	}

	override fun writeBytes(array: ByteArray, copyRequired: Boolean) {
		lock.withLock {
			if (closed)
				throw IOException("Pipe closed")
			writable.writeBytes(array, copyRequired)
			dataAvailableCondition.signal()
		}
	}
}