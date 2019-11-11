package pl.shockah.unikorn

class Ref<T>(
		var value: T
) {
	override fun equals(other: Any?): Boolean {
		return other is Ref<*> && other.value == value
	}

	override fun hashCode(): Int {
		return value.hashCode()
	}

	override fun toString(): String {
		return "*($value)"
	}
}