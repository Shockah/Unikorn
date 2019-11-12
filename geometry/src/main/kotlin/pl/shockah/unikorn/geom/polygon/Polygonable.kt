package pl.shockah.unikorn.geom.polygon

import pl.shockah.unikorn.geom.Shape

class Polygonable private constructor() {
	interface Open: Shape {
		fun asPolygon(): Polygon
	}

	interface Closed: Shape.Filled, Open {
		fun asClosedPolygon(): ClosedPolygon

		override fun asPolygon(): Polygon {
			val closedPolygon = asClosedPolygon()
			return Polygon(listOf(*closedPolygon.points.toTypedArray(), closedPolygon.points.first()))
		}
	}
}