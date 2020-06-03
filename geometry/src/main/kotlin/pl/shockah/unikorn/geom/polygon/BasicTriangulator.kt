@file:Suppress("LocalVariableName")

package pl.shockah.unikorn.geom.polygon

import pl.shockah.unikorn.geom.Triangle
import pl.shockah.unikorn.math.Vector2

/*
 * code taken from Slick2D - http://slick.ninjacave.com/
 */
class BasicTriangulator: Triangulator {
	companion object {
		private const val EPSILON = 0.0000000001f
	}

	override fun triangulate(points: List<Vector2>): List<Triangle>? {
		val process = Process(points)
		return if (process.triangulate()) process.tris else null
	}

	inner class Process(
			val points: List<Vector2>
	) {
		val tris: MutableList<Triangle> = mutableListOf()
		private var tried: Boolean = false

		val triangleCount: Int
			get() {
				if (!tried)
					triangulate()
				return tris.size / 3
			}

		fun triangulate(): Boolean {
			tried = true
			return process(points, tris)
		}

		private fun area(contour: List<Vector2>): Double {
			val n = contour.size

			var A = 0.0
			var p = n - 1
			var q = 0
			while (q < n) {
				val contourP = contour[p]
				val contourQ = contour[q]
				A += contourP.x * contourQ.y - contourQ.x * contourP.y
				p = q++
			}
			return A * 0.5
		}

		private fun insideTriangle(Ax: Double, Ay: Double, Bx: Double, By: Double, Cx: Double, Cy: Double, Px: Double, Py: Double): Boolean {
			val ax: Double = Cx - Bx
			val ay: Double = Cy - By
			val bx: Double = Ax - Cx
			val by: Double = Ay - Cy
			val cx: Double = Bx - Ax
			val cy: Double = By - Ay
			val apx: Double = Px - Ax
			val apy: Double = Py - Ay
			val bpx: Double = Px - Bx
			val bpy: Double = Py - By
			val cpx: Double = Px - Cx
			val cpy: Double = Py - Cy
			val cCROSSap: Double = cx * apy - cy * apx
			val bCROSScp: Double = bx * cpy - by * cpx
			val aCROSSbp: Double = ax * bpy - ay * bpx
			return aCROSSbp >= 0f && bCROSScp >= 0f && cCROSSap >= 0f
		}

		private fun snip(contour: List<Vector2>, u: Int, v: Int, w: Int, n: Int, V: IntArray): Boolean {
			var p = 0
			val Ax: Double = contour[V[u]].x
			val Ay: Double = contour[V[u]].y
			val Bx: Double = contour[V[v]].x
			val By: Double = contour[V[v]].y
			val Cx: Double = contour[V[w]].x
			val Cy: Double = contour[V[w]].y
			var Px: Double
			var Py: Double

			if (EPSILON > (Bx - Ax) * (Cy - Ay) - (By - Ay) * (Cx - Ax))
				return false

			while (p < n) {
				if (p == u || p == v || p == w) {
					p++
					continue
				}
				Px = contour[V[p]].x
				Py = contour[V[p]].y
				if (insideTriangle(Ax, Ay, Bx, By, Cx, Cy, Px, Py))
					return false
				p++
			}

			return true
		}

		private fun process(contour: List<Vector2>, result: MutableList<Triangle>): Boolean {
			result.clear()

			val n = contour.size
			if (n < 3)
				return false

			val V = IntArray(n)

			if (0f < area(contour)) {
				for (v in 0 until n) {
					V[v] = v
				}
			} else {
				for (v in 0 until n) {
					V[v] = n - 1 - v
				}
			}

			var nv = n
			var count = 2 * nv

			var v = nv - 1
			while (nv > 2) {
				if (0 >= count--)
					return false

				var u = v
				if (nv <= u)
					u = 0
				v = u + 1
				if (nv <= v)
					v = 0
				var w = v + 1
				if (nv <= w)
					w = 0

				if (snip(contour, u, v, w, nv, V)) {
					val a: Int = V[u]
					val b: Int = V[v]
					val c: Int = V[w]
					var s: Int = v
					var t: Int = v + 1

					result.add(Triangle(contour[a], contour[b], contour[c]))

					while (t < nv) {
						V[s] = V[t]
						s++
						t++
					}
					nv--
					count = 2 * nv
				}
			}
			return true
		}
	}
}