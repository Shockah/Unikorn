package pl.shockah.unikorn.geom

import pl.shockah.unikorn.ease.Easable
import pl.shockah.unikorn.geom.polygon.Polygon
import pl.shockah.unikorn.geom.polygon.Polygonable
import pl.shockah.unikorn.math.*
import kotlin.math.max
import kotlin.math.min

class Line(
		point1: Vector2 = ImmutableVector2.zero,
		point2: Vector2
): Polygonable.Open, Easable<Line> {
	var point1: MutableVector2 = point1.mutableCopy()
	var point2: MutableVector2 = point2.mutableCopy()

	override val boundingBox: Rectangle
		get() {
			val minX = min(point1.x, point2.x)
			val minY = min(point1.y, point2.y)
			val maxX = max(point1.x, point2.x)
			val maxY = max(point1.y, point2.y)
			return Rectangle(ImmutableVector2(minX, minY), ImmutableVector2(maxX - minX, maxY - minY))
		}

	override val center: Vector2
		get() = (point1 + point2) * 0.5

	val degrees: Degrees
		get() = (point2 - point1).degrees

	val radians: Radians
		get() = (point2 - point1).radians

	companion object {
		init {
			Shape.registerCollisionHandler { a: Line, b: Line ->
				a intersect b != null
			}
		}
	}

	override fun copy(): Line {
		return Line(point1, point2)
	}

	override fun equals(other: Any?): Boolean {
		return other is Line && point1 == other.point1 && point2 == other.point2
	}

	override fun hashCode(): Int {
		return point1.hashCode() * 31 + point2.hashCode()
	}

	override fun translate(vector: Vector2) {
		point1.xy += vector
		point2.xy += vector
	}

	override fun mirror(horizontal: Boolean, vertical: Boolean) {
		if (horizontal) {
			point1.x *= -1f
			point2.x *= -1f
		}
		if (vertical) {
			point1.y *= -1f
			point2.y *= -1f
		}
	}

	override fun scale(scale: Double) {
		point1.xy *= scale
		point2.xy *= scale
	}

	infix fun intersect(line: Line): Vector2? {
		val dx1 = point2.x - point1.x
		val dx2 = line.point2.x - line.point1.x
		val dy1 = point2.y - point1.y
		val dy2 = line.point2.y - line.point1.y
		val denom = dy2 * dx1 - dx2 * dy1

		if (denom == 0.0)
			return null

		val ua = (dx2 * (point1.y - line.point1.y) - dy2 * (point1.x - line.point1.x)) / denom
		val ub = (dx1 * (point1.y - line.point1.y) - dy1 * (point1.x - line.point1.x)) / denom

		if (ua < 0 || ua > 1 || ub < 0 || ub > 1)
			return null

		val ix = point1.x + ua * (point2.x - point1.x)
		val iy = point1.y + ua * (point2.y - point1.y)
		return ImmutableVector2(ix, iy)
	}

	override fun asPolygon(): Polygon {
		return Polygon(point1, point2)
	}

	override fun ease(other: Line, f: Float): Line {
		return Line(
				point1.ease(other.point1, f),
				point2.ease(other.point2, f)
		)
	}
}