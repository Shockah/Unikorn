package pl.shockah.unikorn.plugin

open class Plugin(
		val manager: PluginManager,
		val info: PluginInfo
) {
	open fun onRequiredDependenciesLoaded() {
	}

	open fun onUnload() {
	}

	open fun onDependencyLoaded(plugin: Plugin) {
	}

	open fun onAllPluginsLoaded() {
	}

	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.PROPERTY)
	annotation class RequiredDependency

	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.PROPERTY)
	annotation class OptionalDependency(val value: String)

	data class Version(
			val parts: List<Int>
	) {
		companion object {
			operator fun invoke(toParse: String): Version {
				val parts = toParse.split(".").map { it.toInt() }
				if (parts.isEmpty())
					throw IllegalArgumentException()
				return Version(parts)
			}
		}

		override fun toString(): String {
			return parts.joinToString(".")
		}

		data class Filter(
				val parts: List<Part>
		) {
			companion object {
				operator fun invoke(toParse: String): Filter {
					val parts = toParse.split(".").map {
						when {
							it == "*" -> Part.Any
							it.endsWith("+") -> Part.MinNumber(it.removeSuffix("+").toInt())
							else -> Part.Number(it.toInt())
						}
					}
					if (parts.isEmpty())
						throw IllegalArgumentException()
					return Filter(parts)
				}
			}

			override fun toString(): String {
				return parts.joinToString(".")
			}

			fun matches(version: Version): Boolean {
				parts.forEachIndexed { i, part ->
					if (!part.matches(version.parts.getOrElse(i) { 0 }))
						return false
					if (part !is Part.Number)
						return true
				}
				return parts.size == version.parts.size
			}

			sealed class Part {
				abstract fun matches(value: Int): Boolean

				data class Number(
						val number: Int
				): Part() {
					override fun toString(): String {
						return "$number"
					}

					override fun matches(value: Int): Boolean {
						return value == number
					}
				}

				data class MinNumber(
						val number: Int
				): Part() {
					override fun toString(): String {
						return "$number+"
					}

					override fun matches(value: Int): Boolean {
						return value >= number
					}
				}

				object Any : Part() {
					override fun toString(): String {
						return "*"
					}

					override fun matches(value: Int): Boolean {
						return true
					}
				}
			}
		}
	}
}