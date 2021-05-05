package pl.shockah.unikorn.plugin

interface PluginInfoProvider<Reference> {
	fun getPluginInfos(): Set<PluginInfo.WithReference<Reference>>
}