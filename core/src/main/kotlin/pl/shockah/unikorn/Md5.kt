package pl.shockah.unikorn

import java.nio.charset.Charset
import java.security.MessageDigest

data class Md5(
		val bytes: ByteArray
) {
	init {
		require(bytes.size == 16)
	}

	companion object {
		private val md5Digest = MessageDigest.getInstance("MD5")

		fun of(data: ByteArray): Md5 {
			return Md5(md5Digest.digest(data))
		}

		fun of(data: String, charset: Charset = Charsets.UTF_8): Md5 {
			return of(data.toByteArray(charset))
		}
	}

	override fun equals(other: Any?): Boolean {
		return other is Md5 && bytes.contentEquals(other.bytes)
	}

	override fun hashCode(): Int {
		return bytes.contentHashCode()
	}
}