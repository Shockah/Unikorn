package pl.shockah.unikorn.geom.polygon

import pl.shockah.unikorn.ObservableList
import pl.shockah.unikorn.geom.Line
import pl.shockah.unikorn.geom.Shape
import pl.shockah.unikorn.geom.Triangle
import pl.shockah.unikorn.math.MutableVector2
import pl.shockah.unikorn.math.Vector2
import pl.shockah.unikorn.property.LazyDirty

class ClosedPolygon(
		points: List<Vector2>
): Polygon(points), Shape.Filled {
	var triangulator: Triangulator = BasicTriangulator()

	private val dirtyTriangles = LazyDirty {
		triangulator.triangulate(points) ?: throw IllegalStateException("Cannot triangulate polygon.")
	}
	val triangles: List<Triangle> by dirtyTriangles

	override val lines: List<Line>
		get() {
			val result = mutableListOf<Line>()
			for (i in 0 until points.size) {
				result += Line(points[i], points[(i + 1) % points.size])
			}
			return result
		}

	constructor(vararg points: Vector2) : this(points.toList())

	init {
		super.points.listeners += object : ObservableList.ChangeListener<MutableVector2> {
			override fun onAddedToList(element: MutableVector2) {
				dirtyTriangles.invalidate()
			}

			override fun onRemovedFromList(element: MutableVector2) {
				dirtyTriangles.invalidate()
			}
		}
	}

	companion object {
		init {
			Shape.registerCollisionHandler { a: ClosedPolygon, b: ClosedPolygon ->
				for (aTriangle in a.triangles) {
					for (bTriangle in b.triangles) {
						if (aTriangle collides bTriangle)
							return@registerCollisionHandler true
					}
				}
				return@registerCollisionHandler false
			}
			Shape.registerCollisionHandler { polygon: ClosedPolygon, triangle: Triangle ->
				for (polygonTriangle in polygon.triangles) {
					if (triangle collides polygonTriangle)
						return@registerCollisionHandler true
				}
				return@registerCollisionHandler false
			}
			Shape.registerCollisionHandler { polygon: ClosedPolygon, line: Line ->
				for (polygonTriangle in polygon.triangles) {
					if (line collides polygonTriangle)
						return@registerCollisionHandler true
				}
				return@registerCollisionHandler false
			}
		}
	}

	override fun copy(): ClosedPolygon {
		return ClosedPolygon(points).apply {
			triangulator = this@ClosedPolygon.triangulator
		}
	}

	override fun contains(point: Vector2): Boolean {
		for (triangle in triangles) {
			if (point in triangle)
				return true
		}
		return false
	}

	override fun ease(other: Polygon, f: Float): ClosedPolygon {
		if (other !is ClosedPolygon)
			throw IllegalArgumentException()
		if (points.size != other.points.size)
			throw IllegalArgumentException()
		return ClosedPolygon(points.mapIndexed { index, point -> point.ease(other.points[index], f) })
	}
}