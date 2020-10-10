package com.codeland.uhc.core

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.blockfix.BrownMushroomFix
import com.codeland.uhc.blockfix.LeavesFix
import com.codeland.uhc.blockfix.RedMushroomFix
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.Team
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldGenFile
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType

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

	fun playerIsAlive(player: Player?): Boolean {
		return when {
			player == null -> false
			uhc.isEnabled(QuirkType.PESTS) && Pests.isPest(player) -> false
			player.gameMode == GameMode.SURVIVAL -> true
			else -> false
		}
	}

	fun teamIsAlive(team: Team): Boolean {
		return team.members.any { entry -> playerIsAlive(entry.player) }
	}

	/**
	 * returns both the number of remaining teams
	 * and the last remaining team if there is exactly 1
	 */
	fun remainingTeams(focus: Team? = null) : Triple<Int, Team?, Boolean> {
		var retRemaining = 0
		var retAlive = null as Team?
		var retFocus = false

		TeamData.teams.forEach { team ->
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

		TeamData.teams.forEach { team ->
			if (teamIsAlive(team)) ++retRemaining
		}

		/* only give last alive if only one team is alive */
		return retRemaining
	}

	fun playerDeath(deadPlayer: Player, removeTeam: Boolean) {
		var deadPlayerTeam = TeamData.playersTeam(deadPlayer) ?: return
		var (remainingTeams, lastRemaining, teamIsAlive) = remainingTeams(deadPlayerTeam)

		val killer = deadPlayer.killer
		val killerTeam = if (killer == null) null else TeamData.playersTeam(killer)

		var killerName = if (killer == null) {
			null
		} else {
			if (deadPlayerTeam === killerTeam) "teammate"
			else killer.name
		}

		/* add to ledger */
		uhc.ledger.addEntry(deadPlayer.name, uhc.elapsedTime, killerName)

		/* broadcast elimination message */
		val message0 = "${deadPlayerTeam.colorPair.colorString(deadPlayerTeam.displayName)} ${ChatColor.GOLD}${ChatColor.BOLD}has been Eliminated!"
		val message1 = "$remainingTeams teams remain"

		if (!teamIsAlive) Bukkit.getServer().onlinePlayers.forEach { player ->
			sendGameMessage(player, message0)
			sendGameMessage(player, message1)
		}

		if (removeTeam) TeamData.removeFromTeam(deadPlayerTeam, deadPlayer)

		/* uhc ending point (stops kill reward) */
		if (lastRemaining != null || remainingTeams == 0)
			return uhc.endUHC(lastRemaining)

		/* kill reward awarding */
		if (killerTeam != null) {
			if (!teamIsAlive) uhc.killReward.applyReward(killerTeam)
		}
	}

	fun prettyPlayerName(player: Player): String {
		val team = TeamData.playersTeam(player)
		return team?.colorPair?.colorString(player.name) ?: player.name
	}

	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun coloredInGameMessage(string: String, color: ChatColor): String {
		return "$color${ChatColor.BOLD}$string${ChatColor.GOLD}${ChatColor.BOLD}"
	}

	fun broadcast(message: String) {
		Bukkit.getOnlinePlayers().forEach { sendGameMessage(it, message)}
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
