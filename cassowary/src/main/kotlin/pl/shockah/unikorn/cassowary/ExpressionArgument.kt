package pl.shockah.unikorn.cassowary

import pl.shockah.unikorn.Ref

interface ExpressionArgument {
	fun buildExpression(constant: Ref<Double>, terms: MutableMap<Variable, Double>)

	data class Scalar(
			val value: Double
	): ExpressionArgument {
		override fun buildExpression(constant: Ref<Double>, terms: MutableMap<Variable, Double>) {
			constant.value += value
		}
	}

	data class Multiplier(
			val multiplier: Double,
			val argument: ExpressionArgument
	): ExpressionArgument {
		override fun buildExpression(constant: Ref<Double>, terms: MutableMap<Variable, Double>) {
			val innerConstant = Ref(0.0)
			val innerTerms = mutableMapOf<Variable, Double>()

			argument.buildExpression(innerConstant, innerTerms)

			constant.value += innerConstant.value * multiplier
			for ((variable, coefficient) in innerTerms) {
				terms[variable] = terms.getOrDefault(variable, 0.0) + coefficient * multiplier
			}
		}
	}
}

operator fun ExpressionArgument.unaryMinus(): ExpressionArgument {
	return ExpressionArgument.Multiplier(-1.0, this)
}

operator fun ExpressionArgument.plus(scalar: Double): ExpressionArgument {
	return this + ExpressionArgument.Scalar(scalar)
}

operator fun ExpressionArgument.minus(scalar: Double): ExpressionArgument {
	return this + ExpressionArgument.Scalar(-scalar)
}

operator fun ExpressionArgument.plus(argument: ExpressionArgument): ExpressionArgument {
	return Expression(this, argument)
}

operator fun ExpressionArgument.minus(argument: ExpressionArgument): ExpressionArgument {
	return Expression(this, ExpressionArgument.Multiplier(-1.0, argument))
}

operator fun ExpressionArgument.times(coefficient: Double): ExpressionArgument {
	return Expression(ExpressionArgument.Multiplier(coefficient, this))
}

operator fun ExpressionArgument.div(coefficient: Double): ExpressionArgument {
	require(coefficient != 0.0)
	return Expression(ExpressionArgument.Multiplier(1.0 / coefficient, this))
}