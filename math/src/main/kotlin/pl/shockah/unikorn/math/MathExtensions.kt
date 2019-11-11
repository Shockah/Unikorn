package pl.shockah.unikorn.math

import kotlin.math.ceil

fun Double.inCycle(min: Double, max: Double): Double {
	val cycle = max - min
	var new = this - min

	if (new >= cycle)
		new %= cycle
	else if (new < 0)
		new += ceil(-new / cycle) * cycle

	return new + min
}

fun Float.inCycle(min: Float, max: Float): Float {
	val cycle = max - min
	var new = this - min

	if (new >= cycle)
		new %= cycle
	else if (new < 0)
		new += ceil(-new / cycle) * cycle

	return new + min
}

fun Int.inCycle(min: Int, max: Int): Int {
	val cycle = max - min
	var new = this - min

	if (new >= cycle)
		new %= cycle
	else if (new < 0)
		new += ceil(-new.toDouble() / cycle.toDouble()).toInt() * cycle

	return new + min
}