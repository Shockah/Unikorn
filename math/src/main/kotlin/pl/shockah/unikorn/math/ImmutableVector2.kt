package pl.shockah.unikorn.math

import kotlin.math.sqrt

class ImmutableVector2(
		override val x: Double,
		override val y: Double
): BaseVector2<ImmutableVector2>() {
	override val lengthSquared by lazy { super.lengthSquared }
	override val length by lazy { sqrt(lengthSquared) }
	override val degrees by lazy { super.degrees }
	override val radians by lazy { super.radians }
	override val normalized by lazy { super.normalized }

	constructor(): this(0.0, 0.0)

	companion object {
		val zero = ImmutableVector2()

		operator fun invoke(angle: Angle, length: Double): ImmutableVector2 {
			val radians = angle.radians
			return ImmutableVector2(radians.cos * length, radians.sin * length)
		}
	}

	override fun immutable(): ImmutableVector2 {
		return this
	}

	override operator fun unaryMinus(): ImmutableVector2 {
		return ImmutableVector2(-x, -y)
	}

	override operator fun plus(vector: Vector2): ImmutableVector2 {
		return ImmutableVector2(x + vector.x, y + vector.y)
	}

	override operator fun minus(vector: Vector2): ImmutableVector2 {
		return ImmutableVector2(x - vector.x, y - vector.y)
	}

	override operator fun times(vector: Vector2): ImmutableVector2 {
		return ImmutableVector2(x * vector.x, y * vector.y)
	}

	override operator fun div(vector: Vector2): ImmutableVector2 {
		return ImmutableVector2(x / vector.x, y / vector.y)
	}

	override operator fun plus(scalar: Double): ImmutableVector2 {
		return normalized * (length + scalar)
	}

	override operator fun minus(scalar: Double): ImmutableVector2 {
		return normalized * (length - scalar)
	}

	override operator fun times(scalar: Double): ImmutableVector2 {
		return ImmutableVector2(x * scalar, y * scalar)
	}

	override operator fun div(scalar: Double): ImmutableVector2 {
		return ImmutableVector2(x / scalar, y / scalar)
	}

	override fun rotated(angle: Angle): ImmutableVector2 {
		return ImmutableVector2(radians + angle.radians, length)
	}
}