package pl.shockah.unikorn.concurrent

import java.io.FilterOutputStream
import java.io.OutputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

class RateLimitedOutputStream(
		stream: OutputStream,
		val limiter: RateLimiter
) : FilterOutputStream(stream) {
	private val lock = ReentrantLock()

	override fun write(b: Int) {
		lock.withLock {
			limiter.acquire()
			super.write(b)
		}
	}

	override fun write(b: ByteArray) {
		lock.withLock {
			var offset = 0
			while (offset < b.size) {
				val maxPermits = min(limiter.maxPermits, b.size - offset)
				val permits = limiter.acquire(1..maxPermits)
				super.write(b, offset, permits)
				offset += permits
			}
		}
	}

	override fun write(b: ByteArray, off: Int, len: Int) {
		lock.withLock {
			var offset = 0
			while (offset < b.size) {
				val maxPermits = min(limiter.maxPermits, len - offset)
				val permits = limiter.acquire(1..maxPermits)
				super.write(b, offset + off, permits)
				offset += permits
			}
		}
	}
}