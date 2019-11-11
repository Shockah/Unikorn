package pl.shockah.unikorn.cassowary

import java.util.*
import kotlin.math.abs

private const val epsilon = 1.0e-8

private val Double.nearZero: Boolean
	get() = abs(this) < epsilon

class Solver {
	private val constraints = mutableMapOf<Constraint, Tag>()
	private val rows = mutableMapOf<Symbol, Row>()
	private val variables = mutableMapOf<Variable, Symbol>()
	private val editMap = mutableMapOf<Variable, EditInfo>()

	private var nextSymbolId = 0
	private val objective = Row()
	private var artificial: Row? = null
	private val infeasibleRows = LinkedList<Symbol>()

	// Creating a row causes symbols to be reserved for the variables
	// in the constraint. If this method exits with an exception,
	// then its possible those variables will linger in the var map.
	// Since its likely that those variables will be used in other
	// constraints and since exceptional conditions are uncommon,
	// i'm not too worried about aggressive cleanup of the var map.
	operator fun plusAssign(constraint: Constraint) {
		check(!constraints.containsKey(constraint))

		val (row, tag) = createRowCreation(constraint)
		var subject = chooseSubject(row, tag)

		// If chooseSubject couldnt find a valid entering symbol, one
		// last option is available if the entire row is composed of
		// dummy variables. If the constant of the row is zero, then
		// this represents redundant constraints and the new dummy
		// marker can enter the basis. If the constant is non-zero,
		// then it represents an unsatisfiable constraint.
		if (subject == null && row.areAllDummies) {
			check(row.constant.nearZero) { "Unsatisfiable constraint" }
			subject = tag.marker
		}

		// If an entering symbol still isn't found, then the row must
		// be added using an artificial variable. If that fails, then
		// the row represents an unsatisfiable constraint.
		if (subject == null) {
			check(addWithArtificialVariable(row)) { "Unsatisfiable constraint" }
		} else {
			row.solveFor(subject)
			substitute(subject, row)
			rows[subject] = row
		}

		constraints[constraint] = tag

		// Optimizing after each constraint is added performs less
		// aggregate work due to a smaller average system size. It
		// also ensures the solver remains in a consistent state.
		optimize(objective)
	}

	operator fun minusAssign(constraint: Constraint) {
		val tag = constraints[constraint] ?: throw IllegalArgumentException("Unknown constraint")

		// Remove the error effects from the objective function
		// *before* pivoting, or substitutions into the objective
		// will lead to incorrect solver results.
		removeConstraintEffects(constraint, tag)

		// If the marker is basic, simply drop the row. Otherwise,
		// pivot the marker into the basis and then drop the row.
		if (rows.remove(tag.marker) == null) {
			val leaving = getMarkerLeavingSymbol(tag.marker!!)
			checkNotNull(leaving) { "Failed to find leaving row" }
			val row = rows.remove(leaving)!!
			row.solveForEx(leaving, tag.marker)
			substitute(tag.marker, row)
		}

		// Optimizing after each constraint is removed ensures that the
		// solver remains consistent. It makes the solver api easier to
		// use at a small tradeoff for speed.
		optimize(objective)
	}

	operator fun contains(constraint: Constraint): Boolean {
		return constraints.containsKey(constraint)
	}

	fun addEditVariable(variable: Variable, strength: Constraint.Strength = Constraint.Strength.strong) {
		require(!editMap.containsKey(variable)) { "Duplicate edit variable $variable" }
		require(strength != Constraint.Strength.required) { "Bad required strength" }

		val constraint = Constraint(Expression(variable), Constraint.Operator.Equal, null, strength)
		this += constraint
		val tag = constraints[constraint]!!
		val info = EditInfo(tag, constraint, 0.0)
		editMap[variable] = info
	}

	fun removeEditVariable(variable: Variable) {
		val editInfo = editMap[variable] ?: throw IllegalArgumentException("Unknown edit variable $variable")
		this -= editInfo.constraint
	}

