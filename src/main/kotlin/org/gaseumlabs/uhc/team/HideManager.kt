package org.gaseumlabs.uhc.team

import org.gaseumlabs.uhc.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object HideManager {
	private fun updatePlayer(updatePlayer: Player, hidePlayer: Player, show: Boolean) {
		if (show)
			updatePlayer.showPlayer(org.gaseumlabs.uhc.UHCPlugin.plugin, hidePlayer)
		else
			updatePlayer.hidePlayer(org.gaseumlabs.uhc.UHCPlugin.plugin, hidePlayer)
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
