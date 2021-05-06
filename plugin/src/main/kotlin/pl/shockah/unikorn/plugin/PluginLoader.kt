package pl.shockah.unikorn.plugin

interface PluginLoaderFactory<PluginInfoType: PluginInfo> {
	fun createPluginLoader(pluginInfos: Collection<PluginInfoType>): PluginLoader<PluginInfoType>
}

interface PluginLoader<PluginInfoType: PluginInfo> {
	fun loadPlugin(info: PluginInfoType, parameterHandlers: List<PluginConstructorParameterHandler>): Plugin
}