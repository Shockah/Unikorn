package pl.shockah.unikorn.geom

import pl.shockah.unikorn.ease.Easable
import pl.shockah.unikorn.geom.polygon.ClosedPolygon
import pl.shockah.unikorn.geom.polygon.Polygonable
import pl.shockah.unikorn.math.ImmutableVector2
import pl.shockah.unikorn.math.MutableVector2
import pl.shockah.unikorn.math.Vector2

class Rectangle(
		position: Vector2 = ImmutableVector2.zero,
		size: Vector2
): Polygonable.Closed, Easable<Rectangle> {
	var position: MutableVector2 = position.mutableCopy()
	var size: MutableVector2 = size.mutableCopy()

	override val boundingBox: Rectangle
		get() = copy()

	override val center: Vector2
		get() = position + size * 0.5

	override val perimeter: Double
		get() = (size.x + size.y) * 2.0

	override val area: Double
		get() = size.x * size.y

	val left: Double
		get() = position.x

	val right: Double
		get() = position.x + size.x

	val top: Double
		get() = position.y

	val bottom: Double
		get() = position.y + size.y

	val topLeft: Vector2
		get() = position

	val topRight: Vector2
		get() = ImmutableVector2(position.x + size.x, position.y)

	val bottomLeft: Vector2
		get() = ImmutableVector2(position.x, position.y + size.y)

	val bottomRight: Vector2
		get() = position + size

	val lines: List<Line>
		get() = listOf(
				Line(topLeft, topRight),
				Line(topRight, bottomRight),
				Line(bottomRight, bottomLeft),
				Line(bottomLeft, topLeft)
		)

	companion object {
		fun centered(position: Vector2, size: Vector2): Rectangle {
			return Rectangle(position - size * 0.5, size)
		}

		init {
			Shape.registerCollisionHandler { a: Rectangle, b: Rectangle ->
				a.position.x < b.position.x + b.size.x
						&& a.position.x + a.size.x > b.position.x
						&& a.position.y < b.position.y + b.size.y
						&& a.position.y + a.size.y > b.position.y
			}
			Shape.registerCollisionHandler { rectangle: Rectangle, line: Line ->
				if (line.point1 in rectangle || line.point2 in rectangle)
					return@registerCollisionHandler true
				for (rectangleLine in rectangle.lines) {
					if (line collides rectangleLine)
						return@registerCollisionHandler true
				}
				return@registerCollisionHandler false
			}
		}
	}

	override fun copy(): Rectangle {
		return Rectangle(position, size)
	}

	override fun equals(other: Any?): Boolean {
		return other is Rectangle && other.position == position && other.size == size
	}

	override fun hashCode(): Int {
		return position.hashCode() * 31 + size.hashCode()
	}

	override fun translate(vector: Vector2) {
		position.xy += vector
	}

	override fun mirror(horizontal: Boolean, vertical: Boolean) {
		if (horizontal)
			position.x = -position.x - size.x
		if (vertical)
			position.y = -position.y - size.y
	}

	override fun scale(scale: Double) {
		position.xy *= scale
		size.xy *= scale
	}

	override operator fun contains(point: Vector2): Boolean {
		return point.x in left..right && point.y in top..bottom
	}

	override fun asClosedPolygon(): ClosedPolygon {
		return ClosedPolygon(topLeft, topRight, bottomRight, bottomLeft)
	}

	override fun ease(other: Rectangle, f: Float): Rectangle {
		return Rectangle(
				position.ease(other.position, f),
				size.ease(other.size, f)
		)
	}
}