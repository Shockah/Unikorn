package pl.shockah.unikorn.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pl.shockah.unikorn.plugin.impl.PluginDependencyResolverImpl

class PluginDependencyResolverImplTest {
	private val resolver = PluginDependencyResolverImpl()

	@Test
	fun testNoDependenciesResolve() {
		val infosToLoad = setOf(
				PluginInfo.WithReference("a", "a", reference = Unit),
				PluginInfo.WithReference("b", "b", reference = Unit),
				PluginInfo.WithReference("c", "c", reference = Unit)
		)

		val result = resolver.resolvePluginDependencies(infosToLoad, emptySet())

		assertTrue(result.unresolvableDueToMissingDependencies.isEmpty())
		assertTrue(result.unresolvableChains.isEmpty())
		assertEquals(1, result.loadOrder.size)
		assertEquals(infosToLoad, result.loadOrder.single())
	}

	@Test
	fun testDependenciesResolve() {
		val firstPhaseInfos = setOf(
				PluginInfo.WithReference("a1", "a1", reference = Unit),
				PluginInfo.WithReference("a2", "a2", reference = Unit)
		)
		val secondPhaseInfos = setOf(
				PluginInfo.WithReference("b1", "b1", dependencies = setOf(PluginInfo.DependencyEntry("a1")), reference = Unit),
				PluginInfo.WithReference("b2", "b2", dependencies = setOf(PluginInfo.DependencyEntry("a1"), PluginInfo.DependencyEntry("a2")), reference = Unit)
		)
		val thirdPhaseInfos = setOf(
				PluginInfo.WithReference("c1", "c1", dependencies = setOf(PluginInfo.DependencyEntry("b1")), reference = Unit),
				PluginInfo.WithReference("c2", "c2", dependencies = setOf(PluginInfo.DependencyEntry("a2"), PluginInfo.DependencyEntry("b2")), reference = Unit)
		)

		val result = resolver.resolvePluginDependencies(firstPhaseInfos + secondPhaseInfos + thirdPhaseInfos, emptySet())

		assertTrue(result.unresolvableDueToMissingDependencies.isEmpty())
		assertTrue(result.unresolvableChains.isEmpty())
		assertEquals(3, result.loadOrder.size)
		assertEquals(firstPhaseInfos, result.loadOrder[0])
		assertEquals(secondPhaseInfos, result.loadOrder[1])
		assertEquals(thirdPhaseInfos, result.loadOrder[2])
	}

	@Test
	fun testMissingDependenciesResolve() {
		val info1 = PluginInfo.WithReference("a", "a", reference = Unit)
		val info2 = PluginInfo.WithReference("b", "b", dependencies = setOf(PluginInfo.DependencyEntry("c")), reference = Unit)

		val result = resolver.resolvePluginDependencies(setOf(info1, info2), emptySet())

		assertTrue(result.unresolvableChains.isEmpty())
		assertEquals(1, result.loadOrder.size)
		assertEquals(setOf(info1), result.loadOrder.single())
		assertEquals(setOf(PluginDependencyResolveResult.UnresolvableDueToMissingDependencies(info2, setOf(PluginInfo.DependencyEntry("c")))), result.unresolvableDueToMissingDependencies)
	}

	@Test
	fun testSelfChainingDependencyResolve() {
		val info = PluginInfo.WithReference("a", "a", dependencies = setOf(PluginInfo.DependencyEntry("a")), reference = Unit)

		val result = resolver.resolvePluginDependencies(setOf(info), emptySet())

		assertTrue(result.unresolvableDueToMissingDependencies.isEmpty())
		assertTrue(result.loadOrder.isEmpty())
		assertEquals(setOf(
				PluginDependencyResolveResult.UnresolvableChain(
						listOf((info to PluginInfo.DependencyEntry("a")))
				)
		), result.unresolvableChains)
	}

	@Test
	fun testChainingDependencyResolve() {
		val info1 = PluginInfo.WithReference("a", "a", dependencies = setOf(PluginInfo.DependencyEntry("b")), reference = Unit)
		val info2 = PluginInfo.WithReference("b", "b", dependencies = setOf(PluginInfo.DependencyEntry("a")), reference = Unit)

		val result = resolver.resolvePluginDependencies(setOf(info1, info2), emptySet())

		assertTrue(result.unresolvableDueToMissingDependencies.isEmpty())
		assertTrue(result.loadOrder.isEmpty())
		assertEquals(setOf(
				PluginDependencyResolveResult.UnresolvableChain(
						listOf((info1 to PluginInfo.DependencyEntry("a")), (info2 to PluginInfo.DependencyEntry("b")))
				)
		), result.unresolvableChains)
	}
}