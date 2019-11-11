package pl.shockah.unikorn.math

import pl.shockah.unikorn.ease.ease

inline class Degrees @Deprecated("Use Degrees.Companion.of(value: Double) instead") constructor(
		val value: Double
) : BaseAngle<Degrees> {
	companion object {
		val zero = Degrees()
		val half = of(180.0)

		operator fun invoke(): Degrees {
			return of(0.0)
		}

		fun of(value: Double): Degrees {
			@Suppress("DEPRECATION")
			return Degrees(value.inCycle(-180.0, 180.0))
		}
	}

	override val degrees: Degrees
		get() = this

	override val radians: Radians
		get() = Radians.of(Math.toRadians(value))

	override val sin: Double
		get() = radians.sin

	override val cos: Double
		get() = radians.cos

	override val tan: Double
		get() = radians.tan

	override infix fun delta(angle: Angle): Degrees {
		val r = angle.degrees.value - value
		return of(r + if (r > 180) -360 else if (r < -180) 360 else 0)
	}

	override fun ease(other: Angle, f: Float): Degrees {
		val delta = this delta other
		return if (delta.value > 0)
			of(f.ease(value.toFloat(), other.degrees.value.toFloat()).toDouble())
		else
			of(f.ease((value + 360).toFloat(), other.degrees.value.toFloat()).toDouble())
	}

	override operator fun plus(other: Angle): Degrees {
		return of(value + other.degrees.value)
	}

	override operator fun minus(other: Angle): Degrees {
		return of(value - other.degrees.value)
	}

	operator fun plus(degrees: Float): Degrees {
		return of(value + degrees)
	}

	operator fun minus(degrees: Float): Degrees {
		return of(value - degrees)
	}

	override fun rotated(fullRotations: Double): Degrees {
		return of(value + fullRotations * 360.0)
	}
}