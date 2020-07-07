package com.codeland.uhc.core

import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.UHCPlugin
import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

object GameRunner {
	var uhc = UHC(600.0, 25.0, 1125, 2250, 600, 0)

	var plugin : UHCPlugin? = null

	var phase = UHCPhase.WAITING

	var abundance = true;
	var unsheltered = true;

	fun startGame(commandSender : CommandSender) {
		if (phase != UHCPhase.WAITING) {
			commandSender.sendMessage("UHC already in progress")
		} else {
			uhc.start(commandSender)
			phase = UHCPhase.GRACE
		}
	}

	fun remainingTeams() : Int {
		var ret = 0
		for (team in Bukkit.getServer().scoreboardManager.mainScoreboard.teams) {
			for (entry in team.entries) {
				val player = Bukkit.getServer().getPlayer(entry)
				if (player != null && player.gameMode == GameMode.SURVIVAL) {
					++ret
					break
				}
			}
		}
		return ret
	}

	fun playerDeath(deadPlayer: Player) {
		var aliveTeam: Team? = null

		var closestDistSqrd = 2500.0
		var closestPlayer: Player? = null
		val deadPlayerTeam = playersTeam(deadPlayer.displayName)
		for (oPlayer in Bukkit.getServer().onlinePlayers) {
			if (playersTeam(oPlayer.displayName)?.equals(deadPlayerTeam) == false) {
				val dist = oPlayer.location.distanceSquared(deadPlayer.location)
				if (dist < closestDistSqrd) {
					closestDistSqrd = dist
					closestPlayer = oPlayer
				}
			}
		}

		for (team in Bukkit.getServer().scoreboardManager.mainScoreboard.teams) {
			var isThisTeam = false
			var isAlive = false
			for (entry in team.entries) {
				val player = Bukkit.getServer().getPlayer(entry)
				if (deadPlayer.equals(player)) {
					isThisTeam = true
				} else if (player != null && player.gameMode == GameMode.SURVIVAL) {
					isAlive = true
				}
			}
			if (isThisTeam && !isAlive) {
				val teamComp = TextComponent(team.displayName)
				teamComp.color = team.color.asBungee()
				teamComp.isBold = true
				val elimComp = TextComponent(" has been ELIMINATED!")
				elimComp.color = ChatColor.GOLD
				val remainingComp = TextComponent("" + remainingTeams() + " teams remain")
				remainingComp.color = ChatColor.GOLD
				Bukkit.getServer().onlinePlayers.forEach {
					it.sendMessage(teamComp, elimComp)
					it.sendMessage(remainingComp)
				}
			}
			if (isAlive) {
				aliveTeam = team;
			}
		}

		if (remainingTeams() == 1) {
			if (aliveTeam != null) {
				endUHC(aliveTeam)
			}
		}

		if (closestPlayer != null) {
			uhc.killReward.applyReward(playersTeam(closestPlayer.displayName)!!)
		}
	}

	fun playersTeam(player: String) : Team? {
		for (team in Bukkit.getServer().scoreboardManager.mainScoreboard.teams) {
			for (entry in team.entries) {
				if (entry == player) {
					return team
				}
			}
		}
		return null
	}

	fun endUHC(winner: Team) {
		Bukkit.getServer().onlinePlayers.forEach {
			val winningTeamComp = TextComponent(winner.displayName)
			winningTeamComp.isBold = true
			winningTeamComp.color = winner.color.asBungee()
			val congratsComp = TextComponent("HAS WON!")
			it.sendTitle(Title(winningTeamComp, congratsComp, 0, 200, 40))
			phase = UHCPhase.POSTGAME
		}
		uhc.currentPhase?.interrupt()
	}

	fun sendPlayer(player: Player, message: String) {
		val comp = TextComponent(message)
		comp.color = ChatColor.GOLD
		comp.isBold = true
		player.sendMessage(comp)
	}

	fun netherIsAllowed() : Boolean {
		return !(uhc.netherToZero && (phase == UHCPhase.FINAL || phase == UHCPhase.GLOWING || phase == UHCPhase.ENDGAME))
	}
}