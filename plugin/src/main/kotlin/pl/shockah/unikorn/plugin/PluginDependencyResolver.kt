package pl.shockah.unikorn.plugin

data class PluginDependencyResolveResult<PluginInfoType: PluginInfo>(
		val loadOrder: List<Set<PluginInfoType>>,
		val unresolvableDueToMissingDependencies: Set<UnresolvableDueToMissingDependencies<PluginInfoType>>,
		val unresolvableChains: Set<UnresolvableChain<PluginInfoType>>
) {
	data class UnresolvableDueToMissingDependencies<PluginInfoType: PluginInfo>(
			val info: PluginInfoType,
			val missingDependencies: Set<PluginInfo.DependencyEntry>
	)

	data class UnresolvableChain<PluginInfoType: PluginInfo>(
			val chain: List<Pair<PluginInfoType, PluginInfo.DependencyEntry>>
	) {
		override fun equals(other: Any?): Boolean {
			if (other !is UnresolvableChain<*>)
				return false
			if (other.chain.size != chain.size)
				return false
			// chains are equal if their elements are in the same order, no matter the offset
			startIndexes@ for (startIndex in chain.indices) {
				for (i in chain.indices) {
					if (other.chain[(startIndex + i) % chain.size] != chain[i])
						continue@startIndexes
				}
				return true
			}
			return false
		}

		override fun hashCode(): Int {
			return chain.toSet().hashCode()
		}
	}
}

interface PluginDependencyResolver {
	fun <PluginInfoType: PluginInfo> resolvePluginDependencies(infosToLoad: Collection<PluginInfoType>, loadedInfos: Collection<PluginInfoType>): PluginDependencyResolveResult<PluginInfoType>
}