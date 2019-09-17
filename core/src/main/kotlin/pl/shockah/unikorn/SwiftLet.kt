package pl.shockah.unikorn

inline fun <T1, T2> let(
		t1: () -> T1?,
		t2: () -> T2?,
		block: (T1, T2) -> Unit
) {
	block(
			t1() ?: return,
			t2() ?: return
	)
}

inline fun <T1, T2, T3> let(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		block: (T1, T2, T3) -> Unit
) {
	block(
			t1() ?: return,
			t2() ?: return,
			t3() ?: return
	)
}

inline fun <T1, T2, T3, T4> let(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		t4: () -> T4?,
		block: (T1, T2, T3, T4) -> Unit
) {
	block(
			t1() ?: return,
			t2() ?: return,
			t3() ?: return,
			t4() ?: return
	)
}

inline fun <T1, T2, T3, T4, T5> let(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		t4: () -> T4?,
		t5: () -> T5?,
		block: (T1, T2, T3, T4, T5) -> Unit
) {
	block(
			t1() ?: return,
			t2() ?: return,
			t3() ?: return,
			t4() ?: return,
			t5() ?: return
	)
}

inline fun <T1, T2, T3, T4, T5, T6> let(
		t1: () -> T1?,
		t2: () -> T2?,
		t3: () -> T3?,
		t4: () -> T4?,
		t5: () -> T5?,
		t6: () -> T6?,
		block: (T1, T2, T3, T4, T5, T6) -> Unit
) {
	block(
			t1() ?: return,
			t2() ?: return,
			t3() ?: return,
			t4() ?: return,
			t5() ?: return,
			t6() ?: return
	)
}