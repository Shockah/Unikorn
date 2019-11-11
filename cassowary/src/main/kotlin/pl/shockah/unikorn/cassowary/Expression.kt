package pl.shockah.unikorn.cassowary

import pl.shockah.unikorn.Ref

class Expression private constructor(
		val terms: Map<Variable, Double>,
		val constant: Double
): ExpressionArgument {
	val isConstant: Boolean
		get() = terms.isEmpty()

	val value: Double by lazy {
		var result = constant
		for (term in terms) {
			result += term.key.value * term.value
		}
		return@lazy result
	}

	companion object {
		operator fun invoke(vararg arguments: ExpressionArgument): Expression {
			return this(arguments.toList())
		}

		operator fun invoke(arguments: Collection<ExpressionArgument>): Expression {
			val constant = Ref(0.0)
			val terms = mutableMapOf<Variable, Double>()

			for (argument in arguments) {
				argument.buildExpression(constant, terms)
			}

			return Expression(terms, constant.value)
		}
	}

	override fun buildExpression(constant: Ref<Double>, terms: MutableMap<Variable, Double>) {
		constant.value += this.constant
		for (term in this.terms) {
			terms[term.key] = terms.getOrDefault(term.key, 0.0) + term.value
		}
	}
}