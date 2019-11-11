package pl.shockah.unikorn.cassowary

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleTest {
	@Test
	fun test() {
		val solver = Solver()

		val left = Variable.Simple(100.0)
		val width = Variable.Simple(400.0)

		solver.addEditVariable(left)
		solver.addEditVariable(width)
		solver.suggestValue(left)
		solver.suggestValue(width)

		val right = Variable.Simple()
		solver += width equals right - left

		solver.updateVariables()
		assertEquals(500.0, right.value)
	}
}