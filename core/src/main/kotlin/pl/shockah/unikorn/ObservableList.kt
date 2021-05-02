package pl.shockah.unikorn

class ObservableList<E>(
		private val observedList: MutableList<E> = mutableListOf()
): List<E> by observedList {
	val listeners = mutableListOf<ChangeListener<E>>()

	operator fun plusAssign(element: E) {
		add(element)
	}

	operator fun minusAssign(element: E) {
		remove(element)
	}

	fun add(element: E): Boolean {
		val result = observedList.add(element)
		if (result)
			listeners.forEach { it.onAddedToList(element) }
		return result
	}

	fun remove(element: E): Boolean {
		val result = observedList.remove(element)
		if (result)
			listeners.forEach { it.onRemovedFromList(element) }
		return result
	}

	interface ChangeListener<E> {
		fun onAddedToList(element: E)
		fun onRemovedFromList(element: E)
	}

	interface ChangeAdapter<E>: ChangeListener<E> {
		override fun onAddedToList(element: E) { }
		override fun onRemovedFromList(element: E) { }
	}
}