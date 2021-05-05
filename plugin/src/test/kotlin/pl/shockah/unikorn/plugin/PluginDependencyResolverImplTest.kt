package pl.shockah.unikorn.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pl.shockah.unikorn.plugin.impl.PluginDependencyResolverImpl

class PluginDependencyResolverImplTest {
	private data class MockPluginInfo(
			override val identifier: String,
			override val version: PluginVersion = PluginVersion("1.0"),
			override val dependencies: Set<PluginInfo.DependencyEntry> = emptySet()
	): PluginInfo

	private val resolver = PluginDependencyResolverImpl()

	@Test
	fun testNoDependenciesResolve() {
		val infosToLoad = setOf(
				MockPluginInfo("a"),
				MockPluginInfo("b"),
				MockPluginInfo("c")
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
				MockPluginInfo("a1"),
				MockPluginInfo("a2")
		)
		val secondPhaseInfos = setOf(
				MockPluginInfo("b1", dependencies = setOf(PluginInfo.DependencyEntry("a1"))),
				MockPluginInfo("b2", dependencies = setOf(PluginInfo.DependencyEntry("a1"), PluginInfo.DependencyEntry("a2")))
		)
		val thirdPhaseInfos = setOf(
				MockPluginInfo("c1", dependencies = setOf(PluginInfo.DependencyEntry("b1"))),
				MockPluginInfo("c2", dependencies = setOf(PluginInfo.DependencyEntry("a2"), PluginInfo.DependencyEntry("b2")))
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
		val info1 = MockPluginInfo("a")
		val info2 = MockPluginInfo("b", dependencies = setOf(PluginInfo.DependencyEntry("c")))

		val result = resolver.resolvePluginDependencies(setOf(info1, info2), emptySet())

		assertTrue(result.unresolvableChains.isEmpty())
		assertEquals(1, result.loadOrder.size)
		assertEquals(setOf(info1), result.loadOrder.single())
		assertEquals(setOf(PluginDependencyResolveResult.UnresolvableDueToMissingDependencies(info2, setOf(PluginInfo.DependencyEntry("c")))), result.unresolvableDueToMissingDependencies)
	}

	@Test
	fun testSelfChainingDependencyResolve() {
		val info = MockPluginInfo("a", dependencies = setOf(PluginInfo.DependencyEntry("a")))

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
		val info1 = MockPluginInfo("a", dependencies = setOf(PluginInfo.DependencyEntry("b")))
		val info2 = MockPluginInfo("b", dependencies = setOf(PluginInfo.DependencyEntry("a")))

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