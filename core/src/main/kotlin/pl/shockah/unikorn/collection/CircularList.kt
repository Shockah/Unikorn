package pl.shockah.unikorn.collection

import kotlin.math.absoluteValue

interface CircularList<E>: Collection<E>

interface MutableCircularList<E>: CircularList<E>, MutableCollection<E> {
	fun addNext(element: E)

	fun addPrevious(element: E)

	fun removeNext(): E

	fun removePrevious(): E

	fun rotate(values: Int)
}

open class CircularListImpl<E> internal constructor(): Collection<E> {
	internal inner class Node(
			val value: E
	) {
		var next: Node = this
		var previous: Node = this
	}

	internal var root: Node? = null

	override var size: Int = 0
		protected set

	override fun contains(element: E): Boolean {
		var current: Node = root ?: return false
		repeat(size) {
			if (current.value == element)
				return true
			current = current.next
		}
		return false
	}

	override fun containsAll(elements: Collection<E>): Boolean {
		if (elements.isEmpty())
			return true
		val left = elements.toMutableList()

		var current: Node = root ?: return false
		repeat(size) {
			if (current.value in left) {
				left -= current.value
				if (left.isEmpty())
					return true
			}
			current = current.next
		}
		return false
	}

	override fun isEmpty(): Boolean {
		return size == 0
	}

	override fun iterator(): kotlin.collections.Iterator<E> {
		return Iterator()
	}

	open inner class Iterator : kotlin.collections.Iterator<E> {
		internal var currentNode: Node? = null
		protected var index = 0

		override fun hasNext(): Boolean {
			return index < size
		}

		override fun next(): E {
			index++
			currentNode = currentNode?.next ?: root
			return currentNode!!.value
		}
	}
}

class MutableCircularListImpl<E> : CircularListImpl<E>(), MutableCircularList<E> {
	override fun addNext(element: E) {
		val newNode = Node(element)
		val root = root
		if (root == null) {
			this.root = newNode
			size++
		} else {
			val tmp = root.next
			root.next = newNode
			newNode.previous = root
			newNode.next = tmp
			tmp.previous = newNode
			size++
		}
	}

	override fun addPrevious(element: E) {
		val newNode = Node(element)
		val root = root
		if (root == null) {
			this.root = newNode
			size++
		} else {
			val tmp = root.previous
			root.previous = newNode
			newNode.next = root
			newNode.previous = tmp
			tmp.next = newNode
			size++
		}
	}

	override fun add(element: E): Boolean {
		addPrevious(element)
		return true
	}

	override fun addAll(elements: Collection<E>): Boolean {
		elements.forEach { addPrevious(it) }
		return true
	}

	override fun clear() {
		root = null
		size = 0
	}

	override fun iterator(): kotlin.collections.MutableIterator<E> {
		return MutableIterator()
	}

	override fun removeNext(): E {
		val root = root ?: throw IllegalStateException()

		if (size == 1) {
			val value = root.value
			this.root = null
			size = 0
			return value
		} else {
			val current = root.next
			val previous = current.previous
			val next = current.next
			previous.next = next
			next.previous = previous
			size--
			return current.value
		}
	}

	override fun removePrevious(): E {
		val root = root ?: throw IllegalStateException()

		if (size == 1) {
			val value = root.value
			this.root = null
			size = 0
			return value
		} else {
			val current = root.previous
			val previous = current.previous
			val next = current.next
			previous.next = next
			next.previous = previous
			size--
			return current.value
		}
	}

	override fun remove(element: E): Boolean {
		val root = root ?: return false

		if (size == 1 && root.value == element) {
			this.root = null
			size = 0
		} else {
			var current: Node = root
			repeat(size) {
				if (current.value == element) {
					val previous = current.previous
					val next = current.next
					previous.next = next
					next.previous = previous
					size--
					return true
				}
				current = current.next
			}
		}
		return false
	}

	override fun removeAll(elements: Collection<E>): Boolean {
		return elements.map { remove(it) }.any { it }
	}

	override fun retainAll(elements: Collection<E>): Boolean {
		return removeAll(this - elements)
	}

	override fun rotate(values: Int) {
		val root = root
		if (values == 0 || root == null || size < 2)
			return

		val goingNext = values > 0
		val times = values.absoluteValue

		var current: Node = root
		repeat(times) {
			current = if (goingNext) current.next else current.previous
		}
		this.root = current
	}

	inner class MutableIterator : Iterator(), kotlin.collections.MutableIterator<E> {
		private var removed = true

		override fun remove() {
			check(!removed)

			if (size == 1) {
				root = null
				currentNode = null
			} else {
				val previous = currentNode!!.previous
				val next = currentNode!!.next
				previous.next = next
				next.previous = previous
				currentNode = previous
			}

			index--
			size--
			removed = true
		}
	}
}