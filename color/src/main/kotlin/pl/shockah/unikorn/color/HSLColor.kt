package pl.shockah.unikorn.color

import pl.shockah.unikorn.ease.ease
import pl.shockah.unikorn.math.Angle
import pl.shockah.unikorn.math.Degrees
import kotlin.math.pow
import kotlin.math.sqrt

data class HSLColor(
		val h: Angle,
		val s: Float,
		val l: Float
): BaseColor<HSLColor>() {
	companion object {
		fun from(rgb: RGBColor): HSLColor {
			val max = maxOf(rgb.r, rgb.g, rgb.b)
			val min = minOf(rgb.r, rgb.g, rgb.b)
			val range = max - min

			var h = 0f
			val s: Float
			val l = (max + min) / 2f

			if (range == 0f) {
				s = 0f
			} else {
				s = if (l < 0.5f) range / (max + min) else range / (2f - max - min)

				val rr = ((max - rgb.r) / 6f + range / 2f) / range
				val gg = ((max - rgb.g) / 6f + range / 2f) / range
				val bb = ((max - rgb.b) / 6f + range / 2f) / range

				when (max) {
					rgb.r -> h = bb - gg
					rgb.g -> h = 1f / 3f + rr - bb
					rgb.b -> h = 2f / 3f + gg - rr
				}

				if (h < 0)
					h += 1f
				if (h > 1)
					h -= 1f
			}

			return HSLColor(Degrees.of(h * 360.0), s, l)
		}
	}

	override val rgb by lazy {
		if (s == 0f)
			return@lazy RGBColor(l)

		val v2 = if (l < 0.5f) l * (1 + s) else l + s - s * l
		val v1 = 2 * l - v2

		val hDegrees = h.degrees
		val r = hue2rgb(v1, v2, (hDegrees.value / 360.0 + 1.0 / 3.0).toFloat())
		val g = hue2rgb(v1, v2, (hDegrees.value / 360.0).toFloat())
		val b = hue2rgb(v1, v2, (hDegrees.value / 360.0 - 1.0 / 3.0).toFloat())
		return@lazy RGBColor(r, g, b)
	}

	private fun hue2rgb(v1: Float, v2: Float, vh: Float): Float {
		var vh2 = vh
		if (vh2 < 0)
			vh2 += 1f
		if (vh2 > 1)
			vh2 -= 1f

		return when {
			6 * vh2 < 1 -> v1 + (v2 - v1) * 6f * vh2
			2 * vh2 < 1 -> v2
			3 * vh2 < 2 -> v1 + (v2 - v1) * (2f / 3f - vh2) * 6f
			else -> v1
		}
	}

	override fun getDistance(other: HSLColor): Float {
		val delta = h delta other.h
		return sqrt((delta.degrees.value / 180.0).pow(2) + (s - other.s).pow(2) + (l - other.l).pow(2)).toFloat()
	}

	override fun ease(other: HSLColor, f: Float): HSLColor {
		return HSLColor(
				h.ease(other.h, f),
				f.ease(s, other.s),
				f.ease(l, other.l)
		)
	}
}