package org.gaseumlabs.uhc.event

import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.event.world.WorldSaveEvent
import org.gaseumlabs.uhc.core.Lobby
import org.gaseumlabs.uhc.core.OfflineZombie
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.discord.storage.DiscordStorage
import org.gaseumlabs.uhc.gui.CommandItemType
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.team.HideManager
import org.gaseumlabs.uhc.team.NameManager
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldManager

class LobbyEvents : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(org.gaseumlabs.uhc.UHCPlugin.plugin) {
			val player = event.player
			val playerData = PlayerData.get(player.uniqueId)

			NameManager.onPlayerLogin(event.player, UHC.getTeams().playersTeam(player.uniqueId))

			/* lobby spawn */
			if (!playerData.participating) {
				Lobby.onSpawnLobby(event.player)
			}

			/* update who the player sees */
			HideManager.updateAllForPlayer(player)
			/* update who sees the player */
			HideManager.updatePlayerForAll(player)
		}
	}

	@EventHandler
	fun onLogOut(event: PlayerQuitEvent) {
		val player = event.player
		val playerData = PlayerData.get(player.uniqueId)
		val pvpGame = ArenaManager.playersArena(player.uniqueId)

		if (pvpGame != null) {
			ArenaManager.removePlayer(player.uniqueId)
		} else if (playerData.participating && player.gameMode != GameMode.SPECTATOR) {
			OfflineZombie.createZombie(player, playerData)
		}
	}

	@EventHandler
	fun onSave(event: WorldSaveEvent) {
		if (event.world === WorldManager.pvpWorld) {
			ArenaManager.saveWorldInfo(event.world)
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		val stack = event.item ?: return
		val player = event.player

		if (
			event.action === Action.RIGHT_CLICK_AIR ||
			event.action === Action.RIGHT_CLICK_BLOCK
		) {
			CommandItemType.values().find { it.isItem(stack) }?.execute(player)
		}
	}

	@EventHandler
	fun onWorld(event: PlayerChangedWorldEvent) {
		/* hide this player for everyone else not in the player's new world */
		HideManager.updatePlayerForAll(event.player)

		/* hide other players not in the player's new world to the player */
		HideManager.updateAllForPlayer(event.player)
	}

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val block = event.clickedBlock ?: return
		if (
			event.action === Action.RIGHT_CLICK_BLOCK &&
			block.type === Material.RESPAWN_ANCHOR &&
			block.world === WorldManager.lobbyWorld
		) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun serverListPing(event: ServerListPingEvent) {
		/* do not attempt to modify MOTD if discordstorage is not set */
		val splashText = DiscordStorage.splashText ?: return

		val color0 = TextColor.color(DiscordStorage.color0 ?: return)
		val color1 = TextColor.color(DiscordStorage.color1 ?: return)

		val length = 48
		val strip =
			String(CharArray(length + 1) { i -> if (i == length) '\n' else splashText[i % splashText.length] })

		event.motd(
			Util.gradientString(strip, color0, color1).append(Util.gradientString(strip, color0, color1))
		)
	}
}