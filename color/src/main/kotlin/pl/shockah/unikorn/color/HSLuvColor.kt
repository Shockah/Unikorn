package pl.shockah.unikorn.color

import pl.shockah.unikorn.ease.ease
import pl.shockah.unikorn.math.Angle
import kotlin.math.pow
import kotlin.math.sqrt

data class HSLuvColor(
		val h: Angle,
		val s: Float,
		val luv: Float,
		val reference: XYZColor.Reference = XYZColor.Reference.D65_2
): BaseColor<HSLuvColor>() {
	companion object {
		private const val kappa = 903.2962962f
		private const val epsilon = 0.0088564516f

		private val m = arrayOf(
				floatArrayOf(3.240969941904521f, -1.537383177570093f, -0.498610760293f),
				floatArrayOf(-0.96924363628087f, 1.87596750150772f, 0.041555057407175f),
				floatArrayOf(0.055630079696993f, -0.20397695888897f, 1.056971514242878f)
		)

		fun from(lch: LCHColor): HSLuvColor {
			if (lch.l > 99.9999999f)
				return HSLuvColor(lch.h, 0f, 1f, lch.reference)
			if (lch.l < 0.00000001f)
				return HSLuvColor(lch.h, 0f, 0f, lch.reference)

			val max = maxChromaForLH(lch.l, lch.h)
			val s = lch.c / max
			return HSLuvColor(lch.h, s, lch.l * 0.01f, lch.reference)
		}

		private fun maxChromaForLH(L: Float, H: Angle): Float {
			val bounds = getBounds(L)
			var min = Float.MAX_VALUE

			for (bound in bounds) {
				val length = lengthOfRayUntilIntersect(H, bound)
				if (length >= 0f)
					min = min.coerceAtMost(length)
			}

			return min
		}

		private fun lengthOfRayUntilIntersect(theta: Angle, line: FloatArray): Float {
			return line[1] / (theta.sin - line[0] * theta.cos).toFloat()
		}

		private fun getBounds(L: Float): List<FloatArray> {
			val result: MutableList<FloatArray> = mutableListOf()

			val sub1 = (L + 16).pow(3) / 1560896
			val sub2 = if (sub1 > epsilon) sub1 else L / kappa

			for (c in 0..2) {
				val m1 = m[c][0]
				val m2 = m[c][1]
				val m3 = m[c][2]

				for (t in 0..1) {
					val top1 = (284517 * m1 - 94839 * m3) * sub2
					val top2 = (838422 * m3 + 769860 * m2 + 731718 * m1) * L * sub2 - 769860f * t.toFloat() * L
					val bottom = (632260 * m3 - 126452 * m2) * sub2 + 126452 * t
					result.add(floatArrayOf(top1 / bottom, top2 / bottom))
				}
			}

			return result
		}
	}

	data class ReferenceRanges internal constructor(
			val reference: XYZColor.Reference,
			val luv: ClosedRange<Float>
	) {
		companion object {
			val D50_2: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D50_2) }
			val D50_10: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D50_10) }

			val D65_2: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D65_2) }
			val D65_10: ReferenceRanges by lazy { ReferenceRanges(XYZColor.Reference.D65_10) }

			operator fun invoke(reference: XYZColor.Reference): ReferenceRanges {
				val steps = 16
				var minLuv: Float? = null
				var maxLuv: Float? = null

				for (bi in 0 until steps) {
					for (gi in 0 until steps) {
						for (ri in 0 until steps) {
							val hsluv = LabColor.from(RGBColor(
									1f / (steps - 1) * ri,
									1f / (steps - 1) * gi,
									1f / (steps - 1) * bi
							).xyz, reference).lch.hsluv

							if (minLuv == null || hsluv.luv < minLuv)
								minLuv = hsluv.luv
							if (maxLuv == null || hsluv.luv > maxLuv)
								maxLuv = hsluv.luv
						}
					}
				}

				return ReferenceRanges(reference, minLuv!!..maxLuv!!)
			}
		}
	}

	override val rgb by lazy { lch.rgb }

	val exactRgb: RGBColor by lazy { lch.exactRgb }

	val lch: LCHColor by lazy {
		if (luv > 0.999999999f)
			return@lazy LCHColor(1f, 0f, h)
		if (luv < 0.0000000001f)
			return@lazy LCHColor(0f, 0f, h)

		val max = maxChromaForLH(luv * 100f, h)
		val c = max * s
		return@lazy LCHColor(luv * 100f, c, h, reference)
	}

	override fun getDistance(other: HSLuvColor): Float {
		val delta = h delta other.h
		return sqrt((delta.degrees.value / 180.0).pow(2) + (s - other.s).pow(2) + (luv - other.luv).pow(2)).toFloat()
	}

	override fun ease(other: HSLuvColor, f: Float): HSLuvColor {
		return HSLuvColor(
				h.ease(other.h, f),
				f.ease(s, other.s),
				f.ease(luv, other.luv),
				reference
		)
	}
}