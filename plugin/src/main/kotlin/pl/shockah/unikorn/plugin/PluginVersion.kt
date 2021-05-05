package pl.shockah.unikorn.plugin

data class PluginVersion(
		val parts: List<Int>
) {
	companion object {
		operator fun invoke(toParse: String): PluginVersion {
			val parts = toParse.split(".").map { it.toInt() }
			if (parts.isEmpty())
				throw IllegalArgumentException()
			return PluginVersion(parts)
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

		fun matches(version: PluginVersion): Boolean {
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

			object Any: Part() {
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