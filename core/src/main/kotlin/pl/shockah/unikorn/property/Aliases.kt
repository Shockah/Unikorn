package pl.shockah.unikorn.property

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

val <T> KProperty0<T>.alias: ReadOnlyProperty<Any?, T>
	get() = object: ReadOnlyProperty<Any?, T> {
		override fun getValue(thisRef: Any?, property: KProperty<*>): T {
			return get()
		}
	}

val <T> KMutableProperty0<T>.alias: ReadWriteProperty<Any?, T>
	get() = object: ReadWriteProperty<Any?, T> {
		override fun getValue(thisRef: Any?, property: KProperty<*>): T {
			return get()
		}

		override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
			set(value)
		}
	}