	fun containsEditVariable(variable: Variable): Boolean {
		return editMap.containsKey(variable)
	}

	fun suggestValue(variable: Variable, value: Double = variable.value) {
		val editInfo = editMap[variable] ?: throw IllegalArgumentException("Unknown edit variable $variable")
		val delta = value - editInfo.constant
		editInfo.constant = value

		fun check(symbol: Symbol): Boolean {
			rows[symbol]?.let { row ->
				row.constant -= delta
				if (row.constant < 0.0)
					infeasibleRows += symbol
				dualOptimize()
				return true
			}
			return false
		}

		// Check first if the positive error variable is basic.
		editInfo.tag.marker?.let {
			if (check(it))
				return
		}

		// Check next if the negative error variable is basic.
		editInfo.tag.other?.let {
			if (check(it))
				return
		}

		// Otherwise update each row where the error variables exist.
		for ((symbol, row) in rows) {
			val coefficient = row.getCoefficient(editInfo.tag.marker!!)
			if (coefficient != 0.0) {
				row.constant += delta * coefficient
				if (row.constant < 0.0 && symbol.type != Symbol.Type.External)
					infeasibleRows += symbol
			}
		}
		dualOptimize()
	}

	fun updateVariables() {
		for ((variable, symbol) in variables) {
			val row = rows[symbol]
			if (row == null)
				variable.value = 0.0
			else
				variable.value = row.constant
		}
	}

	private fun makeSymbol(type: Symbol.Type): Symbol {
		return Symbol(type, nextSymbolId++)
	}

	private fun chooseSubject(row: Row, tag: Tag): Symbol? {
		row.cells.keys.firstOrNull { it.type == Symbol.Type.External }?.let { return it }

		fun get(symbol: Symbol?): Symbol? {
			return symbol?.takeIf { it.type in arrayOf(Symbol.Type.Slack, Symbol.Type.Error) && row.getCoefficient(it) < 0.0 }
		}

		return get(tag.marker) ?: get(tag.other)
	}

	private fun addWithArtificialVariable(row: Row): Boolean {
		// Create and add the artificial variable to the tableau.
		val artificialSymbol = makeSymbol(Symbol.Type.Slack)
		rows[artificialSymbol] = row.copy()
		val artificial = row.copy()
		this.artificial = artificial

		// Optimize the artificial objective. This is successful
		// only if the artificial objective is optimized to zero.
		optimize(artificial)
		val success = artificial.constant.nearZero
		this.artificial = null

		// If the artificial variable is basic, pivot the row so that
		// it becomes non-basic. If the row is constant, exit early.
		rows.remove(artificialSymbol)?.let { basicRow ->
			if (basicRow.isConstant)
				return success
			val entering = anyPivotableSymbol(basicRow) ?: return false
			basicRow.solveForEx(artificialSymbol, entering)
			substitute(entering, basicRow)
			rows[entering] = basicRow
		}

		// Remove the artificial variable from the tableau.
		for (existingRow in rows.values) {
			existingRow.removeSymbol(artificialSymbol)
		}
		objective.removeSymbol(artificialSymbol)
		return success
	}

	private fun substitute(symbol: Symbol, row: Row) {
		for ((existingSymbol, existingRow) in rows) {
			existingRow.substitute(symbol, row)
			if (existingRow.constant < 0.0 && existingSymbol.type != Symbol.Type.External)
				infeasibleRows += existingSymbol
		}
		objective.substitute(symbol, row)
		artificial?.substitute(symbol, row)
	}

	private fun optimize(objective: Row) {
		while (true) {
			val entering = getEnteringSymbol(objective) ?: return
			val leaving = getLeavingSymbol(entering)
			check(leaving != null) { "The objective is unbounded" }
			// pivot the entering symbol into the basis
			val row = rows.remove(leaving)!!
			row.solveForEx(leaving, entering)
			substitute(entering, row)
			rows[entering] = row
		}
	}

