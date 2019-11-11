package pl.shockah.unikorn.math

import pl.shockah.unikorn.ease.ease
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

inline class Radians @Deprecated("Use Radians.Companion.of(value: Double) instead") constructor(
		val value: Double
): BaseAngle<Radians> {
	companion object {
		val zero = Radians()
		val half = of(PI)

		operator fun invoke(): Radians {
			return of(0.0)
		}

		fun of(value: Double): Radians {
			@Suppress("DEPRECATION")
			return Radians(value.inCycle(-PI, PI))
		}
	}

	override val radians: Radians
		get() = this

	override val degrees: Degrees
		get() = Degrees.of(Math.toDegrees(value))

	override val sin: Double
		get() = sin(value)

	override val cos: Double
		get() = cos(value)

	override val tan: Double
		get() = tan(value)

	override infix fun delta(angle: Angle): Radians {
		val r = angle.radians.value - value
		return of(r + (if (r > PI) -2 * PI else if (r < -PI) 2 * PI else 0.0))
	}

	override fun ease(other: Angle, f: Float): Radians {
		val delta = this delta other
		return if (delta.value > 0)
			of(f.ease(value.toFloat(), other.radians.value.toFloat()).toDouble())
		else
			of(f.ease((value + 360).toFloat(), other.radians.value.toFloat()).toDouble())
	}

	override operator fun plus(other: Angle): Radians {
		return of(value + other.radians.value)
	}

	override operator fun minus(other: Angle): Radians {
		return of(value - other.radians.value)
	}

	operator fun plus(radians: Float): Radians {
		return of(value + radians)
	}

	operator fun minus(radians: Float): Radians {
		return of(value - radians)
	}

	override fun rotated(fullRotations: Double): Radians {
		return of(value + fullRotations * PI * 2.0)
	}
}