package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.AbstractLobby
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.team.TeamData
import org.bukkit.*
import org.bukkit.entity.Player

class WaitingDefault : Phase() {
	override fun customStart() {
		/* set gamerules for all worlds */
		Bukkit.getWorlds().forEach { world -> AbstractLobby.prepareWorld(world, uhc) }

		TeamData.destroyTeam(null, uhc.usingBot, true) {}

		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.inventory.clear()
			onPlayerJoin(player)
		}
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return 1.0
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {}

	override fun endPhrase() = "Game starts in"

	fun onPlayerJoin(player: Player) {
		AbstractLobby.onSpawnLobby(player)
	}
}
