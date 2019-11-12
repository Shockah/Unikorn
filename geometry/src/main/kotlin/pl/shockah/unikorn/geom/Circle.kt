package pl.shockah.unikorn.geom

import pl.shockah.unikorn.ease.Easable
import pl.shockah.unikorn.ease.ease
import pl.shockah.unikorn.geom.polygon.Polygon
import pl.shockah.unikorn.math.ImmutableVector2
import pl.shockah.unikorn.math.MutableVector2
import pl.shockah.unikorn.math.Vector2
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

class Circle(
		position: Vector2 = ImmutableVector2.zero,
		var radius: Double
): Shape.Filled, Easable<Circle> {
	var position: MutableVector2 = position.mutableCopy()

	override val boundingBox: Rectangle
		get() = Rectangle.centered(position, ImmutableVector2(radius * 2, radius * 2))

	override val center: Vector2
		get() = position

	override val perimeter: Double
		get() = 2.0 * PI * radius

	override val area: Double
		get() = PI * radius.pow(2)

	companion object {
		init {
			Shape.registerCollisionHandler { a: Circle, b: Circle ->
				(b.position - a.position).length < b.radius + a.radius
			}
			Shape.registerCollisionHandler { circle: Circle, line: Line ->
				line.point1 in circle || line.point2 in circle || !(circle intersect line).isEmpty()
			}
			Shape.registerCollisionHandler { circle: Circle, rectangle: Rectangle ->
				val testPoint = circle.position.mutableCopy()

				if (circle.position.x < rectangle.position.x)
					testPoint.x = rectangle.position.x
				else if (circle.position.x > rectangle.position.x + rectangle.size.x)
					testPoint.x = rectangle.position.x + rectangle.size.x

				if (circle.position.y < rectangle.position.y)
					testPoint.y = rectangle.position.y
				else if (circle.position.y > rectangle.position.y + rectangle.size.y)
					testPoint.y = rectangle.position.y + rectangle.size.y

				return@registerCollisionHandler (circle.position - testPoint).length < circle.radius
			}
			Shape.registerCollisionHandler { circle: Circle, polygon: Polygon ->
				for (line in polygon.lines) {
					if (circle collides line)
						return@registerCollisionHandler true
				}
				return@registerCollisionHandler false
			}
		}
	}

	override fun copy(): Circle {
		return Circle(position, radius)
	}

	override fun equals(other: Any?): Boolean {
		return other is Circle && position == other.position && radius == other.radius
	}

	override fun hashCode(): Int {
		return position.hashCode() * 31 + radius.hashCode()
	}

	override fun translate(vector: Vector2) {
		position.xy += vector
	}

	override fun mirror(horizontal: Boolean, vertical: Boolean) {
		if (horizontal)
			position.x *= -1f
		if (vertical)
			position.y *= -1f
	}

	override fun scale(scale: Double) {
		position.xy *= scale
		radius *= scale
	}

	override operator fun contains(point: Vector2): Boolean {
		return (position - point).length <= radius
	}

	infix fun intersect(line: Line): List<ImmutableVector2> {
		val baX = line.point2.x - line.point1.x
		val baY = line.point2.y - line.point1.y
		val caX = position.x - line.point1.x
		val caY = position.y - line.point1.y

		val a = baX * baX + baY * baY
		val bBy2 = baX * caX + baY * caY
		val c = caX * caX + caY * caY - radius * radius

		val pBy2 = bBy2 / a
		val q = c / a

		val disc = pBy2 * pBy2 - q
		if (disc < 0)
			return emptyList()

		val tmpSqrt = sqrt(disc)
		val abScalingFactor1 = -pBy2 + tmpSqrt
		val abScalingFactor2 = -pBy2 - tmpSqrt

		val p1 = ImmutableVector2(line.point1.x - baX * abScalingFactor1, line.point1.y - baY * abScalingFactor1)
		if (disc == 0.0)
			return listOf(p1)

		val p2 = ImmutableVector2(line.point1.x - baX * abScalingFactor2, line.point1.y - baY * abScalingFactor2)
		return listOf(p1, p2)
	}

	override fun ease(other: Circle, f: Float): Circle {
		return Circle(
				position.ease(other.position, f),
				f.ease(radius.toFloat(), other.radius.toFloat()).toDouble()
		)
	}
}