package pl.shockah.unikorn.collection

fun IntArray.maxIndex(): Int? {
	if (size == 0)
		return null

	var max = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] > max) {
			max = this[i]
			index = i
		}
	}
	return index
}

fun LongArray.maxIndex(): Int? {
	if (size == 0)
		return null

	var max = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] > max) {
			max = this[i]
			index = i
		}
	}
	return index
}

fun <T: Comparable<T>> Array<T>.maxIndex(): Int? {
	if (size == 0)
		return null

	var max = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] > max) {
			max = this[i]
			index = i
		}
	}
	return index
}

fun <T: Comparable<T>> List<T>.maxIndex(): Int? {
	if (size == 0)
		return null

	var max = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] > max) {
			max = this[i]
			index = i
		}
	}
	return index
}

fun IntArray.minIndex(): Int? {
	if (size == 0)
		return null

	var min = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] < min) {
			min = this[i]
			index = i
		}
	}
	return index
}

fun LongArray.minIndex(): Int? {
	if (size == 0)
		return null

	var min = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] < min) {
			min = this[i]
			index = i
		}
	}
	return index
}

fun <T: Comparable<T>> Array<T>.minIndex(): Int? {
	if (size == 0)
		return null

	var min = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] < min) {
			min = this[i]
			index = i
		}
	}
	return index
}

fun <T: Comparable<T>> List<T>.minIndex(): Int? {
	if (size == 0)
		return null

	var min = this[0]
	var index = 0
	for (i in 1 until size) {
		if (this[i] < min) {
			min = this[i]
			index = i
		}
	}
	return index
}

inline fun <T, R: Comparable<R>> Array<out T>.maxIndexBy(selector: (T) -> R): Int? {
	if (size == 0)
		return null

	var max = selector(this[0])
	var index = 0
	for (i in 1 until size) {
		val value = selector(this[i])
		if (value > max) {
			max = value
			index = i
		}
	}
	return index
}

inline fun <T, R: Comparable<R>> List<T>.maxIndexBy(selector: (T) -> R): Int? {
	if (size == 0)
		return null

	var max = selector(this[0])
	var index = 0
	for (i in 1 until size) {
		val value = selector(this[i])
		if (value > max) {
			max = value
			index = i
		}
	}
	return index
}

inline fun <T, R: Comparable<R>> Array<out T>.minIndexBy(selector: (T) -> R): Int? {
	if (size == 0)
		return null

	var min = selector(this[0])
	var index = 0
	for (i in 1 until size) {
		val value = selector(this[i])
		if (value < min) {
			min = value
			index = i
		}
	}
	return index
}

inline fun <T, R: Comparable<R>> List<T>.minIndexBy(selector: (T) -> R): Int? {
	if (size == 0)
		return null

	var min = selector(this[0])
	var index = 0
	for (i in 1 until size) {
		val value = selector(this[i])
		if (value < min) {
			min = value
			index = i
		}
	}
	return index
}

inline fun <T> Iterable<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
	return indexOfFirst(predicate).takeIf { it != -1 }
}

inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
	return indexOfFirst(predicate).takeIf { it != -1 }
}

inline fun <T> Iterable<T>.indexOfLastOrNull(predicate: (T) -> Boolean): Int? {
	return indexOfLast(predicate).takeIf { it != -1 }
}

inline fun <T> List<T>.indexOfLastOrNull(predicate: (T) -> Boolean): Int? {
	return indexOfLast(predicate).takeIf { it != -1 }
}

inline fun <T, R> Iterable<T>.mapValid(transform: (T) -> R): List<R> {
	return map {
		try {
			return@map transform(it) to null
		} catch (t: Throwable) {
			return@map null to t
		}
	}.filter { it.second == null }.map { it.first!! }
}

inline fun <T> MutableCollection<T>.removeFirst(predicate: (T) -> Boolean): MutableCollection<T> {
	val iterator = iterator()
	while (iterator.hasNext()) {
		val next = iterator.next()
		if (predicate(next)) {
			iterator.remove()
			break
		}
	}
	return this
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
	var sum: Long = 0
	for (element in this) {
		sum += selector(element)
	}
	return sum
}

fun <K, V> MutableMap<K, V>.withLazyInitializer(initializer: (key: K) -> V): MutableMap<K, V> {
	return LazyInitialValueMutableMap(this, initializer)
}