package pl.shockah.unikorn.plugin.impl

import pl.shockah.unikorn.plugin.PluginLoader
import pl.shockah.unikorn.plugin.PluginLoaderFactory
import java.net.URLClassLoader

class FilePluginLoaderFactory: PluginLoaderFactory<FilePluginInfo> {
	override fun createPluginLoader(pluginInfos: Set<FilePluginInfo>): PluginLoader<FilePluginInfo> {
		return ClassLoaderPluginLoader(URLClassLoader(pluginInfos.map { it.jarFile.toURI().toURL() }.toTypedArray())) { it.pluginClassName }
	}
}