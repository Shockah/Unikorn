package pl.shockah.unikorn.plugin.impl

import pl.shockah.unikorn.plugin.ClassPluginLoader
import pl.shockah.unikorn.plugin.Plugin
import pl.shockah.unikorn.plugin.PluginLoader
import pl.shockah.unikorn.plugin.PluginLoaderFactory
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class FilePluginLoaderFactory: PluginLoaderFactory<FilePluginInfo> {
	override fun createPluginLoader(pluginInfos: Set<FilePluginInfo>): PluginLoader<FilePluginInfo> {
		return FilePluginLoader(URLClassLoader(pluginInfos.map { it.jarFile.toURI().toURL() }.toTypedArray()))
	}
}

class FilePluginLoader internal constructor (
		private val classLoader: ClassLoader
): ClassPluginLoader<FilePluginInfo>() {
	override fun loadPluginClass(info: FilePluginInfo): KClass<out Plugin> {
		val klazz = classLoader.loadClass(info.pluginClassName).kotlin
		if (!klazz.isSubclassOf(Plugin::class))
			throw IllegalArgumentException("${info.pluginClassName} is not a Plugin.")

		@Suppress("UNCHECKED_CAST")
		return klazz as KClass<out Plugin>
	}
}