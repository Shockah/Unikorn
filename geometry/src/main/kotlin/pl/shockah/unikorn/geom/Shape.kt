package pl.shockah.unikorn.geom

import pl.shockah.unikorn.geom.polygon.Polygonable
import pl.shockah.unikorn.math.ImmutableVector2
import pl.shockah.unikorn.math.Vector2
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

interface Shape {
	val boundingBox: Rectangle

	val center: Vector2
		get() = boundingBox.center

	val perimeter: Double

	companion object {
		val none = object: Filled {
			override val boundingBox: Rectangle
				get() = Rectangle(size = ImmutableVector2.zero)

			override val perimeter: Double
				get() = 0.0

			override val area: Double
				get() = 0.0

			override fun copy(): Filled {
				return this
			}

			override fun translate(vector: Vector2) {
			}

			override fun mirror(horizontal: Boolean, vertical: Boolean) {
			}

			override fun scale(scale: Double) {
			}

			override fun contains(point: Vector2): Boolean {
				return false
			}
		}

		val infinitePlane = object: Filled {
			override val boundingBox: Rectangle
				get() = Rectangle(
						ImmutableVector2(-Double.MAX_VALUE, -Double.MAX_VALUE),
						ImmutableVector2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
				)

			override val perimeter: Double
				get() = Double.POSITIVE_INFINITY

			override val area: Double
				get() = Double.POSITIVE_INFINITY

			override fun copy(): Filled {
				return this
			}

			override fun translate(vector: Vector2) {
			}

			override fun mirror(horizontal: Boolean, vertical: Boolean) {
			}

			override fun scale(scale: Double) {
			}

			override fun contains(point: Vector2): Boolean {
				return true
			}
		}

		@PublishedApi
		internal val collisionHandlers: MutableMap<Pair<KClass<out Shape>, KClass<out Shape>>, (Shape, Shape) -> Boolean> = mutableMapOf()

		inline fun <reified A: Shape, reified B: Shape> registerCollisionHandler(noinline handler: (A, B) -> Boolean) {
			@Suppress("UNCHECKED_CAST")
			collisionHandlers[Pair(A::class, B::class)] = handler as ((Shape, Shape) -> Boolean)
		}

		fun <A: Shape, B: Shape> shapesCollide(first: A, second: B): Boolean {
			val handler = findHandler(first::class, second::class)
			if (handler != null)
				return handler(first, second)

			if (first is Polygonable.Closed && second is Polygonable.Closed)
				return first.asClosedPolygon() collides second.asClosedPolygon()

			if (first is Polygonable.Open && second is Polygonable.Open)
				return first.asPolygon() collides second.asPolygon()

			throw UnsupportedOperationException("${first::class.simpleName} --><-- ${second::class.simpleName} collision isn't implemented.")
		}

		private fun findHandler(first: KClass<out Shape>, second: KClass<out Shape>): ((Shape, Shape) -> Boolean)? {
			return collisionHandlers[Pair(first, second)] ?: collisionHandlers[Pair(second, first)] ?: findHandlerFromSupertypes(first, second)
		}

		private fun findHandlerFromSupertypes(first: KClass<out Shape>, second: KClass<out Shape>): ((Shape, Shape) -> Boolean)? {
			for (firstSubclass in first.superclasses.filter { it != Shape::class && it.isSubclassOf(Shape::class) }.map {
				@Suppress("UNCHECKED_CAST")
				it as KClass<out Shape>
			}) {
				val result = findHandler(firstSubclass, second)
				if (result != null)
					return result
			}
			for (secondSubclass in second.superclasses.filter { it != Shape::class && it.isSubclassOf(Shape::class) }.map {
				@Suppress("UNCHECKED_CAST")
				it as KClass<out Shape>
			}) {
				val result = findHandler(first, secondSubclass)
				if (result != null)
					return result
			}
			return null
		}
	}

	fun copy(): Shape

	fun translate(vector: Vector2)

	fun mirror(horizontal: Boolean, vertical: Boolean)

	fun scale(scale: Double)

	infix fun collides(other: Shape): Boolean {
		return shapesCollide(this, other)
	}

	interface Filled: Shape {
		val area: Double

		operator fun contains(point: Vector2): Boolean
	}
}