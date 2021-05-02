package pl.shockah.unikorn.color

import pl.shockah.unikorn.ease.ease
import kotlin.math.pow
import kotlin.math.sqrt

data class RGBColor(
		val r: Float,
		val g: Float,
		val b: Float
): BaseColor<RGBColor>() {
	constructor(lightness: Float): this(lightness, lightness, lightness)

	companion object {
		val white = RGBColor(1f)
		val black = RGBColor(0f)

		val gray = RGBColor(0.5f)
		val lightGray = RGBColor(0.75f)
		val darkGray = RGBColor(0.25f)

		val red = RGBColor(1f, 0f, 0f)
		val green = RGBColor(0f, 1f, 0f)
		val blue = RGBColor(0f, 0f, 1f)

		val yellow = RGBColor(1f, 1f, 0f)
		val fuchsia = RGBColor(1f, 0f, 1f)
		val cyan = RGBColor(0f, 1f, 1f)
	}

	override val rgb = this
	val hsl: HSLColor by lazy { HSLColor.from(this) }
	val hsv: HSVColor by lazy { HSVColor.from(this) }

	override fun getDistance(other: RGBColor): Float {
		return sqrt((r - other.r).pow(2) + (g - other.g).pow(2) + (b - other.b).pow(2))
	}

	override fun ease(other: RGBColor, f: Float): RGBColor {
		return RGBColor(
				f.ease(r, other.r),
				f.ease(g, other.g),
				f.ease(b, other.b)
		)
	}

	operator fun times(rgb: RGBColor): RGBColor {
		return RGBColor(r * rgb.r, g * rgb.g, b * rgb.b)
	}
}