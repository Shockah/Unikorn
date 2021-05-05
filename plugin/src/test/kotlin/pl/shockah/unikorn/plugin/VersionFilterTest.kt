package pl.shockah.unikorn.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VersionFilterTest {
	@Test
	fun exact132Version() {
		val filter = PluginVersion.Filter("1.3.2")
		assertEquals(true, filter.matches(PluginVersion("1.3.2")))
		assertEquals(false, filter.matches(PluginVersion("1.3.4")))
		assertEquals(false, filter.matches(PluginVersion("1.3")))
		assertEquals(false, filter.matches(PluginVersion("1.3.2.1")))
		assertEquals(false, filter.matches(PluginVersion("1.4")))
		assertEquals(false, filter.matches(PluginVersion("1.2")))
	}

	@Test
	fun minimum132Version() {
		val filter = PluginVersion.Filter("1.3.2+")
		assertEquals(true, filter.matches(PluginVersion("1.3.2")))
		assertEquals(true, filter.matches(PluginVersion("1.3.4")))
		assertEquals(false, filter.matches(PluginVersion("1.3")))
		assertEquals(true, filter.matches(PluginVersion("1.3.2.1")))
		assertEquals(false, filter.matches(PluginVersion("1.4")))
		assertEquals(false, filter.matches(PluginVersion("1.2")))
	}

	@Test
	fun any132Version() {
		val filter = PluginVersion.Filter("1.3.2.*")
		assertEquals(true, filter.matches(PluginVersion("1.3.2")))
		assertEquals(false, filter.matches(PluginVersion("1.3.4")))
		assertEquals(false, filter.matches(PluginVersion("1.3")))
		assertEquals(true, filter.matches(PluginVersion("1.3.2.1")))
		assertEquals(false, filter.matches(PluginVersion("1.4")))
		assertEquals(false, filter.matches(PluginVersion("1.2")))
	}

	@Test
	fun anyVersion() {
		val filter = PluginVersion.Filter("*")
		assertEquals(true, filter.matches(PluginVersion("1.0")))
		assertEquals(true, filter.matches(PluginVersion("2.0")))
		assertEquals(true, filter.matches(PluginVersion("0")))
	}
}