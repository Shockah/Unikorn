package pl.shockah.unikorn.cassowary

fun Double.mix(a: Constraint.Strength, b: Constraint.Strength): Constraint.Strength {
	return a + (b - a) * this
}

infix fun ExpressionArgument.equals(other: ExpressionArgument): Constraint {
	return Constraint(this, Constraint.Operator.Equal, other)
}

infix fun ExpressionArgument.equals(other: Double): Constraint {
	return Constraint(this, Constraint.Operator.Equal, ExpressionArgument.Scalar(other))
}

infix fun ExpressionArgument.greaterOrEquals(other: ExpressionArgument): Constraint {
	return Constraint(this, Constraint.Operator.GreaterOrEqual, other)
}

infix fun ExpressionArgument.lessOrEqual(other: ExpressionArgument): Constraint {
	return Constraint(this, Constraint.Operator.LessOrEqual, other)
}

data class Constraint internal constructor(
		val expression: Expression,
		val operator: Operator,
		val strength: Strength
) {
	companion object {
		operator fun invoke(lhs: ExpressionArgument, operator: Operator, rhs: ExpressionArgument? = null, strength: Strength = Strength.required): Constraint {
			return Constraint(Expression(if (rhs == null) lhs else lhs - rhs), operator, strength)
		}
	}

	enum class Operator {
		Equal, GreaterOrEqual, LessOrEqual
	}

	data class Strength(
			val value: Double
	) {
		init {
			require(value in 0.0..1_000_000_000.0)
		}

		operator fun plus(other: Strength): Strength {
			return Strength(value + other.value)
		}

		operator fun minus(other: Strength): Strength {
			return Strength(value - other.value)
		}

		operator fun times(other: Strength): Strength {
			return Strength(value * other.value)
		}

		operator fun times(scalar: Double): Strength {
			return Strength(value * scalar)
		}

		operator fun div(other: Strength): Strength {
			return Strength(value / other.value)
		}

		companion object {
			val required = Strength(1_000_000_000.0)
			val strong = Strength(1_000_000.0)
			val medium = Strength(1_000.0)
			val weak = Strength(1.0)
		}
	}
}