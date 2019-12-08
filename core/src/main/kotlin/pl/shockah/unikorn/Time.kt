package pl.shockah.unikorn

import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import kotlin.concurrent.thread

fun TimeUnit.of(duration: Long): Time {
	return Time(duration, this)
}

fun Lock.tryLock(time: Time): Boolean {
	return tryLock(time.duration, time.unit)
}

fun ScheduledExecutorService.schedule(time: Time, runnable: Runnable) {
	schedule(runnable, time.duration, time.unit)
}

fun ScheduledExecutorService.schedule(time: Time, closure: () -> Unit) {
	schedule({ closure() }, time.duration, time.unit)
}

operator fun Date.plus(time: Time): Date {
	return Date(this.time + time.milliseconds)
}

operator fun Date.minus(other: Date): Time {
	return Time(time - other.time, TimeUnit.MILLISECONDS)
}

data class Time(
		val duration: Long,
		val unit: TimeUnit
): Comparable<Time> {
	companion object {
		val NONE: Time = Time(0, TimeUnit.SECONDS)
	}

	val nanoseconds: Long
		get() = TimeUnit.NANOSECONDS.convert(duration, unit)

	val microseconds: Long
		get() = TimeUnit.MICROSECONDS.convert(duration, unit)

	val milliseconds: Long
		get() = TimeUnit.MILLISECONDS.convert(duration, unit)

	val seconds: Long
		get() = TimeUnit.SECONDS.convert(duration, unit)

	val minutes: Long
		get() = TimeUnit.MINUTES.convert(duration, unit)

	val hours: Long
		get() = TimeUnit.HOURS.convert(duration, unit)

	val days: Long
		get() = TimeUnit.DAYS.convert(duration, unit)

	override fun toString(): String {
		return "$duration ${unit.name}"
	}

	override fun compareTo(other: Time): Int {
		return when {
			unit == other.unit -> duration.compareTo(other.duration)
			other.unit.ordinal > unit.ordinal -> convert(other.unit).compareTo(other)
			unit.ordinal > other.unit.ordinal -> compareTo(other.convert(unit))
			else -> 0
		}
	}

	operator fun plus(time: Time): Time {
		val minUnit = listOf(this.unit, time.unit).minBy { it.ordinal }!!
		return minUnit.of(convert(minUnit).duration + time.convert(minUnit).duration)
	}

	operator fun minus(time: Time): Time {
		val minUnit = listOf(this.unit, time.unit).minBy { it.ordinal }!!
		return minUnit.of(convert(minUnit).duration - time.convert(minUnit).duration)
	}

	operator fun times(scalar: Int): Time {
		return unit.of(duration * scalar)
	}

	operator fun div(scalar: Int): Time {
		return unit.of(duration / scalar)
	}

	fun convert(unit: TimeUnit): Time {
		return unit.of(unit.convert(duration, this.unit))
	}

	fun sleep() {
		if (duration > 0)
			unit.sleep(duration)
	}

	fun runDelayed(isDaemon: Boolean = false, alwaysNewThread: Boolean = false, closure: () -> Unit) {
		if (!alwaysNewThread && duration <= 0) {
			closure()
		} else {
			thread(isDaemon = isDaemon) {
				sleep()
				closure()
			}
		}
	}
}