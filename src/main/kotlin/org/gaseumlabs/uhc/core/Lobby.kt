package org.gaseumlabs.uhc.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.BOLD
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.gui.CommandItemType
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.WorldStorage
import org.gaseumlabs.uhc.world.WorldManager

object Lobby {
	fun resetPlayerStats(player: Player) {
		player.exp = 0.0f
		player.totalExperience = 0
		player.level = 0
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.absorptionAmount = 0.0
		player.health = 20.0
		player.foodLevel = 20
		player.saturation = 5.0f
		player.fallDistance = 0f
		player.fireTicks = -1
		player.inventory.clear()
		player.setItemOnCursor(null)
		player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
		player.fallDistance = 0f
		player.setStatistic(Statistic.TIME_SINCE_REST, 0)
		player.isFlying = false
		player.isSneaking = false
		player.enderChest.clear()
	}

	fun onSpawnLobby(player: Player) {
		resetPlayerStats(player)

		player.gameMode = GameMode.ADVENTURE

		val playerData = PlayerData.get(player.uniqueId)

		playerData.participating = false
		playerData.alive = false

		CommandItemType.GUI_OPENER.giveItem(player.inventory)
		CommandItemType.PVP_OPENER.giveItem(player.inventory)
		CommandItemType.SPECTATE.giveItem(player.inventory)

		player.teleport(WorldManager.lobbyWorld.spawnLocation)
	}

	fun lobbyTipsTick(subTick: Int) {
		if (subTick % 20 == 0) {
			fun slideN(slide: Int, num: Int, time: Int) = (subTick / 20) % (num * time) < time * (slide + 1)

			Bukkit.getOnlinePlayers().forEach { player ->
				val playerData = PlayerData.get(player.uniqueId)
				val game = ArenaManager.playersArena(player.uniqueId)

				if (!playerData.participating && game == null) {
					val team = UHC.preGameTeams.playersTeam(player.uniqueId)
					val queueTime = PvpQueue.queueTime(player.uniqueId)

					if (queueTime != null) {
						val queueType = playerData.inLobbyPvpQueue

						player.sendActionBar(Util.gradientString(
							"${PvpQueue.queueName(queueType)} | " +
							"Queue Time: ${Util.timeString(queueTime)} | " +
							"Players in Queue: ${PvpQueue.size(queueType)}",
							TextColor.color(0x750c0c), TextColor.color(0xeb1f0c)
						))

					} else if (player.gameMode == GameMode.SPECTATOR) {
						if (slideN(0, 3, 6)) {
							player.sendActionBar(
								Component.text("Use ", GOLD)
									.append(Component.text("/uhc lobby ", Style.style(BOLD)))
									.append(Component.text("to return to lobby", GOLD))
							)

						} else {
							player.sendActionBar(Component.empty())
						}
					} else if (UHC.dataManager.linkData.isUnlinked(player.uniqueId)) {
						player.sendActionBar(
							Component.text("You are not linked! ", RED, BOLD)
								.append(Component.text("Type ", GOLD))
								.append(Component.text("/link", Style.style(BOLD)))
								.append(Component.text(" to link", GOLD))
						)

					} else if (team != null && team.name == null) {
						val warningColor = TextColor.color(if (slideN(0, 2, 1)) 0xFF0000 else 0xFFFFFF)

						player.sendActionBar(
							Component.text("Your team does not have a name! ", RED, BOLD)
								.append(Component.text("\"/teamName [name]\" ", warningColor, BOLD))
								.append(Component.text("to set your team's name", GOLD))
						)

					}
				}
			}
		}
	}
}