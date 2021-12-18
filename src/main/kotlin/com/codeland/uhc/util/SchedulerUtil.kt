package com.codeland.uhc.util

import com.codeland.uhc.UHCPlugin
import org.bukkit.Bukkit

object SchedulerUtil {
	val plugin = UHCPlugin.plugin

	fun nextTick(runnable: () -> Unit) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable)
	}

	fun everyTick(runnable: () -> Unit): Int {
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 1)
	}

	fun everyN(n: Long, runnable: () -> Unit): Int {
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, n)
	}

	fun later(ticks: Long, runnable: () -> Unit): Int {
		return Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, ticks)
	}

	fun delayedFor(ticks: Long, range: Iterable<Int>, runnable: (Int) -> Unit) {
		if (!range.none()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
				runnable(range.first())
				delayedFor(ticks, range.drop(1), runnable)
			}, ticks)
		}
	}
}