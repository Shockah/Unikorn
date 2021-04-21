package pl.shockah.unikorn.concurrent

import pl.shockah.unikorn.Time

class ReadOnlyObject<T>(
		@PublishedApi internal val wrapped: ReadWriteObject<T>
) {
	inline fun <R> read(closure: (T) -> R): R {
		return wrapped.read(closure)
	}

	inline fun tryRead(time: Time, closure: (T) -> Unit): Boolean {
		return wrapped.tryRead(time, closure)
	}
}