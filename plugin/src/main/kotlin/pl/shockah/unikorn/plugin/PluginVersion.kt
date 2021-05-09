package pl.shockah.unikorn.plugin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(PluginVersion.Serializer::class)
data class PluginVersion(
		val parts: List<Int>
) {
	object Serializer: KSerializer<PluginVersion> {
		override val descriptor = String.serializer().descriptor

		override fun serialize(encoder: Encoder, value: PluginVersion) {
			encoder.encodeString(value.toString())
		}

		override fun deserialize(decoder: Decoder): PluginVersion {
			return PluginVersion(decoder.decodeString())
		}
	}

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

	@Serializable(Filter.Serializer::class)
	data class Filter(
			val parts: List<Part>
	) {
		object Serializer: KSerializer<Filter> {
			override val descriptor = String.serializer().descriptor

			override fun serialize(encoder: Encoder, value: Filter) {
				encoder.encodeString(value.toString())
			}

			override fun deserialize(decoder: Decoder): Filter {
				return Filter(decoder.decodeString())
			}
		}

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