package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.boss.BossBar

class EndgameNone : Phase() {
	override fun customStart() {
		closeNether()
	}

	override fun customEnd() {}
	override fun onTick(currentTick: Int) {}
	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		barStatic(bossBar)
	}

	override fun endPhrase(): String {
		return ""
	}

	companion object {
		fun closeNether() {
			SchedulerUtil.nextTick {
				Bukkit.getOnlinePlayers().forEach { player ->
					if (player.world.environment != GameRunner.uhc.defaultEnvironment) {
						Commands.errorMessage(player, "Failed to return to home dimension!")
						player.damage(100000000000.0)
					}
				}

				GameRunner.uhc.playerDataList.forEach { (uuid, playerData) ->
					playerData.offlineZombie?.damage(100000000000.0)
				}
			}
		}
	}
}
