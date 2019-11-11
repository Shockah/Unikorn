package pl.shockah.unikorn.color

import pl.shockah.unikorn.ease.Easable

typealias Color = BaseColor<*>

abstract class BaseColor<C : BaseColor<C>> : Easable<C> {
	abstract val rgb: RGBColor

	abstract fun getDistance(other: C): Float

	fun alpha(alpha: Float = 1.0f): AlphaColor {
		return AlphaColor(this, alpha)
	}
}