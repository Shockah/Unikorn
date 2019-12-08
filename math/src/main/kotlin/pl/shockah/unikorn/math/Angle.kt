package pl.shockah.unikorn.math

import pl.shockah.unikorn.ease.Easable

typealias Angle = BaseAngle<*>

interface BaseAngle<T: BaseAngle<T>>: Easable<BaseAngle<*>> {
	val degrees: Degrees
	val radians: Radians

	val sin: Double
	val cos: Double
	val tan: Double

	infix fun delta(angle: Angle): T

	operator fun plus(other: Angle): T
	operator fun minus(other: Angle): T

	fun rotated(fullRotations: Double): T
}