package pl.shockah.unikorn.property

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyDirty<T : Any>(
		private val initializer: () -> T
): ReadOnlyProperty<Any?, T> {
	private var value: T? = null

	fun invalidate() {
		value = null
	}

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		if (value == null)
			value = initializer()
		return value!!
	}
}