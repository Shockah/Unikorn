package pl.shockah.unikorn.geom

import pl.shockah.unikorn.ease.Easable
import pl.shockah.unikorn.geom.polygon.ClosedPolygon
import pl.shockah.unikorn.geom.polygon.Polygonable
import pl.shockah.unikorn.math.ImmutableVector2
import pl.shockah.unikorn.math.MutableVector2
import pl.shockah.unikorn.math.Vector2
import kotlin.math.abs

class Triangle(
		point1: Vector2,
		point2: Vector2,
		point3: Vector2
): Polygonable.Closed, Easable<Triangle> {
	val points: List<MutableVector2> = listOf(point1.mutableCopy(), point2.mutableCopy(), point3.mutableCopy())

	override val boundingBox: Rectangle
		get() {
			val minX = minOf(points[0].x, points[1].x, points[2].x)
			val minY = minOf(points[0].y, points[1].y, points[2].y)
			val maxX = maxOf(points[0].x, points[1].x, points[2].x)
			val maxY = maxOf(points[0].y, points[1].y, points[2].y)
			return Rectangle(ImmutableVector2(minX, minY), ImmutableVector2(maxX - minX, maxY - minY))
		}

	override val perimeter: Double
		get() = lines.sumOf { it.perimeter }

	override val area: Double
		get() = abs((points[0].x * (points[1].y - points[2].y) + points[1].x * (points[2].y - points[0].y) + points[2].x * (points[0].y - points[1].y)) * 0.5)

	val lines: List<Line>
		get() = listOf(
				Line(points[0], points[1]),
				Line(points[1], points[2]),
				Line(points[2], points[3])
		)

	companion object {
		init {
			Shape.registerCollisionHandler { a: Triangle, b: Triangle ->
				for (point in b.points) {
					if (point in a)
						return@registerCollisionHandler true
				}
				for (aLine in a.lines) {
					for (bLine in b.lines) {
						if (bLine collides aLine)
							return@registerCollisionHandler true
					}
				}
				return@registerCollisionHandler false
			}
			Shape.registerCollisionHandler { triangle: Triangle, line: Line ->
				if (line.point1 in triangle || line.point2 in triangle)
					return@registerCollisionHandler true
				for (triangleLine in triangle.lines) {
					if (line collides triangleLine)
						return@registerCollisionHandler true
				}
				return@registerCollisionHandler false
			}
		}
	}

	override fun copy(): Triangle {
		return Triangle(points[0], points[1], points[2])
	}

	override fun equals(other: Any?): Boolean {
		return other is Triangle && points == other.points
	}

	override fun hashCode(): Int {
		return points.hashCode()
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

	override operator fun contains(point: Vector2): Boolean {
		fun sign(testedPoint: Vector2, point1: Vector2, point2: Vector2): Double {
			return (testedPoint.x - point2.x) * (point1.y - point2.y) - (point1.x - point2.x) * (testedPoint.y - point2.y)
		}

		val b1 = sign(point, points[0], points[1]) < 0f
		val b2 = sign(point, points[1], points[2]) < 0f
		val b3 = sign(point, points[2], points[0]) < 0f
		return b1 == b2 && b2 == b3
	}

	override fun asClosedPolygon(): ClosedPolygon {
		return ClosedPolygon(points)
	}

	override fun ease(other: Triangle, f: Float): Triangle {
		return Triangle(
				points[0].ease(other.points[0], f),
				points[1].ease(other.points[1], f),
				points[2].ease(other.points[2], f)
		)
	}
}