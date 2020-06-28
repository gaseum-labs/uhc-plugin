package com.codeland.uhc.core

import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

object GameRunner {
	private var uhc: UHC? = null
	private var world : World? = null

	var phase = UHCPhase.WAITING

	fun setUhc(uhc: UHC?) {
		this.uhc = uhc
	}

	fun startGame(commandSender : CommandSender, w : World) {
		if (uhc == null) {
			commandSender.sendMessage("You must setup the game first")
		} else if (phase != UHCPhase.WAITING) {
			commandSender.sendMessage("UHC already in progress")
		} else {
			uhc!!.start(commandSender, w)
			world = w
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
		var aliveTeam : Team? = null
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
				Bukkit.getServer().onlinePlayers.forEach {
					val teamComp = TextComponent(team.displayName)
					teamComp.color = ChatColor.valueOf(team.color.toString())
					teamComp.isBold = true
					val elimComp = TextComponent(" has been ELIMINATED!")
					it.sendMessage(teamComp, elimComp)
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
	}

	fun endUHC(winner: Team) {
		Bukkit.getServer().onlinePlayers.forEach {
			val winningTeamComp = TextComponent(winner.displayName)
			winningTeamComp.isBold = true
			winningTeamComp.color = ChatColor.valueOf(winner.color.toString())
			val congratsComp = TextComponent("HAS WON!")
			it.sendTitle(Title(winningTeamComp, congratsComp, 0, 10, 2))
		}
	}
}