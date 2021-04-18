package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.World

class EndgameNone : Phase() {
	override fun customStart() {
		closeNether()
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return 1.0
	}

	override fun perTick(currentTick: Int) {}
	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun endPhrase(): String {
		return ""
	}

	companion object {
		fun closeNether() {
			SchedulerUtil.nextTick {
				val defaultWorld = UHC.getDefaultWorld()

				PlayerData.playerDataList.forEach { (uuid, playerData) ->
					val location = GameRunner.getPlayerLocation(uuid)

					if (location != null && location.world !== defaultWorld) {
						GameRunner.playerAction(uuid) { player -> Commands.errorMessage(player, "Failed to return to home dimension!") }
						GameRunner.damagePlayer(uuid, 100000000000.0)
					}
				}
			}
		}
	}
}
