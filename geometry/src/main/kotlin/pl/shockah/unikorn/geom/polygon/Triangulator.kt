package pl.shockah.unikorn.geom.polygon

import pl.shockah.unikorn.geom.Triangle
import pl.shockah.unikorn.math.Vector2

interface Triangulator {
	fun triangulate(points: List<Vector2>): List<Triangle>?
}