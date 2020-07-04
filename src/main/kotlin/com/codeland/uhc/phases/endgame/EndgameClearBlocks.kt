package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.phases.Phase
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.max

class EndgameClearBlocks : Phase() {

	override fun start(uhc: UHC, length: Long) {
		var w : World? = null
		for (world in Bukkit.getServer().worlds) {
			if (world.environment == World.Environment.NORMAL) {
				w = world
			}
		}
		runnable = object : BukkitRunnable() {
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
				for (player in Bukkit.getServer().onlinePlayers) {
					val preComp = TextComponent("block range is between ")
					val min = TextComponent(max(botBoundary, 0).toString())
					val mid = TextComponent(" and ")
					val max = TextComponent(topBoundary.toString())
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, preComp, min, mid, max)
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
		runnable?.runTaskTimer(GameRunner.plugin!!, 0, 1)
	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun getPhaseType(): UHCPhase {
		return UHCPhase.ENDGAME
	}

	override fun endPhrase(): String {
		return ""
	}

}