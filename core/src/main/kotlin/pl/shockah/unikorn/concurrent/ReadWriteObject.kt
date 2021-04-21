package pl.shockah.unikorn.concurrent

import pl.shockah.unikorn.Time
import pl.shockah.unikorn.tryLock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

open class ReadWriteObject<T>(
		@PublishedApi internal val wrapped: T,
		fair: Boolean = false
) {
	@PublishedApi
	internal val lock: ReadWriteLock = ReentrantReadWriteLock(fair)

	inline fun <R> read(closure: (T) -> R): R {
		try {
			lock.readLock().lock()
			return closure(wrapped)
		} finally {
			lock.readLock().unlock()
		}
	}

	inline fun <R> write(closure: (T) -> R): R {
		try {
			lock.writeLock().lock()
			return closure(wrapped)
		} finally {
			lock.writeLock().unlock()
		}
	}

	inline fun tryRead(time: Time, closure: (T) -> Unit): Boolean {
		if (lock.readLock().tryLock(time)) {
			try {
				closure(wrapped)
				return true
			} finally {
				lock.readLock().unlock()
			}
		} else {
			return false
		}
	}

	inline fun tryWrite(time: Time, closure: (T) -> Unit): Boolean {
		if (lock.readLock().tryLock(time)) {
			try {
				closure(wrapped)
				return true
			} finally {
				lock.readLock().unlock()
			}
		} else {
			return false
		}
	}

	fun asReadOnly(): ReadOnlyObject<T> {
		return ReadOnlyObject(this)
	}
}