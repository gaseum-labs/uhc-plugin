package com.codeland.uhc.core

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.blockfix.BrownMushroomFix
import com.codeland.uhc.blockfix.LeavesFix
import com.codeland.uhc.blockfix.RedMushroomFix
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType
import org.bukkit.scoreboard.Team

object GameRunner {
	var bot: MixerBot? = null
	lateinit var uhc: UHC

	val leavesFix = LeavesFix()
	val redMushroomFix = RedMushroomFix()
	val brownMushroomFix = BrownMushroomFix()

	val netherWorldFix: Boolean
	val mushroomWorldFix: Boolean
	val oreWorldFix: Boolean
	val melonWorldFix: Boolean

	init {
		val worldGenInfo = WorldGenFile.getSettings()

		netherWorldFix = worldGenInfo.netherFix
		mushroomWorldFix = worldGenInfo.mushroomFix
		oreWorldFix = worldGenInfo.oreFix
		melonWorldFix = worldGenInfo.melonFix

		Util.log("${ChatColor.GOLD}Nether World Fix: ${ChatColor.RED}$netherWorldFix")
		Util.log("${ChatColor.GOLD}Mushroom World Fix: ${ChatColor.RED}$mushroomWorldFix")
		Util.log("${ChatColor.GOLD}Ore World Fix: ${ChatColor.RED}$oreWorldFix")
		Util.log("${ChatColor.GOLD}Melon World Fix: ${ChatColor.RED}$melonWorldFix")
	}

	fun teamIsAlive(team: Team): Boolean {
		return team.entries.any { entry ->
			val player = Bukkit.getServer().getPlayer(entry)

			when {
				player == null -> false
				uhc.isEnabled(QuirkType.PESTS) && Pests.isPest(player) -> false
				player.gameMode == GameMode.SURVIVAL -> true
				else -> false
			}
		}
	}

	/**
	 * returns both the number of remaining teams
	 * and the last remaining team if there is exactly 1
	 */
	fun remainingTeams(focus: Team? = null) : Triple<Int, Team?, Boolean> {
		var retRemaining = 0
		var retAlive = null as Team?
		var retFocus = false

		Bukkit.getServer().scoreboardManager.mainScoreboard.teams.forEach { team ->
			val alive = teamIsAlive(team)

			if (team == focus) retFocus = alive

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
			if (teamIsAlive(team)) ++retRemaining
		}

		/* only give last alive if only one team is alive */
		return retRemaining
	}

	fun playerDeath(deadPlayer: Player, removeTeam: Boolean) {
		var deadPlayerTeam = playersTeam(deadPlayer.name) ?: return
		var (remainingTeams, lastRemaining, teamIsAlive) = remainingTeams(deadPlayerTeam)

		var killerName = deadPlayer.killer?.name
		if (killerName != null) {
			val team = playersTeam(deadPlayer.name)

			/* prevent teamkills from counting as player kills */
			if (team != null && team.entries.contains(killerName))
				killerName = "teammate"
		}

		/* add to ledger */
		uhc.ledger.addEntry(deadPlayer.name, uhc.elapsedTime, killerName)

		/* broadcast elimination message */
		if (!teamIsAlive) Bukkit.getServer().onlinePlayers.forEach { player ->
			sendGameMessage(player, "${deadPlayerTeam.color}${ChatColor.BOLD}${deadPlayerTeam.displayName} ${ChatColor.GOLD}${ChatColor.BOLD}has been Eliminated!")
			sendGameMessage(player, "$remainingTeams teams remain")
		}

		if (removeTeam) {
			val team = playersTeam(deadPlayer.name)
			if (team != null) TeamData.removeFromTeam(team, deadPlayer.name)
		}

		/* uhc ending point (stops kill reward) */
		if (lastRemaining != null || remainingTeams == 0)
			return uhc.endUHC(lastRemaining)

		/* kill reward awarding */
		val killer = deadPlayer.killer ?: return
		val killerTeam = playersTeam(killer.name) ?: return

		uhc.killReward.applyReward(killerTeam)
	}

	fun playersTeam(playerName: String) : Team? {
		return Bukkit.getServer().scoreboardManager.mainScoreboard.getEntryTeam(playerName)
	}

	fun prettyPlayerName(name: String): String {
		val team = playersTeam(name)
		return (team?.color ?: ChatColor.WHITE).toString() + name
	}

	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun netherIsAllowed() : Boolean {
		return !uhc.isPhase(PhaseType.ENDGAME)
	}

	fun registerHearts() {
		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		val objective = scoreboard.getObjective("hp")
			?: scoreboard.registerNewObjective("hp", "health", "hp", RenderType.HEARTS)

		objective.displaySlot = DisplaySlot.PLAYER_LIST
	}
}
