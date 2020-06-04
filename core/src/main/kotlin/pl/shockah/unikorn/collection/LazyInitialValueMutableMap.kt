package pl.shockah.unikorn.collection

class LazyInitialValueMutableMap<K, V>(
		wrapped: MutableMap<K, V>,
		private val initializer: (K) -> V
): MutableMap<K, V> by wrapped {
	private val wrapped = wrapped.withDefault(initializer)
	private val initializedKeys = mutableSetOf<K>()

	private fun initializeIfNeeded(key: K) {
		if (!initializedKeys.contains(key)) {
			wrapped[key] = initializer(key)
			initializedKeys.add(key)
		}
	}

	override fun containsKey(key: K): Boolean {
		return !initializedKeys.contains(key) || wrapped.containsKey(key)
	}

	override fun get(key: K): V? {
		initializeIfNeeded(key)
		return wrapped[key]
	}

	override fun getOrDefault(key: K, defaultValue: V): V {
		initializeIfNeeded(key)
		return wrapped[key]!!
	}

	override fun put(key: K, value: V): V? {
		initializedKeys.add(key)
		return wrapped.put(key, value)
	}

	override fun putAll(from: Map<out K, V>) {
		initializedKeys.addAll(from.keys)
		wrapped.putAll(from)
	}
}