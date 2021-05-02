package pl.shockah.unikorn.concurrent

import pl.shockah.unikorn.Time
import pl.shockah.unikorn.schedule
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

class RateLimiter(
		val maxPermits: Int,
		val time: Time,
		initialPermits: Int = maxPermits,
		paused: Boolean = false
) {
	private var permits: Int = initialPermits
	private val timePerPermit = time.convert(TimeUnit.NANOSECONDS) / maxPermits
	private var lastUpdateTime = System.nanoTime()
	private var pauseDeltaTime: Long? = if (paused) 0L else null

	val isPaused: Boolean
		get() = lock.withLock {
			return pauseDeltaTime != null
		}

	private val lock = ReentrantLock()
	private val condition = lock.newCondition()
	private val executor = Executors.newSingleThreadScheduledExecutor()

	private fun update() {
		lock.withLock {
			if (isPaused)
				return

			val currentTime = System.nanoTime()
			val delta = currentTime - lastUpdateTime

			val newPermits = (delta / timePerPermit.nanoseconds).toInt()
			permits = min(permits + newPermits, maxPermits)
			lastUpdateTime += newPermits * timePerPermit.nanoseconds
		}
	}

	fun acquire(permits: Int = 1) {
		if (permits < 1 || permits > maxPermits)
			throw IllegalArgumentException()

		lock.withLock {
			update()

			while (this.permits < permits) {
				executor.schedule(timePerPermit * (permits - this.permits)) {
					lock.withLock {
						update()
						condition.signal()
					}
				}
				condition.await()
			}
			this.permits -= permits
		}
	}

	fun acquire(permits: ClosedRange<Int>, filter: ((permits: Int) -> Boolean)? = null): Int {
		if (permits.start < 1 || permits.start > maxPermits)
			throw IllegalArgumentException()

		lock.withLock {
			update()

			if (this.permits >= permits.start) {
				var toAcquire = min(this.permits, permits.endInclusive)
				while (filter != null && !filter(toAcquire)) {
					toAcquire--
					if (toAcquire < 1)
						throw IllegalArgumentException()
				}

				this.permits -= toAcquire
				return toAcquire
			} else {
				var toAcquire = permits.start
				while (filter != null && !filter(toAcquire)) {
					toAcquire--
					if (toAcquire < 1)
						throw IllegalArgumentException()
				}

				acquire(permits.start)
				return permits.start
			}
		}
	}

	fun pause() {
		lock.withLock {
			if (isPaused)
				return

			update()
			pauseDeltaTime = System.nanoTime() - lastUpdateTime
		}
	}

	fun resume() {
		lock.withLock {
			if (!isPaused)
				return

			lastUpdateTime = System.nanoTime() - pauseDeltaTime!!
			pauseDeltaTime = null
		}
	}

	fun replenish() {
		lock.withLock {
			permits = maxPermits
			lastUpdateTime = System.nanoTime()
			condition.signal()
		}
	}
}