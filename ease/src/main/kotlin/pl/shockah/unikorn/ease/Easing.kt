package pl.shockah.unikorn.ease

abstract class Easing {
	companion object {
		val linear = object : Easing() {
			override fun ease(f: Float): Float {
				return f
			}
		}
	}

	fun ease(a: Float, b: Float, f: Float): Float {
		return a + ease(f) * (b - a)
	}

	fun <T : Easable<T>> ease(a: T, b: T, f: Float): T {
		return a.ease(b, ease(f))
	}

	abstract fun ease(f: Float): Float
}