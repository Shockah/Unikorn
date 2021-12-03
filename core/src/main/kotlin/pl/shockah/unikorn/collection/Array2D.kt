package pl.shockah.unikorn.collection

@Suppress("EqualsOrHashCode")
open class Array2D<T> @PublishedApi internal constructor(
		val width: Int,
		val height: Int,
		protected val values: Array<T>
) {
	companion object {
		val cardinalNeighborIndexes = setOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
		val cardinalAndDiagonalNeighborIndexes = cardinalNeighborIndexes + setOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)

		inline operator fun <reified T> invoke(width: Int, height: Int, fill: T): Array2D<T> {
			return Array2D(width, height, Array(width * height) { fill })
		}

		inline operator fun <reified T> invoke(width: Int, height: Int, init: (x: Int, y: Int) -> T): Array2D<T> {
			return Array2D(width, height, Array(width * height) { init(it % width, it / width) })
		}

		fun <T, R> neighborCountPredicate(
			neighborIndexes: Set<Pair<Int, Int>>,
			outOfBoundsPasses: Boolean = false,
			predicateFunction: (neighbor: T) -> Boolean,
			mappingFunction: (element: T, count: Int) -> R
		): ((array: Array2D<T>, x: Int, y: Int, element: T) -> R) {
			return closure@ { array, x, y, element ->
				val indexes = neighborIndexes.map { it.first + x to it.second + y }.toSet()
				val count = array.count(indexes, outOfBoundsPasses, predicateFunction)
				return@closure mappingFunction(element, count)
			}
		}
	}

	protected fun getIndex(x: Int, y: Int): Int {
		return y * width + x
	}

	operator fun get(x: Int, y: Int): T {
		return values[getIndex(x, y)]
	}

	operator fun get(xRange: IntRange, yRange: IntRange): Array2D<T> {
		val width = xRange.last - xRange.first + 1
		val height = yRange.last - yRange.first + 1
		val list = ArrayList<T>(width * height)
		(0..(width * height)).forEach { list.add(this[xRange.first + it % width, yRange.first + it % height]) }
		@Suppress("UNCHECKED_CAST")
		return Array2D(width, height, list.toArray() as Array<T>)
	}

	override fun equals(other: Any?): Boolean {
		return other is Array2D<*> && other.width == width && other.height == height && other.values.contentEquals(values)
	}

	fun toList(): List<T> {
		val result = ArrayList<T>(width * height)
		for (y in 0 until height) {
			for (x in 0 until width) {
				result += this[x, y]
			}
		}
		return result
	}

	inline fun <reified R> map(mappingFunction: (element: T) -> R): Array2D<R> {
		return Array2D(width, height) { x, y -> mappingFunction(this[x, y]) }
	}

	inline fun <reified R> map(mappingFunction: (array: Array2D<T>, x: Int, y: Int, element: T) -> R): Array2D<R> {
		return Array2D(width, height) { x, y -> mappingFunction(this, x, y, this[x, y]) }
	}

	fun count(indexes: Set<Pair<Int, Int>>, outOfBoundsPasses: Boolean = false, predicate: (T) -> Boolean): Int {
		return indexes.asSequence().filter {
			val outOfBounds = it.first !in 0 until width || it.second !in 0 until height
			if (outOfBounds)
				return@filter outOfBounds == outOfBoundsPasses
			return@filter predicate(this[it.first, it.second])
		}.count()
	}

	fun toMap(): Map<Pair<Int, Int>, T> {
		val result = LinkedHashMap<Pair<Int, Int>, T>(width * height)
		for (y in 0 until height) {
			for (x in 0 until width) {
				result[Pair(x, y)] = this[x, y]
			}
		}
		return result
	}

	fun getRow(row: Int): List<T> {
		return (0 until width).map { this[it, row] }
	}

	fun getColumn(column: Int): List<T> {
		return (0 until height).map { this[column, it] }
	}
}

inline fun <reified T> Array2D<T>.takeRows(rows: Iterable<Int>): Array2D<T> {
	val targetRows = rows.toList()
	targetRows.forEach { require(it in 0 until height) }
	return Array2D(width, targetRows.size) { x, y -> this[x, targetRows[y]] }
}

inline fun <reified T> Array2D<T>.takeColumns(columns: Iterable<Int>): Array2D<T> {
	val targetColumns = columns.toList()
	targetColumns.forEach { require(it in 0 until width) }
	return Array2D(targetColumns.size, height) { x, y -> this[targetColumns[x], y] }
}

inline fun <reified T> Array2D<T>.dropRows(rows: Iterable<Int>): Array2D<T> {
	return takeRows((0 until height) - rows)
}

inline fun <reified T> Array2D<T>.dropColumns(columns: Iterable<Int>): Array2D<T> {
	return takeColumns((0 until width) - columns)
}

inline fun <reified T> Array2D<T>.takeRows(predicate: (List<T>) -> Boolean): Array2D<T> {
	val rows = (0 until height).map { it to getRow(it) }.filter { predicate(it.second) }.map { it.first }
	return takeRows(rows)
}

inline fun <reified T> Array2D<T>.takeColumns(predicate: (List<T>) -> Boolean): Array2D<T> {
	val columns = (0 until width).map { it to getColumn(it) }.filter { predicate(it.second) }.map { it.first }
	return takeColumns(columns)
}

class MutableArray2D<T> @PublishedApi internal constructor(
		width: Int,
		height: Int,
		values: Array<T>
): Array2D<T>(width, height, values) {
	companion object {
		inline operator fun <reified T: Any?> invoke(width: Int, height: Int): MutableArray2D<T?> {
			return MutableArray2D(width, height, Array(width * height) { null })
		}

		inline operator fun <reified T> invoke(width: Int, height: Int, fill: T): MutableArray2D<T> {
			return MutableArray2D(width, height, Array(width * height) { fill })
		}

		inline operator fun <reified T> invoke(width: Int, height: Int, init: (x: Int, y: Int) -> T): MutableArray2D<T> {
			return MutableArray2D(width, height, Array(width * height) { init(it % width, it / width) })
		}
	}

	operator fun set(x: Int, y: Int, value: T) {
		values[getIndex(x, y)] = value
	}

	operator fun set(xRange: IntRange, yRange: IntRange, value: T) {
		for (y in yRange) {
			for (x in xRange) {
				this[x, y] = value
			}
		}
	}
}