package pl.shockah.unikorn.math

import pl.shockah.unikorn.ease.Easable
import pl.shockah.unikorn.ease.ease
import kotlin.math.atan2
import kotlin.math.sqrt

typealias Vector2 = BaseVector2<*>

abstract class BaseVector2<T: BaseVector2<T>>: Comparable<BaseVector2<*>>, Easable<BaseVector2<*>> {
	abstract val x: Double
	abstract val y: Double

	open val lengthSquared: Double
		get() = x * x + y * y

	open val length: Double
		get() = sqrt(lengthSquared)

	open val degrees: Degrees
		get() = ImmutableVector2.zero degrees this

	open val radians: Radians
		get() = ImmutableVector2.zero radians this

	open val normalized: T
		get() {
			val length = length
			@Suppress("UNCHECKED_CAST")
			return when (length) {
				0.0 -> throw IllegalStateException()
				1.0 -> this as T
				else -> this / length
			}
		}

	operator fun get(index: Int): Double {
		return when (index) {
			0 -> x
			1 -> y
			else -> throw IllegalArgumentException()
		}
	}

	operator fun component1(): Double {
		return x
	}

	operator fun component2(): Double {
		return y
	}

	open fun immutable(): ImmutableVector2 {
		return ImmutableVector2(x, y)
	}

	fun mutableCopy(): MutableVector2 {
		return MutableVector2(x, y)
	}

	abstract operator fun unaryMinus(): T

	abstract operator fun plus(vector: Vector2): T

	abstract operator fun minus(vector: Vector2): T

	abstract operator fun times(vector: Vector2): T

	abstract operator fun div(vector: Vector2): T

	abstract operator fun plus(scalar: Double): T

	abstract operator fun minus(scalar: Double): T

	abstract operator fun times(scalar: Double): T

	abstract operator fun div(scalar: Double): T

	infix fun degrees(vector: Vector2): Degrees {
		return (this radians vector).degrees
	}

	infix fun radians(vector: Vector2): Radians {
		return Radians.of(atan2(y - vector.y, vector.x - x))
	}

	infix fun dot(vector: Vector2): Double {
		return x * vector.x + y * vector.y
	}

	infix fun cross(vector: Vector2): Double {
		return x * vector.y - y * vector.x
	}

	abstract infix fun rotated(angle: Angle): T

	infix fun equals(other: Vector2): Boolean {
		return other.x == x && other.y == y
	}

	infix fun notEquals(other: Vector2): Boolean {
		return other.x != x || other.y != y
	}

	override fun equals(other: Any?): Boolean {
		return other is Vector2 && other.x == x && other.y == y
	}

	override fun hashCode(): Int {
		return x.hashCode() * 31 + y.hashCode()
	}

	override fun toString(): String = "[$x, $y]"

	override fun compareTo(other: BaseVector2<*>): Int {
		return length.compareTo(other.length)
	}

	override fun ease(other: BaseVector2<*>, f: Float): BaseVector2<*> {
		return ImmutableVector2(
				f.ease(x.toFloat(), other.x.toFloat()).toDouble(),
				f.ease(y.toFloat(), other.y.toFloat()).toDouble()
		)
	}
}