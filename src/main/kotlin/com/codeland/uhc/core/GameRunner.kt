package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.destroystokyo.paper.Title
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import java.util.logging.Level

object GameRunner {
	private var uhc: UHC? = null
	private var world : World? = null
	var plugin : UHCPlugin? = null

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
		val entries = closestPlayer?.displayName?.let { playersTeam(it)?.entries }
		if (entries != null) {
			for (entry in entries) {
				Bukkit.getServer().getPlayer(entry)?.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300 * 5, 0))
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

	fun getHighestHPTeam() : Team? {
		var ret : Team? = null
		var maxHP = 0.0
		for (team in Bukkit.getServer().scoreboardManager.mainScoreboard.teams) {
			var thisTeamsHP = 0.0
			for (entry in team.entries) {
				val player = Bukkit.getServer().getPlayer(entry)
				if (player != null) {
					if (player.gameMode == GameMode.SURVIVAL) {
						thisTeamsHP += player.health
					}
				}
			}
			if (thisTeamsHP > maxHP) {
				ret = team
				maxHP = thisTeamsHP
			}
		}
		return ret
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
	}

	fun setGlowingMode(mode: Int) {
		if (uhc != null) {
			uhc!!.glowType = mode
		}
	}
}