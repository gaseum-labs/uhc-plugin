package com.codeland.uhc.command

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.math.ceil

object PreGenner {
	var taskID = -1

	fun pregen(player: Player) {
		val world = GameRunner.uhc.getDefaultWorld()

		val extrema = ceil(GameRunner.uhc.startRadius / 16.0).toInt()
		val sideLength = (extrema * 2 + 1)

		val max = sideLength * sideLength
		val tenPercent = max / 10
		var along = 0

		GameRunner.sendGameMessage(player, "Beginning pregen...")

		/* load a new chunk every tick */
		taskID = SchedulerUtil.everyTick {
			var x = (along % sideLength) - extrema
			var z = ((along / sideLength) % sideLength) - extrema

			/* load */
			world.getChunkAt(x, z)

			if (++along == max) {
				Bukkit.getScheduler().cancelTask(taskID)
				GameRunner.sendGameMessage(player, "Pregen completed")

			} else if (along % tenPercent == 0) {
				GameRunner.sendGameMessage(player, "Pregen ${(along / tenPercent) * 10}% complete")
			}
		}
	}
}
