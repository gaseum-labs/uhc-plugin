package com.codeland.uhc.core

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.blockfix.BrownMushroomFix
import com.codeland.uhc.blockfix.LeavesFix
import com.codeland.uhc.blockfix.RedMushroomFix
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.team.Team
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldGenFile
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType
import java.util.*
import kotlin.collections.ArrayList

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
		return team.members.any { member -> uhc.isAlive(member) }
	}

	data class RemainingTeamsReturn(val remaining: Int, val lastAlive: ArrayList<UUID>?, val teamAlive: Boolean, val individualAlive: Boolean)

	/**
	 * returns both the number of remaining teams
	 * and the last remaining team if there is exactly 1
	 */
	fun remainingTeams(focusTeam: Team?, focusIndividual: UUID?) : RemainingTeamsReturn {
		var remaining = 0
		var lastAlive = null as ArrayList<UUID>?
		var teamAlive = false
		var individualAlive = false

		TeamData.teams.forEach { team ->
			if (teamIsAlive(team)) {
				if (team == focusTeam) teamAlive = true

				++remaining
				lastAlive = team.members
			}
		}

		uhc.playerDataList.forEach { (uuid, playerData) ->
			if (TeamData.playersTeam(uuid) == null && playerData.alive && playerData.participating) {
				if (focusIndividual == uuid) individualAlive = true

				++remaining
				lastAlive = arrayListOf(uuid)
			}
		}

		/* lastAlive is only set if only one group of players remains */
		return RemainingTeamsReturn(remaining, if (remaining == 1) lastAlive else null, teamAlive, individualAlive)
	}

	fun playerDeath(deadPlayer: Player) {
		uhc.setAlive(deadPlayer.uniqueId, false)

		val deadPlayerTeam = TeamData.playersTeam(deadPlayer.uniqueId)
		var (remainingTeams, lastRemaining, teamIsAlive, individualIsAlive) = remainingTeams(deadPlayerTeam, deadPlayer.uniqueId)

		val killer = deadPlayer.killer
		val killerTeam = if (killer == null) null else TeamData.playersTeam(killer.uniqueId)

		var killerName = if (killer == null) {
			null

		} else when {
			killer === deadPlayer -> "self"
			deadPlayerTeam === killerTeam -> "teammate"
			else -> killer.name
		}

		/* add to ledger */
		uhc.ledger.addEntry(deadPlayer.name, uhc.elapsedTime, killerName)

		val message1 = "$remainingTeams teams remain"

		/* broadcast elimination message for an individual */
		if (deadPlayerTeam == null && !individualIsAlive) {
			val message0 = "${ChatColor.GOLD}${ChatColor.BOLD}${deadPlayer.name} has been Eliminated!"

			Bukkit.getServer().onlinePlayers.forEach { player ->
				sendGameMessage(player, message0)
				sendGameMessage(player, message1)
			}

		/* broadcast elimination message for a team */
		} else if (deadPlayerTeam != null && !teamIsAlive) {
			val message0 = "${deadPlayerTeam.colorPair.colorString(deadPlayerTeam.displayName)} ${ChatColor.GOLD}${ChatColor.BOLD}has been Eliminated!"

			Bukkit.getServer().onlinePlayers.forEach { player ->
				sendGameMessage(player, message0)
				sendGameMessage(player, message1)
			}
		}

		/* uhc ending point (stops kill reward) */
		if (remainingTeams <= 1)
			return uhc.endUHC(lastRemaining ?: ArrayList())

		/* kill reward awarding for a team */
		if (killerTeam != null) {
			if (!teamIsAlive) uhc.killReward.applyReward(Array(killerTeam.members.size) { i ->
				Bukkit.getPlayer(killerTeam.members[i])
			})

		/* kill reward awarding for an individual killer */
		} else if (killer != null) {
			if (!teamIsAlive) uhc.killReward.applyReward(arrayOf(killer))
		}
	}

	fun prettyPlayerName(player: Player): String {
		val team = TeamData.playersTeam(player.uniqueId)
		return team?.colorPair?.colorString(player.name) ?: player.name
	}

	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun playerAction(uuid: UUID, action: (Player) -> Unit) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) uhc.getPlayerData(uuid).actionsQueue.add(action)
		else action(onlinePlayer)
	}

	fun teleportPlayer(uuid: UUID, location: Location) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) uhc.getPlayerData(uuid).actionsQueue.add { futurePlayer ->
			futurePlayer.teleport(location)
		}
		else onlinePlayer.teleport(location)
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
