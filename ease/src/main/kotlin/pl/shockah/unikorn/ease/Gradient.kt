package pl.shockah.unikorn.ease

class Gradient<T : Easable<T>>(
		val points: List<Pair<Float, T>>,
		val easing: Easing = Easing.linear
) {
	constructor(vararg points: Pair<Float, T>, easing: Easing = Easing.linear) : this(points.toList(), easing)

	init {
		require(points.size >= 2)

		var last: Float? = null
		for (point in points) {
			require(!(last != null && point.first < last))
			require(point.first in 0f..1f)
			last = point.first
		}
	}

	companion object {
		operator fun <T : Easable<T>> invoke(points: List<T>, easing: Easing = Easing.linear): Gradient<T> {
			return Gradient(points.mapIndexed { index, t -> 1f * index / (points.size - 1) to t }, easing)
		}

		operator fun <T : Easable<T>> invoke(vararg points: T, easing: Easing = Easing.linear): Gradient<T> {
			return Gradient(points.toList(), easing)
		}
	}

	operator fun get(f: Float): T {
		if (f <= 0f)
			return points.first().second
		if (f >= 1f)
			return points.last().second

		var lastF: Float? = null
		var lastT: T? = null
		for ((mapF, mapT) in points) {
			if (lastF != null && f >= lastF && f <= mapF)
				return ((f - lastF) / (mapF - lastF)).ease(lastT!!, mapT)
			lastF = mapF
			lastT = mapT
		}

		return points.last().second
	}
}