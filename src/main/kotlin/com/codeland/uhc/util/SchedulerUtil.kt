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
}