package pl.shockah.unikorn

typealias Tuple2<A, B> = Pair<A, B>
typealias Tuple3<A, B, C> = Triple<A, B, C>

data class Tuple4<out A, out B, out C, out D>(
		val first: A,
		val second: B,
		val third: C,
		val fourth: D
) {
	override fun toString(): String = "($first, $second, $third, $fourth)"
}

data class Tuple5<out A, out B, out C, out D, out E>(
		val first: A,
		val second: B,
		val third: C,
		val fourth: D,
		val fifth: E
) {
	override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

data class Tuple6<out A, out B, out C, out D, out E, out F>(
		val first: A,
		val second: B,
		val third: C,
		val fourth: D,
		val fifth: E,
		val sixth: F
) {
	override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}