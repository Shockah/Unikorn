package pl.shockah.unikorn.property

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LateInitAwaitable<T>(): ReadWriteProperty<Any?, T> {
	constructor(listener: (property: LateInitAwaitable<T>, oldValue: T?, newValue: T) -> Unit) : this() {
		listeners += listener
	}

	private var value: T? = null

	private val awaitees = mutableListOf<(property: LateInitAwaitable<T>, value: T) -> Unit>()

	val listeners = mutableListOf<(property: LateInitAwaitable<T>, oldValue: T?, newValue: T) -> Unit>()

	val initialized: Boolean
		get() = value != null

	fun await(awaitee: (property: LateInitAwaitable<T>, value: T) -> Unit) {
		val value = this.value
		if (value == null)
			awaitees += awaitee
		else
			awaitee(this, value)
	}

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return value!!
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		val oldValue = this.value
		if (value === oldValue)
			return

		this.value = value
		awaitees.forEach { it(this, value) }
		awaitees.clear()
		listeners.forEach { it(this, oldValue, value) }
	}
}