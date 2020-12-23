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
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType
import java.util.*
import kotlin.collections.ArrayList

object GameRunner {
	var bot: MixerBot? = null
	lateinit var uhc: UHC

	val netherWorldFix: Boolean
	val mushroomWorldFix: Boolean
	val oreWorldFix: Boolean
	val melonWorldFix: Boolean
	val dungeonWorldFix: Boolean
	val sugarCaneWorldFix: Boolean
	val halloweenGeneration: Boolean
	val chunkSwapping: Boolean

	init {
		val worldGenInfo = WorldGenFile.getSettings()

		netherWorldFix = worldGenInfo[0]
		mushroomWorldFix = worldGenInfo[1]
		oreWorldFix = worldGenInfo[2]
		melonWorldFix = worldGenInfo[3]
		dungeonWorldFix = worldGenInfo[4]
		sugarCaneWorldFix = worldGenInfo[5]
		halloweenGeneration = worldGenInfo[6]
		chunkSwapping = worldGenInfo[7]

		Util.log("${ChatColor.GOLD}Nether World Fix: ${ChatColor.RED}$netherWorldFix")
		Util.log("${ChatColor.GOLD}Mushroom World Fix: ${ChatColor.RED}$mushroomWorldFix")
		Util.log("${ChatColor.GOLD}Ore World Fix: ${ChatColor.RED}$oreWorldFix")
		Util.log("${ChatColor.GOLD}Melon World Fix: ${ChatColor.RED}$melonWorldFix")
		Util.log("${ChatColor.GOLD}Dungeon World Fix: ${ChatColor.RED}$dungeonWorldFix")
		Util.log("${ChatColor.GOLD}Sugar Cane World Fix: ${ChatColor.RED}$sugarCaneWorldFix")
		Util.log("${ChatColor.GOLD}Halloween Generation: ${ChatColor.RED}$halloweenGeneration")
		Util.log("${ChatColor.GOLD}Chunk Swapping: ${ChatColor.RED}$chunkSwapping")
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

	/**
	 * takes in a group of players uuids, filters out which ones are alive
	 */
	private fun constructAliveList(group: ArrayList<UUID>): ArrayList<UUID> {
		return group.filter { uuid ->
			val data = uhc.playerDataList[uuid]

			data != null && data.participating && data.alive
		} as ArrayList<UUID>
	}

	fun playerDeath(deadUUID: UUID, killer: Player?) {
		uhc.setAlive(deadUUID, false)

		val deadPlayerTeam = TeamData.playersTeam(deadUUID)
		var (remainingTeams, lastRemaining, teamIsAlive, individualIsAlive) = remainingTeams(deadPlayerTeam, deadUUID)

		val killerTeam = if (killer == null) null else TeamData.playersTeam(killer.uniqueId)

		var killerName = if (killer == null) {
			null

		} else when {
			killer.uniqueId === deadUUID -> "self"
			deadPlayerTeam === killerTeam -> "teammate"
			else -> killer.name
		}

		/* add to ledger */
		val deadPlayerName = Bukkit.getOfflinePlayer(deadUUID).name ?: "NULL"
		uhc.ledger.addEntry(deadPlayerName, uhc.elapsedTime, killerName)

		val message1 = "$remainingTeams teams remain"

		/* broadcast elimination message for an individual */
		if (deadPlayerTeam == null && !individualIsAlive) {
			val message0 = "${ChatColor.GOLD}${ChatColor.BOLD}${deadPlayerName} has been Eliminated!"

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
			return uhc.endUHC(
				if (lastRemaining != null)
					constructAliveList(lastRemaining)
				else
					ArrayList()
			)

		/* kill reward awarding for a team */
		if (killerTeam != null) {
			if (!teamIsAlive) uhc.killReward.applyReward(constructAliveList(killerTeam.members).map { uuid ->
				Bukkit.getPlayer(uuid)
			} as ArrayList<Player?>)

		/* kill reward awarding for an individual killer */
		} else if (killer != null) {
			if (!teamIsAlive) uhc.killReward.applyReward(arrayListOf(killer))
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

		if (onlinePlayer == null) {
			val playerData = uhc.getPlayerData(uuid)

			playerData.offlineZombie?.teleport(location)

		} else {
			onlinePlayer.teleport(location)
		}
	}

	fun potionEffectPlayer(uuid: UUID, effect: PotionEffect) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = uhc.getPlayerData(uuid)
			playerData.offlineZombie?.addPotionEffect(effect)

		} else {
			onlinePlayer.addPotionEffect(effect)
		}
	}

	fun getPlayerLocation(uuid: UUID): Location? {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		return if (onlinePlayer == null) {
			val playerData = uhc.getPlayerData(uuid)
			playerData.offlineZombie?.location

		} else {
			onlinePlayer.location
		}
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