	private fun getEnteringSymbol(objective: Row): Symbol? {
		return objective.cells.entries.firstOrNull { (symbol, coefficient) ->
			return@firstOrNull coefficient < 0.0 && symbol.type != Symbol.Type.Dummy
		}?.key
	}

	private fun getLeavingSymbol(entering: Symbol): Symbol? {
		var ratio = Double.MAX_VALUE
		var found: Symbol? = null
		for ((symbol, row) in rows) {
			if (symbol.type == Symbol.Type.External)
				continue
			val temporary = row.getCoefficient(entering)
			if (temporary < 0.0) {
				val temporaryRatio = -row.constant / temporary
				if (temporaryRatio < ratio) {
					ratio = temporaryRatio
					found = symbol
				}
			}
		}
		return found
	}

	private fun getMarkerLeavingSymbol(marker: Symbol): Symbol? {
		val dmax = Double.MAX_VALUE
		var r1 = dmax
		var r2 = dmax
		var first: Symbol? = null
		var second: Symbol? = null
		var third: Symbol? = null

		for ((symbol, row) in rows) {
			val coefficient = row.getCoefficient(marker)
			if (coefficient == 0.0)
				continue

			if (symbol.type == Symbol.Type.External) {
				third = symbol
			} else if (coefficient < 0.0) {
				val r = -row.constant / coefficient
				if (r < r1) {
					r1 = r
					first = symbol
				}
			} else {
				val r = row.constant / coefficient
				if (r < r2) {
					r2 = r
					second = symbol
				}
			}
		}
		return first ?: second ?: third
	}

	private fun anyPivotableSymbol(row: Row): Symbol? {
		return row.cells.keys.firstOrNull { it.type in arrayOf(Symbol.Type.Slack, Symbol.Type.Error) }
	}

	private fun removeConstraintEffects(constraint: Constraint, tag: Tag) {
		if (tag.marker?.type == Symbol.Type.Error)
			removeMarkerEffects(tag.marker, constraint.strength)
		if (tag.other?.type == Symbol.Type.Error)
			removeMarkerEffects(tag.other, constraint.strength)
	}

	private fun removeMarkerEffects(marker: Symbol, strength: Constraint.Strength) {
		val row = rows[marker]
		if (row == null)
			objective.insertSymbol(marker, -strength.value)
		else
			objective.insertRow(row, -strength.value)
	}

	private fun dualOptimize() {
		while (!infeasibleRows.isEmpty()) {
			val leaving = infeasibleRows.removeFirst()!!
			rows[leaving]?.let { row ->
				if (row.constant >= 0.0)
					return@let

				val entering = getDualEnteringSymbol(row) ?: throw IllegalStateException("Dual optimize failed")
				// pivot the entering symbol into the basis
				rows.remove(leaving)
				row.solveForEx(leaving, entering)
				substitute(entering, row)
				rows[entering] = row
			}
		}
	}

	private fun getDualEnteringSymbol(row: Row): Symbol? {
		var ratio = Double.MAX_VALUE
		var entering: Symbol? = null
		for ((symbol, coefficient) in row.cells) {
			if (coefficient > 0.0 && symbol.type != Symbol.Type.Dummy) {
				val objectiveCoefficient = objective.getCoefficient(symbol)
				val r = objectiveCoefficient / coefficient
				if (r < ratio) {
					ratio = r
					entering = symbol
				}
			}
		}
		return entering
	}

	private data class Tag(
			val marker: Symbol?,
			val other: Symbol?
	) {
		constructor(): this(null, null)
	}

	private data class EditInfo(
			val tag: Tag,
			val constraint: Constraint,
			var constant: Double
	)

	private data class Symbol(
			val type: Type?,
			val id: Int
	) {
		enum class Type {
			External, Slack, Error, Dummy
		}
	}

