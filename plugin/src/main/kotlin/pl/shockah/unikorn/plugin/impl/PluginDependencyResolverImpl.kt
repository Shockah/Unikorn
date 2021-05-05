package pl.shockah.unikorn.plugin.impl

import pl.shockah.unikorn.plugin.PluginDependencyResolveResult
import pl.shockah.unikorn.plugin.PluginDependencyResolver
import pl.shockah.unikorn.plugin.PluginInfo

class PluginDependencyResolverImpl: PluginDependencyResolver {
	override fun <PluginInfoType: PluginInfo> resolvePluginDependencies(infosToLoad: Collection<PluginInfoType>, loadedInfos: Collection<PluginInfoType>): PluginDependencyResolveResult<PluginInfoType> {
		val loadOrder = mutableListOf<Set<PluginInfoType>>()
		val infosToLoadLeft = infosToLoad.toMutableList()
		while (infosToLoadLeft.isNotEmpty()) {
			val preCycleCount = infosToLoadLeft.size
			val cycleLoadSet = mutableSetOf<PluginInfoType>()

			val iterator = infosToLoadLeft.iterator()
			while (iterator.hasNext()) {
				val infoToLoad = iterator.next()
				if (infoToLoad.dependencies.all { dependency -> loadedInfos.any { dependency.matches(it) } || loadOrder.flatten().any { dependency.matches(it) } }) {
					cycleLoadSet.add(infoToLoad)
					iterator.remove()
				}
			}

			if (cycleLoadSet.isNotEmpty())
				loadOrder.add(cycleLoadSet)
			if (preCycleCount == infosToLoadLeft.size)
				break
		}

		if (infosToLoadLeft.isEmpty())
			return PluginDependencyResolveResult(loadOrder, emptySet(), emptySet())

		val unresolvableDueToMissingDependencies = mutableSetOf<PluginDependencyResolveResult.UnresolvableDueToMissingDependencies<PluginInfoType>>()
		val unresolvableChains = mutableSetOf<PluginDependencyResolveResult.UnresolvableChain<PluginInfoType>>()

		fun processUnresolvableDueToMissingDependencies() {
			while (infosToLoadLeft.isNotEmpty()) {
				val preCycleCount = infosToLoadLeft.size

				val iterator = infosToLoadLeft.iterator()
				while (iterator.hasNext()) {
					val infoToLoad = iterator.next()
					val unresolvableDependencies = infoToLoad.dependencies.filter { dependency -> unresolvableDueToMissingDependencies.any { dependency.matches(it.info) } }
					val missingDependencies = infoToLoad.dependencies.filter { dependency -> loadedInfos.none { dependency.matches(it) } && infosToLoad.none { dependency.matches(it) } }
					if (missingDependencies.isNotEmpty() || unresolvableDependencies.isNotEmpty()) {
						unresolvableDueToMissingDependencies.add(PluginDependencyResolveResult.UnresolvableDueToMissingDependencies(infoToLoad, (unresolvableDependencies + missingDependencies).toSet()))
						iterator.remove()
					}
				}

				if (preCycleCount == infosToLoadLeft.size)
					break
			}
		}

		fun processUnresolvableChains() {
			while (infosToLoadLeft.isNotEmpty()) {
				val preCycleCount = infosToLoadLeft.size

				val toRemove = mutableSetOf<PluginInfoType>()
				for (infoToLoad in infosToLoad) {
					if (toRemove.contains(infoToLoad))
						continue
					val chain = findDependencyChain(infoToLoad, infosToLoadLeft.toSet())
					if (chain != null) {
						unresolvableChains.add(PluginDependencyResolveResult.UnresolvableChain(chain))
						toRemove.addAll(chain.map { it.first })
					}
				}
				infosToLoadLeft.removeAll(toRemove)

				if (preCycleCount == infosToLoadLeft.size)
					break
			}
		}

		fun processUnresolvable() {
			processUnresolvableDueToMissingDependencies()
			processUnresolvableChains()
		}

		processUnresolvable()
		if (infosToLoadLeft.isNotEmpty())
			throw IllegalStateException("Finished with unknown unresolvable reason for infos: $infosToLoadLeft")
		return PluginDependencyResolveResult(loadOrder, unresolvableDueToMissingDependencies, unresolvableChains)
	}

	private fun <PluginInfoType: PluginInfo> findDependencyChain(infoToLoad: PluginInfoType, knownInfos: Set<PluginInfoType>, currentInfo: PluginInfoType = infoToLoad, currentChain: List<Pair<PluginInfoType, PluginInfo.DependencyEntry>> = emptyList()): List<Pair<PluginInfoType, PluginInfo.DependencyEntry>>? {
		if (currentInfo == infoToLoad && currentChain.isNotEmpty())
			return currentChain
		for (dependency in currentInfo.dependencies) {
			val newInfo = knownInfos.firstOrNull { dependency.matches(it) } ?: continue
			val newChain = currentChain + (newInfo to dependency)
			return findDependencyChain(infoToLoad, knownInfos, newInfo, newChain) ?: continue
		}
		return null
	}
}