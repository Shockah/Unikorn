package pl.shockah.unikorn.cassowary

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pl.shockah.unikorn.property.UnikornDelegates.observable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ViewVariable(
		val owner: View,
		val name: String
): Variable, ReadWriteProperty<View, Double> {
	override var value = 0.0

	override fun toString(): String {
		return "${owner}.${name}"
	}

	override fun getValue(thisRef: View, property: KProperty<*>): Double {
		return value
	}

	override fun setValue(thisRef: View, property: KProperty<*>, value: Double) {
		this.value = value
	}
}

class ViewVariableProvider {
	operator fun provideDelegate(thisRef: View, property: KProperty<*>): ReadOnlyProperty<View, ViewVariable> {
		val variable = ViewVariable(thisRef, property.name)
		return object : ReadOnlyProperty<View, ViewVariable> {
			override fun getValue(thisRef: View, property: KProperty<*>): ViewVariable {
				return variable
			}
		}
	}
}

class View {
	val left by variable
	val top by variable
	val right by variable
	val bottom by variable
	val width by variable
	val height by variable
	val centerX by variable
	val centerY by variable

	val constraints = mutableListOf<Constraint>()

	private val baseEditVariables = listOf(left, right, top, bottom)

	private val baseConstraints = listOf(
			width equals right - left,
			height equals bottom - top,
			centerX equals left + width * 0.5,
			centerY equals top + height * 0.5,
			left lessOrEqual right,
			top lessOrEqual bottom
	)

	private val allConstraints: List<Constraint>
		get() = constraints + baseConstraints

	var solver: Solver? by observable(null) { old: Solver?, new: Solver? ->
		old?.let { solver ->
			baseEditVariables.forEach { solver.removeEditVariable(it) }
			allConstraints.forEach { solver -= it }
		}
		new?.let { solver ->
			baseEditVariables.forEach { solver.addEditVariable(it) }
			allConstraints.forEach { solver += it }
		}
	}

	companion object {
		val variable = ViewVariableProvider()
	}
}

class ViewTest {
	@Test
	fun test() {
		val solver = Solver()

		val window = View().apply {
			constraints += left equals 0.0
			constraints += right equals 800.0
			constraints += top equals 0.0
			constraints += bottom equals 600.0
			this.solver = solver
		}

		val child = View().apply {
			constraints += left equals window.left + 100.0
			constraints += top equals window.top + 200.0
			constraints += width equals 250.0
			constraints += height equals 250.0
			this.solver = solver
		}

		solver.updateVariables()

		window.apply {
			assertEquals(0.0, left.value, 1e-8)
			assertEquals(800.0, right.value, 1e-8)
			assertEquals(0.0, top.value, 1e-8)
			assertEquals(600.0, bottom.value, 1e-8)
			assertEquals(800.0, width.value, 1e-8)
			assertEquals(600.0, height.value, 1e-8)
			assertEquals(400.0, centerX.value, 1e-8)
			assertEquals(300.0, centerY.value, 1e-8)
		}

		child.apply {
			assertEquals(100.0, left.value, 1e-8)
			assertEquals(350.0, right.value, 1e-8)
			assertEquals(200.0, top.value, 1e-8)
			assertEquals(450.0, bottom.value, 1e-8)
			assertEquals(250.0, width.value, 1e-8)
			assertEquals(250.0, height.value, 1e-8)
			assertEquals(225.0, centerX.value, 1e-8)
			assertEquals(325.0, centerY.value, 1e-8)
		}
	}
}