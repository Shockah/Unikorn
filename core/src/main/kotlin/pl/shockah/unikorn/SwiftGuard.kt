@file:Suppress("IMPLICIT_NOTHING_AS_TYPE_PARAMETER")

package pl.shockah.unikorn

inline fun <T> T?.guard(block: () -> Nothing): T {
	return this ?: block()
}

inline fun <T1, T2> guard(
		t1: () -> T1?,
		t2: () -> T2?,
		block: () -> Nothing
): Tuple2<T1, T2> = Tuple2(
		t1() ?: block(),
		t2() ?: block()
)

inline fun <T1, T2, T3> guard(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		block: () -> Nothing
): Tuple3<T1, T2, T3> = Tuple3(
		t1() ?: block(),
		t2() ?: block(),
		t3() ?: block()
)

inline fun <T1, T2, T3, T4> guard(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		t4: () -> T4?,
		block: () -> Nothing
): Tuple4<T1, T2, T3, T4> = Tuple4(
		t1() ?: block(),
		t2() ?: block(),
		t3() ?: block(),
		t4() ?: block()
)

inline fun <T1, T2, T3, T4, T5> guard(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		t4: () -> T4?,
		t5: () -> T5?,
		block: () -> Nothing
): Tuple5<T1, T2, T3, T4, T5> = Tuple5(
		t1() ?: block(),
		t2() ?: block(),
		t3() ?: block(),
		t4() ?: block(),
		t5() ?: block()
)

inline fun <T1, T2, T3, T4, T5, T6> guard(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		t4: () -> T4?,
		t5: () -> T5?,
		t6: () -> T6?,
		block: () -> Nothing
): Tuple6<T1, T2, T3, T4, T5, T6> = Tuple6(
		t1() ?: block(),
		t2() ?: block(),
		t3() ?: block(),
		t4() ?: block(),
		t5() ?: block(),
		t6() ?: block()
)