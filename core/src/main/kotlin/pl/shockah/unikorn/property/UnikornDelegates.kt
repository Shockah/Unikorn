package pl.shockah.unikorn.property

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object UnikornDelegates {
	fun <T> observable(initialValue: T, onChange: () -> Unit): ReadWriteProperty<Any?, T> {
		return observable(initialValue) { _, _, _ -> onChange() }
	}

	fun <T> observable(initialValue: T, onChange: (oldValue: T, newValue: T) -> Unit): ReadWriteProperty<Any?, T> {
		return observable(initialValue) { _, oldValue, newValue -> onChange(oldValue, newValue) }
	}

	fun <T> observable(initialValue: T, onChange: (newValue: T) -> Unit): ReadWriteProperty<Any?, T> {
		return observable(initialValue) { _, _, newValue -> onChange(newValue) }
	}

	fun <T> observable(initialValue: T, onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit): ReadWriteProperty<Any?, T> {
		return object: ObservableProperty<T>(initialValue) {
			override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
				if (newValue != oldValue)
					onChange(property, oldValue, newValue)
			}
		}
	}
}