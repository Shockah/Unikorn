package pl.shockah.unikorn.geom.polygon

import pl.shockah.unikorn.ObservableList
import pl.shockah.unikorn.ease.Easable
import pl.shockah.unikorn.geom.Line
import pl.shockah.unikorn.geom.Rectangle
import pl.shockah.unikorn.geom.Shape
import pl.shockah.unikorn.guard
import pl.shockah.unikorn.math.ImmutableVector2
import pl.shockah.unikorn.math.MutableVector2
import pl.shockah.unikorn.math.Vector2

open class Polygon(
		points: List<Vector2>
): Shape.Outline, Easable<Polygon> {
	val points: ObservableList<MutableVector2> = ObservableList(points.map { it.mutableCopy() }.toMutableList())

	constructor(vararg points: Vector2) : this(points.toMutableList())

	override val boundingBox: Rectangle
		get() {
			val (minX, minY, maxX, maxY) = guard(
					points.map { it.x }::min,
					points.map { it.y }::min,
					points.map { it.x }::max,
					points.map { it.y }::max
			) {
				return Rectangle(size = ImmutableVector2.zero)
			}
			return Rectangle(ImmutableVector2(minX, minY), ImmutableVector2(maxX - minX, maxY - minY))
		}

	open val lines: List<Line>
		get() {
			val result: MutableList<Line> = mutableListOf()
			for (i in 0 until (points.size - 1)) {
				result += Line(points[i], points[i + 1])
			}
			return result
		}

	companion object {
		init {
			Shape.registerCollisionHandler { a: Polygon, b: Polygon ->
				for (aLine in a.lines) {
					for (bLine in b.lines) {
						if (aLine collides bLine)
							return@registerCollisionHandler true
					}
				}
				return@registerCollisionHandler false
			}
			Shape.registerCollisionHandler { polygon: Polygon, line: Line ->
				for (polygonLine in polygon.lines) {
					if (polygonLine collides line)
						return@registerCollisionHandler true
				}
				return@registerCollisionHandler false
			}
		}
	}

	override fun copy(): Polygon {
		return Polygon(points)
	}

	override fun translate(vector: Vector2) {
		points.forEach { it.xy += vector }
	}

	override fun mirror(horizontal: Boolean, vertical: Boolean) {
		if (horizontal)
			points.forEach { it.x *= -1f }
		if (vertical)
			points.forEach { it.y *= -1f }
	}

	override fun scale(scale: Double) {
		points.forEach { it.xy *= scale }
	}

	override fun ease(other: Polygon, f: Float): Polygon {
		if (points.size != other.points.size)
			throw IllegalArgumentException()
		return Polygon(points.mapIndexed { index, point -> point.ease(other.points[index], f) })
	}
}