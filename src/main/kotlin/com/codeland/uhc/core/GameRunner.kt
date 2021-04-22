package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.RenderType
import java.util.*
import kotlin.collections.ArrayList

object GameRunner {
	var bot: MixerBot? = null

	lateinit var heartsObjective: Objective

	data class RemainingTeamsReturn(val remaining: Int, val lastAlive: List<UUID>?, val teamAlive: Boolean)

	/**
	 * returns both the number of remaining teams
	 * and the last remaining team if there is exactly 1
	 */
	fun remainingTeams(focusTeam: Team?) : RemainingTeamsReturn {
		val remainingTeams = TeamData.teams.filter { teamIsAlive(it) }

		val remainingIndividuals = PlayerData.playerDataList.filter { (uuid, playerData) ->
			playerData.alive && TeamData.playersTeam(uuid) == null
		}.map { (uuid, _) -> uuid }

		val remainingCount = remainingTeams.size + remainingIndividuals.size

		val lastAlive = remainingTeams.firstOrNull()?.members ?: remainingIndividuals

		/* lastAlive is only set if only one group of players remains */
		return RemainingTeamsReturn(
			remainingCount,
			if (remainingCount == 1) lastAlive else null,
			remainingTeams.any { it === focusTeam }
		)
	}

	fun teamIsAlive(team: Team) = team.members.any { member -> PlayerData.isAlive(member) }

	/**
	 * @param group the last remaining group of players, can be null for no last remaining group
	 * @return all the uuids of currently alive players in the group
	 */
	private fun constructAliveList(group: List<UUID>?): List<UUID> {
		return group?.map { Pair(it, PlayerData.getPlayerData(it)) }
			?.filter { (_, playerData) -> playerData.alive }
			?.map { (uuid, _) -> uuid }
			?: emptyList()
	}

	fun playerDeath(uuid: UUID, killer: Player?, playerData: PlayerData) {
		if (shouldRespawn(killer, playerData)) {
			playerRespawn(uuid)
		} else {
			playerPermaDeath(uuid, killer, UHC.isEnabled(QuirkType.PESTS)) { Pests.onBecomePest(it) }
		}
	}

	private fun shouldRespawn(killer: Player?, playerData: PlayerData): Boolean {
		return UHC.isPhase(PhaseType.GRACE) || (killer == null && !UHC.isPhase(PhaseType.ENDGAME)) || playerData.undead()
	}

	private fun playerPermaDeath(uuid: UUID, killer: Player?, respawn: Boolean, setupRespawn: (UUID) -> Unit) {
		PlayerData.setAlive(uuid, false)
		PlayerData.setParticipating(uuid, respawn)

		val team = TeamData.playersTeam(uuid)
		val (remainingTeams, lastRemaining, teamIsAlive) = remainingTeams(team)

		val killerTeam = if (killer == null) null else TeamData.playersTeam(killer.uniqueId)

		val killerName = when {
			killer == null -> null
			killer.uniqueId == uuid -> "self"
			team === killerTeam -> "teammate"
			else -> killer.name
		}

		/* add to ledger */
		val deadPlayerName = Bukkit.getOfflinePlayer(uuid).name ?: "NULL"
		UHC.ledger.addEntry(deadPlayerName, UHC.elapsedTime, killerName)

		/* broadcast elimination message */
		val elimMessage1 = when {
			team != null -> team.apply(team.gameName()).append(Component.text(" has been eliminated!", NamedTextColor.GOLD, TextDecoration.BOLD))
			else -> Component.text("$deadPlayerName has been Eliminated!", NamedTextColor.GRAY, TextDecoration.BOLD)
		}
		val elimMessage2 = Component.text("$remainingTeams teams remain", NamedTextColor.GRAY, TextDecoration.BOLD)

		Bukkit.getServer().onlinePlayers.filter { WorldManager.isGameWorld(it.world) }.forEach { player ->
			player.sendMessage(elimMessage1)
			player.sendMessage(elimMessage2)
		}

		/* does the UHC end here? */
		if (remainingTeams <= 1) {
			playerAction(uuid) { it.gameMode = GameMode.SPECTATOR }
			UHC.endUHC(constructAliveList(lastRemaining))

		/* or does it keep going */
		} else {
			/* apply kill reward */
			if (killer != null) UHC.killReward.applyReward(arrayListOf(killer))

			/* tell player they died */
			if (respawn) {
				setupRespawn(uuid)
				playerRespawn(uuid)
			} else {
				playerAction(uuid) { deathTitle(it, killer, false) }
			}
		}
	}

	private fun playerRespawn(uuid: UUID) {
		playerAction(uuid) { deathTitle(it, null, true) }

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin, {
			teleportPlayer(uuid, respawnLocation())
			playerAction(uuid) { it.gameMode = GameMode.SURVIVAL }
			UHC.quirks.filter { it.enabled }.forEach { it.onStart(uuid) }
		}, 100)
	}

	private fun deathTitle(player: Player, killer: Player?, respawn: Boolean) {
		player.gameMode = GameMode.SPECTATOR
		AbstractLobby.resetPlayerStats(player)

		player.sendTitle(
			"${RED}You died!",
			"${DARK_RED}${if (respawn) "Prepare to respawn" else {
				if (killer == null) "Killed by environment" else "Killed by ${killer.name}"
			}}",
			0, 80, 20
		)
	}

	fun respawnLocation(): Location {
		val world = UHC.getDefaultWorld()
		return GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
			?: Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
	}

	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage("$GOLD$BOLD$message")
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage("$GOLD$BOLD$message")
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
				playerData.offlineZombie = playerData.createDefaultZombie(uuid, location)
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
		return "$color$BOLD$string$GOLD$BOLD"
	}

	fun registerHearts() {
		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		val objective = scoreboard.getObjective("hp")
			?: scoreboard.registerNewObjective("hp", "health", Component.text("hp"), RenderType.HEARTS)

		objective.renderType = RenderType.HEARTS
		objective.displayName(Component.text("hp"))
		objective.displaySlot = DisplaySlot.PLAYER_LIST

		heartsObjective = objective
	}
}
