package com.codeland.uhc.team

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.WorldManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

object HideManager {
	private fun updatePlayer(updatePlayer: Player, hidePlayer: Player, show: Boolean) {
		if (show)
			updatePlayer.showPlayer(UHCPlugin.plugin, hidePlayer)
		else
			updatePlayer.hidePlayer(UHCPlugin.plugin, hidePlayer)
	}

	private fun inLobby(player: Player): Boolean {
		return WorldManager.isNonGameWorld(player.world)
	}

	fun updatePlayerForAll(hidePlayer: Player) {
		val hidePlayerInLobby = inLobby(hidePlayer)

		Bukkit.getOnlinePlayers().forEach { updatePlayer ->
			if (updatePlayer !== hidePlayer) {
				updatePlayer(updatePlayer, hidePlayer, inLobby(updatePlayer) == hidePlayerInLobby)
			}
		}
	}

	fun updateAllForPlayer(updatePlayer: Player) {
		val updatePlayerInLobby = inLobby(updatePlayer)

		Bukkit.getOnlinePlayers().forEach { hidePlayer ->
			if (updatePlayer !== hidePlayer) {
				updatePlayer(updatePlayer, hidePlayer, inLobby(hidePlayer) == updatePlayerInLobby)
			}
		}
	}
}
