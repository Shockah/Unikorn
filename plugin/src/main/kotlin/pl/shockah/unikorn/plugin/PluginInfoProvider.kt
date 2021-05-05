package pl.shockah.unikorn.plugin

interface PluginInfoProvider<PluginInfoType: PluginInfo> {
	fun getPluginInfos(): Set<PluginInfoType>
}