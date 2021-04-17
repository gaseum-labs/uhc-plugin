package com.codeland.uhc.core

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType
import java.util.*
import kotlin.collections.ArrayList

object GameRunner {
	var bot: MixerBot? = null
	lateinit var uhc: UHC

	fun teamIsAlive(team: Team): Boolean {
		return team.members.any { member -> PlayerData.isAlive(member) }
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

		/* count up all teams */
		TeamData.teams.forEach { team ->
			if (teamIsAlive(team)) {
				if (team == focusTeam) teamAlive = true

				++remaining
				lastAlive = team.members
			}
		}

		/* count up all players not on a team */
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.alive && TeamData.playersTeam(uuid) == null) {
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
			val data = PlayerData.playerDataList[uuid]

			data != null && data.participating && data.alive
		} as ArrayList<UUID>
	}

	fun playerDeath(deadUUID: UUID, killer: Player?) {
		PlayerData.setAlive(deadUUID, false)
		PlayerData.setParticipating(deadUUID, false)

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
			val message0 = deadPlayerTeam.apply(deadPlayerTeam.gameName()).append(Component.text(" has been eliminated!", NamedTextColor.GOLD, TextDecoration.BOLD))

			Bukkit.getServer().onlinePlayers.forEach { player ->
				player.sendMessage(message0)
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

	fun respawnPlayer(player: Player?, uuid: UUID, playerData: PlayerData): Location? {
		val world = uhc.getDefaultWorld()
		val respawnLocation = GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)

		/* an offline zombie died */
		if (player == null)
			playerData.offlineZombie = playerData.createDefaultZombie(
				uuid, respawnLocation ?: Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
			)

		/* custom quirk behavior when players start */
		SchedulerUtil.nextTick {
			uhc.quirks.forEach { quirk ->
				if (quirk.enabled) quirk.onStart(uuid)
			}
		}

		return respawnLocation
	}

	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}$message")
	}

	fun playerAction(uuid: UUID, action: (Player) -> Unit) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) PlayerData.getPlayerData(uuid).actionsQueue.add(action)
		else action(onlinePlayer)
	}

	fun teleportPlayer(uuid: UUID, location: Location) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)

			val zombie = playerData.offlineZombie
			if (zombie == null)
				playerData.createDefaultZombie(uuid, location)
			else
				zombie.teleport(location)

		} else {
			onlinePlayer.teleport(location)
		}
	}

	fun potionEffectPlayer(uuid: UUID, effect: PotionEffect) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)
			playerData.offlineZombie?.addPotionEffect(effect)

		} else {
			onlinePlayer.addPotionEffect(effect)
		}
	}

	fun damagePlayer(uuid: UUID, damage: Double, source: Entity? = null) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)
			playerData.offlineZombie?.damage(damage, source)

		} else {
			onlinePlayer.damage(damage, source)
		}
	}

	fun getPlayerLocation(uuid: UUID): Location? {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		return if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)
			playerData.offlineZombie?.location

		} else {
			onlinePlayer.location
		}
	}

	fun setPlayerRiding(uuid: UUID, entity: Entity) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val zombie = PlayerData.getPlayerData(uuid).offlineZombie
			if (zombie != null) entity.addPassenger(zombie)

		} else {
			entity.addPassenger(onlinePlayer)
		}
	}

	fun coloredInGameMessage(string: String, color: ChatColor): String {
		return "$color${ChatColor.BOLD}$string${ChatColor.GOLD}${ChatColor.BOLD}"
	}

	fun netherIsAllowed() : Boolean {
		return !uhc.isPhase(PhaseType.ENDGAME)
	}

	fun registerHearts() {
		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		val objective = scoreboard.getObjective("hp")
			?: scoreboard.registerNewObjective("hp", "health", "hp", RenderType.HEARTS)

		objective.renderType = RenderType.HEARTS
		objective.displayName = "hp"
		objective.displaySlot = DisplaySlot.PLAYER_LIST
	}
}
