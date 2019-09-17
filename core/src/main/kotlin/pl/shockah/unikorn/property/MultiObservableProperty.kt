package pl.shockah.unikorn.property

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MultiObservableProperty<T>(
		defaultValue: T
) : ReadWriteProperty<Any?, T> {
	private var value: T = defaultValue

	private val observers = mutableListOf<Observer<T>>()

	private val pendingRemovals = mutableListOf<Observer<T>>()

	private var isCallingObservers = false

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return value
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		synchronized(observers) {
			if (this.value == value)
				return
			val oldValue = this.value
			this.value = value
			isCallingObservers = true
			observers.forEach { it.onValueChanged(property, oldValue, value) }
			isCallingObservers = false
			observers.removeAll(pendingRemovals)
			pendingRemovals.clear()
		}
	}

	fun addObserver(closure: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit): Observer<T> {
		val observer = object : Observer<T> {
			override fun onValueChanged(property: KProperty<*>, oldValue: T, newValue: T) {
				closure(property, oldValue, newValue)
			}
		}
		addObserver(observer)
		return observer
	}

	fun addObserver(observer: Observer<T>) {
		synchronized(observers) {
			if (!observers.contains(observer)) {
				observers += observer
			}
		}
	}

	fun removeObserver(observer: Observer<T>) {
		synchronized(observers) {
			if (isCallingObservers)
				pendingRemovals.add(observer)
			else
				observers -= observer
		}
	}

	interface Observer<T> {
		fun onValueChanged(property: KProperty<*>, oldValue: T, newValue: T)
	}
}