package pl.shockah.unikorn.ease

fun <T: Easable<T>> T.ease(other: T, easing: Easing, f: Float): T {
	return easing.ease(this, other, f)
}

fun <T: Easable<T>> Float.ease(a: T, b: T): T {
	return a.ease(b, this)
}

fun Float.ease(a: Float, b: Float): Float {
	return Easing.linear.ease(a, b, this)
}

fun Float.ease(other: Float, easing: Easing, f: Float): Float {
	return easing.ease(this, other, f)
}

fun <T> ClosedRange<T>.ease(f: Float): T where T: Easable<T>, T: Comparable<T> {
	return f.ease(start, endInclusive)
}

fun <T> ClosedRange<T>.ease(easing: Easing, f: Float): T where T: Easable<T>, T: Comparable<T> {
	return easing.ease(start, endInclusive, f)
}

fun ClosedRange<Float>.ease(f: Float): Float {
	return f.ease(start, endInclusive)
}

fun ClosedRange<Float>.ease(easing: Easing, f: Float): Float {
	return easing.ease(start, endInclusive, f)
}

fun <T> Pair<T, T>.ease(f: Float): T where T: Easable<T> {
	return f.ease(first, second)
}

fun <T: Easable<T>> Pair<T, T>.ease(easing: Easing, f: Float): T {
	return easing.ease(first, second, f)
}

fun Pair<Float, Float>.ease(f: Float): Float {
	return f.ease(first, second)
}

fun Pair<Float, Float>.ease(easing: Easing, f: Float): Float {
	return easing.ease(first, second, f)
}

interface Easable<T> {
	fun ease(other: T, f: Float): T

	class Wrapper<T>(
			val wrapped: T,
			private val function: (a: T, b: T, f: Float) -> T
	): Easable<Wrapper<T>> {
		override fun ease(other: Wrapper<T>, f: Float): Wrapper<T> {
			return Wrapper(function(wrapped, other.wrapped, f), function)
		}
	}
}