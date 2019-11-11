package pl.shockah.unikorn.collection

@Suppress("EqualsOrHashCode")
open class Array2D<T> @PublishedApi internal constructor(
		val width: Int,
		val height: Int,
		protected val values: Array<T>
) {
	companion object {
		inline operator fun <reified T> invoke(width: Int, height: Int, fill: T): Array2D<T> {
			return Array2D(width, height, Array(width * height) { fill })
		}

		inline operator fun <reified T> invoke(width: Int, height: Int, init: (x: Int, y: Int) -> T): Array2D<T> {
			return Array2D(width, height, Array(width * height) { init(it % width, it / width) })
		}
	}

	protected fun getIndex(x: Int, y: Int): Int {
		return y * width + x
	}

	operator fun get(x: Int, y: Int): T {
		return values[getIndex(x, y)]
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

class MutableArray2D<T> @PublishedApi internal constructor(
		width: Int,
		height: Int,
		values: Array<T>
): Array2D<T>(width, height, values) {
	companion object {
		inline operator fun <reified T : Any?> invoke(width: Int, height: Int): MutableArray2D<T?> {
			return MutableArray2D(width, height, Array(width * height) { null as T? })
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
}