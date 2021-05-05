package pl.shockah.unikorn.plugin.impl

import pl.shockah.unikorn.plugin.*
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class FilePluginLoaderFactory: PluginLoaderFactory<File> {
	override fun createPluginLoader(references: Set<File>): PluginLoader<File> {
		return FilePluginLoader(URLClassLoader(references.map { it.toURI().toURL() }.toTypedArray()))
	}
}

class FilePluginLoader internal constructor (
		private val classLoader: ClassLoader
): ClassPluginLoader<File>() {
	override fun loadPluginClass(info: PluginInfo.WithReference<File>): KClass<out Plugin> {
		val klazz = classLoader.loadClass(info.pluginClassName).kotlin
		if (!klazz.isSubclassOf(Plugin::class))
			throw IllegalArgumentException("${info.pluginClassName} is not a Plugin.")

		@Suppress("UNCHECKED_CAST")
		return klazz as KClass<out Plugin>
	}
}