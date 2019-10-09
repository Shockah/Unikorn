package pl.shockah.unikorn.plugin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VersionFilterTest {
	@Test
	fun exact132Version() {
		val filter = Plugin.Version.Filter("1.3.2")
		Assertions.assertEquals(true, filter.matches(Plugin.Version("1.3.2")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.3.4")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.3")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.3.2.1")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.4")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.2")))
	}

	@Test
	fun minimum132Version() {
		val filter = Plugin.Version.Filter("1.3.2+")
		Assertions.assertEquals(true, filter.matches(Plugin.Version("1.3.2")))
		Assertions.assertEquals(true, filter.matches(Plugin.Version("1.3.4")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.3")))
		Assertions.assertEquals(true, filter.matches(Plugin.Version("1.3.2.1")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.4")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.2")))
	}

	@Test
	fun any132Version() {
		val filter = Plugin.Version.Filter("1.3.2.*")
		Assertions.assertEquals(true, filter.matches(Plugin.Version("1.3.2")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.3.4")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.3")))
		Assertions.assertEquals(true, filter.matches(Plugin.Version("1.3.2.1")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.4")))
		Assertions.assertEquals(false, filter.matches(Plugin.Version("1.2")))
	}

	@Test
	fun anyVersion() {
		val filter = Plugin.Version.Filter("*")
		Assertions.assertEquals(true, filter.matches(Plugin.Version("1.0")))
		Assertions.assertEquals(true, filter.matches(Plugin.Version("2.0")))
		Assertions.assertEquals(true, filter.matches(Plugin.Version("0")))
	}
}