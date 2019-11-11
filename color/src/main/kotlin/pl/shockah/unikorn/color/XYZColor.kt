package pl.shockah.unikorn.color

import pl.shockah.unikorn.ease.ease
import kotlin.math.pow
import kotlin.math.sqrt

val RGBColor.xyz: XYZColor
	get() = XYZColor.from(this)

data class XYZColor(
		val x: Float,
		val y: Float,
		val z: Float
): BaseColor<XYZColor>() {
	data class Reference(
			val x: Float,
			val y: Float,
			val z: Float
	) {
		companion object {
			val D50_2: Reference = Reference(96.422f, 100f, 82.521f)
			val D50_10: Reference = Reference(96.720f, 100f, 81.427f)

			val D65_2: Reference = Reference(95.047f, 100f, 108.883f)
			val D65_10: Reference = Reference(94.811f, 100f, 107.304f)
		}
	}

	companion object {
		private var backingXRange: ClosedRange<Float>? = null
		private var backingYRange: ClosedRange<Float>? = null
		private var backingZRange: ClosedRange<Float>? = null

		val xRange: ClosedRange<Float>
			get() {
				if (backingXRange == null)
					calculateRanges()
				return backingXRange!!
			}

		val yRange: ClosedRange<Float>
			get() {
				if (backingYRange == null)
					calculateRanges()
				return backingYRange!!
			}

		val zRange: ClosedRange<Float>
			get() {
				if (backingZRange == null)
					calculateRanges()
				return backingZRange!!
			}

		private fun calculateRanges() {
			val steps = 16
			var minX: Float? = null
			var minY: Float? = null
			var minZ: Float? = null
			var maxX: Float? = null
			var maxY: Float? = null
			var maxZ: Float? = null

			for (bi in 0 until steps) {
				for (gi in 0 until steps) {
					for (ri in 0 until steps) {
						val xyz = RGBColor(
								1f / (steps - 1) * ri,
								1f / (steps - 1) * gi,
								1f / (steps - 1) * bi
						).xyz

						if (minX == null || xyz.x < minX)
							minX = xyz.x
						if (minY == null || xyz.y < minY)
							minY = xyz.y
						if (minZ == null || xyz.z < minZ)
							minZ = xyz.z
						if (maxX == null || xyz.x > maxX)
							maxX = xyz.x
						if (maxY == null || xyz.y > maxY)
							maxY = xyz.y
						if (maxZ == null || xyz.z > maxZ)
							maxZ = xyz.z
					}
				}
			}

			backingXRange = minX!!..maxX!!
			backingYRange = minY!!..maxY!!
			backingZRange = minZ!!..maxZ!!
		}

		fun from(rgb: RGBColor): XYZColor {
			var r = if (rgb.r > 0.04045f) ((rgb.r + 0.055f) / 1.055f).pow(2.4f) else rgb.r / 12.92f
			var g = if (rgb.g > 0.04045f) ((rgb.g + 0.055f) / 1.055f).pow(2.4f) else rgb.g / 12.92f
			var b = if (rgb.b > 0.04045f) ((rgb.b + 0.055f) / 1.055f).pow(2.4f) else rgb.b / 12.92f

			r *= 100
			g *= 100
			b *= 100

			return XYZColor(
					r * 0.4124f + g * 0.3576f + b * 0.1805f,
					r * 0.2126f + g * 0.7152f + b * 0.0722f,
					r * 0.0193f + g * 0.1192f + b * 0.9505f
			)
		}
	}

	override val rgb by lazy { internalRGB(CheckingMode.Clamp) }

	val exactRgb: RGBColor by lazy { internalRGB(CheckingMode.Throw) }

	enum class CheckingMode {
		None, Clamp, Throw
	}

	private fun internalRGB(checkingMode: CheckingMode): RGBColor {
		val x = this.x / 100
		val y = this.y / 100
		val z = this.z / 100

		var r = x * 3.2406f - y * 1.5372f - z * 0.4986f
		var g = -x * 0.9689f + y * 1.8758f + z * 0.0415f
		var b = x * 0.0557f - y * 0.2040f + z * 1.0570f

		r = if (r > 0.0031308f) 1.055f * r.pow(1f / 2.4f) - 0.055f else r * 12.92f
		g = if (g > 0.0031308f) 1.055f * g.pow(1f / 2.4f) - 0.055f else g * 12.92f
		b = if (b > 0.0031308f) 1.055f * b.pow(1f / 2.4f) - 0.055f else b * 12.92f

		when (checkingMode) {
			CheckingMode.None -> { }
			CheckingMode.Clamp -> {
				r = r.coerceIn(0f, 1f)
				g = g.coerceIn(0f, 1f)
				b = b.coerceIn(0f, 1f)
			}
			CheckingMode.Throw -> {
				require(!(r < 0f || r > 1f)) { "Cannot convert to RGB - R outside the 0-1 bounds." }
				require(!(g < 0f || g > 1f)) { "Cannot convert to RGB - G outside the 0-1 bounds." }
				require(!(b < 0f || b > 1f)) { "Cannot convert to RGB - B outside the 0-1 bounds." }
			}
		}

		return RGBColor(r, g, b)
	}

	override fun getDistance(other: XYZColor): Float {
		return sqrt((x - other.x).pow(2) * 0.01f + (y - other.y).pow(2) * 0.01f + (z - other.z).pow(2) * 0.01f)
	}

	override fun ease(other: XYZColor, f: Float): XYZColor {
		return XYZColor(
				f.ease(x, other.x),
				f.ease(y, other.y),
				f.ease(z, other.z)
		)
	}
}