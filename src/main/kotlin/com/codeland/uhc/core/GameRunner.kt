package com.codeland.uhc.core

import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.event.Pests
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.quirk.Quirk
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

	var halfZatoichi = Quirk("Half Zatoichi")
	var abundance = Quirk("Abundance")
	var unsheltered = Quirk("Unsheletered")
	var pests = Quirk("Pests")

	private val _SET_INCOMPATIBILITIES = {
		halfZatoichi.setIncompatible(pests)
	}()

	var gui = Gui()

	fun startGame(commandSender : CommandSender) {
		if (phase != UHCPhase.WAITING) {
			commandSender.sendMessage("UHC already in progress")
		} else {
			uhc.start(commandSender)
			phase = UHCPhase.GRACE
		}
	}

	fun teamIsAlive(team: Team): Boolean {
		return team.entries.any { entry ->
			val player = Bukkit.getServer().getPlayer(entry)

			when {
				player == null -> false
				pests.enabled && Pests.isPest(player) -> false
				player.gameMode == GameMode.SURVIVAL -> true
				else -> false
			}
		}
	}

	/**
	 * returns both the number of remaining teams
	 * and the last remaining team if there is exactly 1
	 */
	fun remainingTeams(focus: Team) : Triple<Int, Team?, Boolean> {
		var retRemaining = 0
		var retAlive = null as Team?
		var retFocus = false

		Bukkit.getServer().scoreboardManager.mainScoreboard.teams.forEach { team ->
			val alive = teamIsAlive(team)

			if (team == focus) retFocus = alive;

			if (alive) {
				++retRemaining
				retAlive = team
			}
		}

		/* only give last alive if only one team is alive */
		return Triple(retRemaining, if (retRemaining == 1) retAlive else null, retFocus)
	}

	fun quickRemainingTeams() : Int {
		var retRemaining = 0

		Bukkit.getServer().scoreboardManager.mainScoreboard.teams.forEach { team ->
			if (teamIsAlive(team))
				++retRemaining
		}

		/* only give last alive if only one team is alive */
		return retRemaining;
	}

	fun playerDeath(deadPlayer: Player) {
		var aliveTeam: Team? = null

		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		/* pest mode keeps everyone in survival */
		if (!pests.enabled)
			deadPlayer.gameMode = GameMode.SPECTATOR

		var deadPlayerTeam = playersTeam(deadPlayer.name)
			?: return

		var (remainingTeams, lastRemaining, teamIsAlive) = remainingTeams(deadPlayerTeam)

		/* broadcast elimination message */
		if (!teamIsAlive) {
			val message = TextComponent("${deadPlayerTeam.displayName} has been Eliminated!")
			message.color = ChatColor.GOLD
			message.isBold = true

			val message2 = TextComponent("$remainingTeams teams remain")
			message2.color = ChatColor.GOLD
			message2.isBold = true

			Bukkit.getServer().onlinePlayers.forEach { player ->
				player.sendMessage(message)
				player.sendMessage(message2)
			}
		}

		/* uhc ending point (stops kill reward) */
		if (lastRemaining != null)
			return endUHC(lastRemaining)

		/* kill reward awarding */
		val killer = deadPlayer.killer
		if (killer != null) {
			val killerTeam = playersTeam(killer.name)
				?: return

			uhc.killReward.applyReward(killerTeam)
		}
	}

	fun playersTeam(playerName: String) : Team? {
		return Bukkit.getServer().scoreboardManager.mainScoreboard.getEntryTeam(playerName);
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