	private class Row(
			var constant: Double = 0.0
	) {
		val cells = mutableMapOf<Symbol, Double>()

		val areAllDummies: Boolean
			get() = cells.all { it.key.type == Symbol.Type.Dummy }

		val isConstant: Boolean
			get() = cells.isEmpty()

		fun copy(): Row {
			return Row(constant).also {
				it.cells.putAll(cells)
			}
		}

		fun insertSymbol(symbol: Symbol, coefficient: Double = 1.0) {
			val newValue = (cells[symbol] ?: 0.0) + coefficient
			if (newValue.nearZero)
				cells.remove(symbol)
			else
				cells[symbol] = newValue
		}

		fun insertRow(other: Row, coefficient: Double = 1.0) {
			constant += other.constant * coefficient
			for ((symbol, existingCoefficient) in other.cells) {
				insertSymbol(symbol, existingCoefficient * coefficient)
			}
		}

		fun reverseSign() {
			constant = -constant
			for (entry in cells) {
				entry.setValue(-entry.value)
			}
		}

		fun getCoefficient(symbol: Symbol): Double {
			return cells[symbol] ?: 0.0
		}

		fun substitute(symbol: Symbol, row: Row) {
			cells.remove(symbol)?.let {
				insertRow(row, it)
			}
		}

		fun removeSymbol(symbol: Symbol) {
			cells.remove(symbol)
		}

		fun solveFor(symbol: Symbol) {
			val coefficient = -1.0 / cells.remove(symbol)!!
			constant *= coefficient
			for (entry in cells) {
				entry.setValue(entry.value * coefficient)
			}
		}

		fun solveForEx(lhs: Symbol, rhs: Symbol) {
			insertSymbol(lhs, -1.0)
			solveFor(rhs)
		}
	}

	private fun createRowCreation(constraint: Constraint): RowCreation {
		val expression = constraint.expression
		val row = Row(expression.constant)

		// Substitute the current basic variables into the row.
		for ((variable, value) in expression.terms) {
			if (value.nearZero)
				continue

			val symbol = variables.computeIfAbsent(variable) { makeSymbol(Symbol.Type.External) }
			val entry = rows[symbol]

			if (entry == null)
				row.insertSymbol(symbol, value)
			else
				row.insertRow(entry, value)
		}

		// Add the necessary slack, error, and dummy variables.
		var tag = Tag()
		when (val operator = constraint.operator) {
			Constraint.Operator.GreaterOrEqual, Constraint.Operator.LessOrEqual -> {
				val coefficient = when (operator) {
					Constraint.Operator.GreaterOrEqual -> -1.0
					Constraint.Operator.LessOrEqual -> 1.0
					else -> throw IllegalStateException()
				}
				val slack = makeSymbol(Symbol.Type.Slack)
				tag = tag.copy(marker = slack)
				row.insertSymbol(slack, coefficient)
				if (constraint.strength != Constraint.Strength.required) {
					val error = makeSymbol(Symbol.Type.Error)
					tag = tag.copy(other = error)
					row.insertSymbol(error, -coefficient)
					objective.insertSymbol(error, constraint.strength.value)
				}
			}
			Constraint.Operator.Equal -> {
				if (constraint.strength == Constraint.Strength.required) {
					val dummy = makeSymbol(Symbol.Type.Dummy)
					tag = tag.copy(marker = dummy)
					row.insertSymbol(dummy)
				} else {
					val errorPlus = makeSymbol(Symbol.Type.Error)
					val errorMinus = makeSymbol(Symbol.Type.Error)
					tag = tag.copy(marker = errorPlus, other = errorMinus)
					row.insertSymbol(errorPlus, -1.0) // v = eplus - eminus
					row.insertSymbol(errorMinus, 1.0) // v - eplus + eminus = 0
					objective.insertSymbol(errorPlus, constraint.strength.value)
					objective.insertSymbol(errorMinus, constraint.strength.value)
				}
			}
		}

		if (row.constant < 0.0)
			row.reverseSign()

		return RowCreation(row, tag)
	}

	private data class RowCreation(
			val row: Row,
			val tag: Tag
	)
}