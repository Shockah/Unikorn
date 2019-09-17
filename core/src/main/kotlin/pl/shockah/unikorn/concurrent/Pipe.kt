package pl.shockah.unikorn.concurrent

import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class Pipe(
		capacity: Int = 8192
) {
	val output: OutputStream = PipedOutputStream()
	val input: InputStream = PipedInputStream(output as PipedOutputStream, capacity)
}