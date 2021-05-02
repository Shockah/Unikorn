package pl.shockah.unikorn.color

import pl.shockah.unikorn.ease.ease
import kotlin.math.pow
import kotlin.math.sqrt

data class LabColor(
		val l: Float,
		val a: Float,
		val b: Float,
		val reference: XYZColor.Reference = XYZColor.Reference.D65_2
): BaseColor<LabColor>() {
	companion object {
		fun from(xyz: XYZColor, reference: XYZColor.Reference = XYZColor.Reference.D65_2): LabColor {
			var x = xyz.x / reference.x
			var y = xyz.y / reference.y
			var z = xyz.z / reference.z

			x = if (x > 0.008856f) x.pow(1f / 3f) else 7.787f * x + 16f / 116f
			y = if (y > 0.008856f) y.pow(1f / 3f) else 7.787f * y + 16f / 116f
			z = if (z > 0.008856f) z.pow(1f / 3f) else 7.787f * z + 16f / 116f

			return LabColor(
					116 * y - 16,
					500 * (x - y),
					200 * (y - z),
					reference
			)
		}
	}

	data class ReferenceRanges internal constructor(
			val reference: XYZColor.Reference,
			val l: ClosedRange<Float>,
			val a: ClosedRange<Float>,
			val b: ClosedRange<Float>
	) {
		companion object {
			val D50_2: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D50_2) }
			val D50_10: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D50_10) }

			val D65_2: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D65_2) }
			val D65_10: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D65_10) }

			operator fun invoke(reference: XYZColor.Reference): ReferenceRanges {
				val steps = 16
				var minL: Float? = null
				var minA: Float? = null
				var minB: Float? = null
				var maxL: Float? = null
				var maxA: Float? = null
				var maxB: Float? = null

				for (bi in 0 until steps) {
					for (gi in 0 until steps) {
						for (ri in 0 until steps) {
							val lab = from(RGBColor(
									1f / (steps - 1) * ri,
									1f / (steps - 1) * gi,
									1f / (steps - 1) * bi
							).xyz, reference)

							if (minL == null || lab.l < minL)
								minL = lab.l
							if (minA == null || lab.a < minA)
								minA = lab.a
							if (minB == null || lab.b < minB)
								minB = lab.b
							if (maxL == null || lab.l > maxL)
								maxL = lab.l
							if (maxA == null || lab.a > maxA)
								maxA = lab.a
							if (maxB == null || lab.b > maxB)
								maxB = lab.b
						}
					}
				}

				return ReferenceRanges(reference, minL!!..maxL!!, minA!!..maxA!!, minB!!..maxB!!)
			}
		}
	}

	override val rgb by lazy { xyz.rgb }
	val exactRgb: RGBColor by lazy { xyz.exactRgb }
	val lch: LCHColor by lazy { LCHColor.from(this) }

	val xyz: XYZColor by lazy {
		var y = (l + 16) / 116f
		var x = a / 500f + y
		var z = y - b / 200f

		x = if (x > 0.008856f) x.pow(3) else (x - 16f / 116f) / 7.787f
		y = if (y > 0.008856f) y.pow(3) else (y - 16f / 116f) / 7.787f
		z = if (z > 0.008856f) z.pow(3) else (z - 16f / 116f) / 7.787f

		return@lazy XYZColor(
				x * reference.x,
				y * reference.y,
				z * reference.z
		)
	}

	override fun getDistance(other: LabColor): Float {
		return sqrt((l - other.l).pow(2) * 0.01f + (a - other.a).pow(2) * 0.005f + (b - other.b).pow(2) * 0.005f)
	}

	override fun ease(other: LabColor, f: Float): LabColor {
		return LabColor(
				f.ease(l, other.l),
				f.ease(a, other.a),
				f.ease(b, other.b),
				reference
		)
	}

	fun with(l: Float = this.l, a: Float = this.a, b: Float = this.b): LabColor {
		return LabColor(l, a, b)
	}
}