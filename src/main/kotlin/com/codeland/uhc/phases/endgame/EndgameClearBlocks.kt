package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.max

class EndgameClearBlocks : Phase() {

	var blockRunnable = null as BukkitRunnable?

	override fun customStart() {
		var w : World? = null

		for (world in Bukkit.getServer().worlds) {
			if (world.environment == World.Environment.NORMAL) {
				w = world
			}
		}

		blockRunnable = object : BukkitRunnable() {
			var topBoundary = 255
			var botBoundary = -135
			var frame = 0
			override fun run() {
				val extrema = w!!.worldBorder.size.toInt() / 2 + 1
				for (y in 0..255) {
					if (y < botBoundary || y > topBoundary) {
						val min = extrema * 2 * frame / 20
						val max = extrema * 2 * (frame + 1) / 20 + 1
						for (x in ((min - extrema)..(max - extrema))) {
							for (z in (-extrema..extrema)) {
								val block = w.getBlockAt(x, y, z)
								if (block.type != Material.AIR) {
									block.type = Material.AIR
								}
							}
						}
					}
				}

				Bukkit.getServer().onlinePlayers.forEach { player ->
					player.sendActionBar("Block range is between ${ChatColor.GOLD}${ChatColor.BOLD}${max(botBoundary, 0)} ${ChatColor.RESET}and ${ChatColor.GOLD}${ChatColor.BOLD}$topBoundary")
				}

				if (frame == 0) {
					--topBoundary
					++botBoundary
				}

				frame = (frame + 1) % 20
				if (botBoundary > 60 || topBoundary < 60) {
					botBoundary = 60
					topBoundary = 60
				}
			}
		}

		blockRunnable?.runTaskTimer(GameRunner.plugin!!, 0, 1)
	}

	override fun perSecond(remainingSeconds: Long) {
		TODO("Not yet implemented")
	}

	override fun onEnd() {
		super.onEnd()

		blockRunnable?.cancel()
	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun endPhrase(): String {
		return ""
	}
}
