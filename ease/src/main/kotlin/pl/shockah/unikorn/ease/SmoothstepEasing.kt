package pl.shockah.unikorn.ease

import kotlin.math.pow

class SmoothstepEasing(
		val level: Int
): Easing() {
	private fun baseEase(f: Float): Float {
		return f * f * (3f - 2f * f)
	}

	override fun ease(f: Float): Float {
		val newF = baseEase(f)
		return newF.pow(level)
	}
}