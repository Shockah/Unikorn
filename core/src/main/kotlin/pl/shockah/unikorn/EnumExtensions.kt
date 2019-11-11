package pl.shockah.unikorn

inline val <reified T : Enum<T>> T.nextInCycle: T
	get() = enumValues<T>()[(ordinal + 1) % enumValues<T>().size]

inline val <reified T : Enum<T>> T.previousInCycle: T
	get() = enumValues<T>()[(ordinal + enumValues<T>().size - 1) % enumValues<T>().size]

inline val <reified T : Enum<T>> T.nextValue: T?
	get() = if (enumValues<T>().last() != this) enumValues<T>()[ordinal + 1] else null

inline val <reified T : Enum<T>> T.previousValue: T?
	get() = if (ordinal != 0) enumValues<T>()[ordinal - 1] else null