package pl.shockah.unikorn.math

open class MutableVector2(
		override var x: Double,
		override var y: Double
): BaseVector2<MutableVector2>() {
	constructor() : this(0.0, 0.0)

	companion object {
		operator fun invoke(angle: Angle, length: Double): MutableVector2 {
			val radians = angle.radians
			return MutableVector2(radians.cos * length, radians.sin * length)
		}
	}

	override var length: Double
		get() = super.length
		set(value) {
			val angle = radians
			x = angle.cos * value
			y = angle.sin * value
		}

	override var degrees: Degrees
		get() = super.degrees
		set(value) {
			val length = length
			val angle = value.radians
			x = angle.cos * length
			y = angle.sin * length
		}

	override var radians: Radians
		get() = super.radians
		set(value) {
			val length = length
			x = value.cos * length
			y = value.sin * length
		}

	val xy: Mutator by lazy { Mutator() }

	operator fun set(index: Int, value: Double) {
		when (index) {
			0 -> x = value
			1 -> y = value
			else -> throw IllegalArgumentException()
		}
	}

	override operator fun unaryMinus(): MutableVector2 {
		return MutableVector2(-x, -y)
	}

	override operator fun plus(vector: Vector2): MutableVector2 {
		return MutableVector2(x + vector.x, y + vector.y)
	}

	override operator fun minus(vector: Vector2): MutableVector2 {
		return MutableVector2(x - vector.x, y - vector.y)
	}

	override operator fun times(vector: Vector2): MutableVector2 {
		return MutableVector2(x * vector.x, y * vector.y)
	}

	override operator fun div(vector: Vector2): MutableVector2 {
		return MutableVector2(x / vector.x, y / vector.y)
	}

	override operator fun plus(scalar: Double): MutableVector2 {
		return normalized * (length + scalar)
	}

	override operator fun minus(scalar: Double): MutableVector2 {
		return normalized * (length - scalar)
	}

	override operator fun times(scalar: Double): MutableVector2 {
		return MutableVector2(x * scalar, y * scalar)
	}

	override operator fun div(scalar: Double): MutableVector2 {
		return MutableVector2(x / scalar, y / scalar)
	}

	fun set(vector: Vector2) {
		x = vector.x
		y = vector.y
	}

	override fun rotated(angle: Angle): MutableVector2 {
		return MutableVector2(radians + angle.radians, length)
	}

	inner class Mutator {
		operator fun plusAssign(vector: Vector2) {
			x += vector.x
			y += vector.y
		}

		operator fun minusAssign(vector: Vector2) {
			x -= vector.x
			y -= vector.y
		}

		operator fun timesAssign(vector: Vector2) {
			x *= vector.x
			y *= vector.y
		}

		operator fun divAssign(vector: Vector2) {
			x /= vector.x
			y /= vector.y
		}

		operator fun plusAssign(scalar: Double) {
			val result = normalized * (length + scalar)
			x = result.x
			y = result.y
		}

		operator fun minusAssign(scalar: Double) {
			val result = normalized * (length - scalar)
			x = result.x
			y = result.y
		}

		operator fun timesAssign(scalar: Double) {
			x *= scalar
			y *= scalar
		}

		operator fun divAssign(scalar: Double) {
			x /= scalar
			y /= scalar
		}
	}
}