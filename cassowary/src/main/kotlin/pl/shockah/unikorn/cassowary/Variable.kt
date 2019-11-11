package pl.shockah.unikorn.cassowary

import pl.shockah.unikorn.Ref

interface Variable: ExpressionArgument {
	var value: Double

	override fun buildExpression(constant: Ref<Double>, terms: MutableMap<Variable, Double>) {
		terms[this] = terms.getOrDefault(this, 0.0) + 1.0
	}

	class Simple(
			defaultValue: Double = 0.0
	): Variable {
		override var value = defaultValue
	}
}