package pl.shockah.unikorn.color

import pl.shockah.unikorn.ease.ease
import pl.shockah.unikorn.math.Angle
import pl.shockah.unikorn.math.Radians
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class LCHColor(
		val l: Float,
		val c: Float,
		val h: Angle,
		val reference: XYZColor.Reference = XYZColor.Reference.D65_2
) : BaseColor<LCHColor>() {
	companion object {
		fun from(lab: LabColor): LCHColor {
			return LCHColor(
					lab.l,
					sqrt(lab.a * lab.a + lab.b * lab.b),
					Radians.of(atan2(lab.b, lab.a).toDouble()),
					lab.reference
			)
		}
	}

	data class ReferenceRanges internal constructor(
			val reference: XYZColor.Reference,
			val l: ClosedRange<Float>,
			val c: ClosedRange<Float>
	) {
		companion object {
			val D50_2: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D50_2) }
			val D50_10: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D50_10) }

			val D65_2: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D65_2) }
			val D65_10: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D65_10) }

			operator fun invoke(reference: XYZColor.Reference): ReferenceRanges {
				val steps = 16
				var minL: Float? = null
				var minC: Float? = null
				var maxL: Float? = null
				var maxC: Float? = null

				for (bi in 0 until steps) {
					for (gi in 0 until steps) {
						for (ri in 0 until steps) {
							val lch = LabColor.from(RGBColor(
									1f / (steps - 1) * ri,
									1f / (steps - 1) * gi,
									1f / (steps - 1) * bi
							).xyz, reference).lch

							if (minL == null || lch.l < minL)
								minL = lch.l
							if (minC == null || lch.c < minC)
								minC = lch.c
							if (maxL == null || lch.l > maxL)
								maxL = lch.l
							if (maxC == null || lch.c > maxC)
								maxC = lch.c
						}
					}
				}

				return ReferenceRanges(reference, minL!!..maxL!!, minC!!..maxC!!)
			}
		}
	}

	override val rgb by lazy { lab.rgb }

	val exactRgb: RGBColor by lazy { lab.exactRgb }

	val lab: LabColor by lazy { LabColor(
			l,
			h.cos.toFloat() * c,
			h.sin.toFloat() * c,
			reference
	) }

	val hsluv: HSLuvColor by lazy { HSLuvColor.from(this) }

	override fun getDistance(other: LCHColor): Float {
		val delta = h delta other.h
		return sqrt((delta.degrees.value / 180f).pow(2) + (l - other.l).pow(2) + (c - other.c).pow(2)).toFloat()
	}

	override fun ease(other: LCHColor, f: Float): LCHColor {
		return LCHColor(
				f.ease(l, other.l),
				f.ease(c, other.c),
				h.ease(other.h, f),
				reference
		)
	}
}