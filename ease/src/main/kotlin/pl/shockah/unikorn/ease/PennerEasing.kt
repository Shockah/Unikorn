package pl.shockah.unikorn.ease

import kotlin.math.*

abstract class PennerEasing: Easing() {
	companion object {
		val quadIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return f * f
			}
		}
		val quadOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return -(f * (f - 2))
			}
		}
		val quadInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f)
					2 * f * f
				else
					-2 * f * f + (4 * f) - 1
			}
		}

		val cubicIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return f * f * f
			}
		}
		val cubicOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return cubicIn.ease(f - 1) + 1
			}
		}
		val cubicInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f) {
					4 * f * f * f
				} else {
					val f2 = 2 * f - 2
					0.5f * f2 * f2 * f2 + 1
				}
			}
		}

		val quarticIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return f * f * f * f
			}
		}
		val quarticOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				val f2 = f - 1
				return f2 * f2 * f2 * (1 - f) + 1
			}
		}
		val quarticInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f) {
					8 * f * f * f * f
				} else {
					val f2 = f - 1
					-8 * f2 * f2 * f2 * f2 + 1
				}
			}
		}

		val quinticIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return f * f * f * f * f
			}
		}
		val quinticOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return quinticIn.ease(f - 1) + 1
			}
		}
		val quinticInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f) {
					16 * f * f * f * f * f
				} else {
					val f2 = 2 * f - 2
					0.5f * f2 * f2 * f2 * f2 * f2 + 1
				}
			}
		}

		val sineIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return sin((f - 1) * PI * 0.5f).toFloat() + 1
			}
		}
		val sineOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return sin(f * PI * 0.5f).toFloat()
			}
		}
		val sineInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return 0.5f * (1 - cos(f * PI).toFloat())
			}
		}

		val circularIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return 1 - sqrt(1 - f * f)
			}
		}
		val circularOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return sqrt((2 - f) * f)
			}
		}
		val circularInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f)
					0.5f * (1 - sqrt(1 - 4 * (f * f)))
				else
					0.5f * (sqrt(-(2 * f - 3) * (2 * f - 1)) + 1)
			}
		}

		val exponentialIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f == 0f) f else 2.0.pow(10.0 * (f - 1)).toFloat()
			}
		}
		val exponentialOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f == 1f) f else 1 - 2.0.pow(-10.0 * f).toFloat()
			}
		}
		val exponentialInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				if (f == 0f || f == 1f)
					return f

				return if (f < 0.5f)
					0.5f * 2.0.pow(20.0 * f - 10).toFloat()
				else
					-0.5f * 2.0.pow(-20.0 * f + 10).toFloat() + 1
			}
		}

		val elasticIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return sin(13 * PI * 0.5f * f).toFloat() * 2.0.pow(10.0 * (f - 1)).toFloat()
			}
		}
		val elasticOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return sin(-13 * PI * 0.5f * (f + 1)).toFloat() * 2.0.pow(-10.0 * f).toFloat() + 1
			}
		}
		val elasticInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f)
					0.5f * sin(13 * PI * 0.5f * (2 * f)).toFloat() * 2.0.pow(10.0 * (2 * f - 1)).toFloat()
				else
					0.5f * (sin(-13 * PI * 0.5f * (2 * f - 1 + 1)).toFloat() * 2.0.pow(-10.0 * (2 * f - 1)).toFloat() + 2)
			}
		}

		val backIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return f * f * f - f * sin(f * PI).toFloat()
			}
		}
		val backOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return 1f - backIn.ease(1 - f)
			}
		}
		val backInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f) {
					val f2 = 2 * f
					0.5f * (f2 * f2 * f2 - f2 * sin(f2 * PI).toFloat())
				} else {
					val f2 = 1 - (2 * f - 1)
					0.5f * (1 - (f2 * f2 * f2 - f2 * sin(f2 * PI).toFloat())) + 0.5f
				}
			}
		}

		val bounceIn = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return 1f - bounceOut.ease(1 - f)
			}
		}
		val bounceOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return when {
					f < 4f / 11f -> (121 * f * f) / 16f
					f < 8f / 11f -> (363 / 40f * f * f) - (99 / 10f * f) + 17 / 5f
					f < 9f / 10f -> (4356 / 361f * f * f) - (35442 / 1805f * f) + 16061 / 1805f
					else -> (54 / 5f * f * f) - (513 / 25f * f) + 268 / 25f
				}
			}
		}
		val bounceInOut = object : PennerEasing() {
			override fun ease(f: Float): Float {
				return if (f < 0.5f)
					0.5f * bounceIn.ease(f * 2)
				else
					0.5f * bounceOut.ease(f * 2 - 1) + 0.5f
			}
		}
	}
}