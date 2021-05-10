package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.team.TeamData
import org.bukkit.*
import org.bukkit.entity.Player

class WaitingDefault : Phase() {
	override fun customStart() {
		PlayerData.prune()

		TeamData.destroyTeam(null, UHC.usingBot.get(), true) {}

		Bukkit.getServer().onlinePlayers.forEach { player -> Lobby.onSpawnLobby(player) }
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Float {
		return 1.0f
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {}

	override fun endPhrase() = "Game starts in"
}
