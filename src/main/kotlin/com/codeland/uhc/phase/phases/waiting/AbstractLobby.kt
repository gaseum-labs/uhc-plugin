package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

object AbstractLobby {
	fun lobbyLocation(uhc: UHC, player: Player): Location {
		return Location(Bukkit.getWorlds()[0], uhc.lobbyX + 0.5, Util.topBlockYTop(Bukkit.getWorlds()[0], 254, uhc.lobbyX, uhc.lobbyZ) + 1.0, uhc.lobbyZ + 0.5)
	}

	fun onSpawnLobby(player: Player): Location {
		player.exp = 0.0F
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.foodLevel = 20
		player.fallDistance = 0f
		player.gameMode = GameMode.CREATIVE

		/* get them on the health scoreboard */
		player.damage(0.05)

		Pests.makeNotPest(player)

		val location = lobbyLocation(GameRunner.uhc, player)
		player.teleport(location)

		CommandItemType.giveItem(CommandItemType.GUI_OPENER, player.inventory)
		CommandItemType.giveItem(CommandItemType.JOIN_PVP, player.inventory)
		CommandItemType.giveItem(CommandItemType.PARKOUR_CHECKPOINT, player.inventory)
		CommandItemType.giveItem(CommandItemType.SPECTATE, player.inventory)

		return location
	}

	fun lobbyTipsTick(subTick: Int) {
		if (subTick % 20 == 0) {
			val numSlides = 3
			val perSlide = 6

			fun slideN(n: Int) = (subTick / 20) % (numSlides * perSlide) < perSlide * (n + 1)
			fun isFirst() = (subTick / 20) % perSlide == 0

			fun tip(player: Player, playerData: PlayerData) {
				if (isFirst()) playerData.loadingTip = (Math.random() * WaitingDefault.loadingTips.size).toInt()

				player.sendActionBar("${ChatColor.GOLD}UHC Tips: ${ChatColor.WHITE}${ChatColor.BOLD}${WaitingDefault.loadingTips[playerData.loadingTip]}")
			}

			Bukkit.getOnlinePlayers().forEach { player ->
				val playerData = PlayerData.getPlayerData(player.uniqueId)
				val team = TeamData.playersTeam(player.uniqueId)

				if (!playerData.participating && !playerData.lobbyPVP.inPvp) {
					if (player.gameMode == GameMode.SPECTATOR) {
						if (slideN(0)) {
							player.sendActionBar("${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}/uhc lobby ${ChatColor.GOLD}to return to lobby")
						} else {
							player.sendActionBar("")
						}
					} else {
						when {
							slideN(0) -> {
								if (GameRunner.uhc.usingBot) {
									val linked = GameRunner.bot?.isLinked(player.uniqueId)

									if (linked == null || linked) tip(player, playerData)
									else player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}You are not linked! ${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}\"%link [your minecraft username]\" ${ChatColor.GOLD}in discord")
								} else tip(player, playerData)
							}
							slideN(1) -> {
								tip(player, playerData)
							}
							slideN(2) -> {
								if (team == null) tip(player, playerData)
								else player.sendActionBar("${ChatColor.GOLD}Team name: ${team.colorPair.colorStringModified(team.displayName, ChatColor.BOLD)} ${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}/uhc name [name] ${ChatColor.GOLD}to set your team's name")
							}
						}
					}
				}
			}
		}
	}
}
