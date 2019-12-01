package pl.shockah.unikorn.collection

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