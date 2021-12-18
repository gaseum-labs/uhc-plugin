package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.codeland.uhc.core.*
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.util.Action
import com.codeland.uhc.world.WorldManager
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {
	@Subcommand("reset")
	@Description("reset things to the waiting stage")
	fun testReset(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		if (UHC.game == null) {
			return Commands.errorMessage(sender, "Game is not running")
		}

		UHC.destroyGame()

		Action.sendGameMessage(sender, "Game reset")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("addLate")
	@Description("adds a player to the game after it has already started")
	fun addLate(sender: CommandSender, latePlayer: OfflinePlayer) {
		if (Commands.opGuard(sender)) return
		val game = UHC.game ?: return Commands.errorMessage(sender, "Game needs to be going")

		if (PlayerData.isOptingOut(latePlayer.uniqueId)) return Commands.errorMessage(sender,
			"${latePlayer.name} is opting out of participating")

		val team = game.teams.playersTeam(latePlayer.uniqueId) ?: return Commands.errorMessage(sender,
			"${latePlayer.name} must be on a team")

		fun randomLocation(): Location? {
			return PlayerSpreader.spreadSinglePlayer(game.world, (game.world.worldBorder.size / 2) - 5)
		}

		/* find a team member who is not the added player, and who is participating */
		val teammate = team.members.filter { it != latePlayer.uniqueId }.find { PlayerData.isParticipating(it) }

		/* teleport to the teammate if possible */
		val startLocation = if (teammate == null) {
			randomLocation()
		} else {
			Action.getPlayerLocation(teammate) ?: randomLocation()
		} ?: return Commands.errorMessage(sender, "Could not find a spot to start ${latePlayer.name}")

		game.startPlayer(latePlayer.uniqueId, startLocation)

		Action.sendGameMessage(sender, "Started player ${latePlayer.name} late")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("kill")
	@Description("kill a player and record it in the death ledger")
	fun kill(sender: CommandSender, offlinePlayer: OfflinePlayer) {
		if (Commands.opGuard(sender)) return
		val game = UHC.game ?: return Commands.errorMessage(sender, "Game needs to be going")

		val playerData = PlayerData.getPlayerData(offlinePlayer.uniqueId)

		if (!playerData.participating) return Commands.errorMessage(sender, "${offlinePlayer.name} is not in the game")
		if (!playerData.alive) return Commands.errorMessage(sender, "${offlinePlayer.name} is already dead")

		game.playerDeath(offlinePlayer.uniqueId, null, playerData, true)
	}

	@Subcommand("pvpCycle")
	fun lobbyCycle(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		ArenaManager.destroyArenas(WorldManager.pvpWorld)

		WorldManager.refreshWorld(WorldManager.PVP_WORLD_NAME, World.Environment.NORMAL, true)

		Action.sendGameMessage(sender, "Pvp world reset")
	}

	private fun moveAllToLobby() {
		Bukkit.getOnlinePlayers().forEach { player ->
			if (!WorldManager.isNonGameWorld(player.world)) Lobby.onSpawnLobby(player)
		}
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tp")
	@Description("teleport to a player's location")
	fun tpCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		sender as Player
		if (Commands.opGuard(sender)) return

		val location = Action.getPlayerLocation(toPlayer.uniqueId)

		if (location == null) {
			Commands.errorMessage(sender, "Could not find that player!")
		} else {
			Action.sendGameMessage(sender, "Teleported to ${toPlayer.name}")
			sender.teleport(location)
		}
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tphere")
	@Description("teleport a player to you")
	fun tpHereCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		sender as Player
		if (Commands.opGuard(sender)) return

		Action.teleportPlayer(toPlayer.uniqueId, sender.location)

		Action.sendGameMessage(sender, "Teleported ${toPlayer.name} to you")
	}

	@CommandCompletion("@uhcplayer @quirkclass")
	@Subcommand("class")
	@Description("override someone's class")
	fun classCommand(sender: CommandSender, player: OfflinePlayer, quirkClass: QuirkClass) {
		sender as Player

		if (Commands.opGuard(sender)) return

		val game = UHC.game ?: return Commands.errorMessage(sender, "Game has not started")
		val classes =
			game.getQuirk<Classes>(QuirkType.CLASSES) ?: return Commands.errorMessage(sender, "Classes are not enabled")

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(sender, "Pick a class")

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val oldClass = classes.getClass(playerData)

		classes.setClass(player.uniqueId, quirkClass)

		/* only start them if the game has already started */
		if (playerData.participating) Action.playerAction(player.uniqueId) { onlinePlayer ->
			Classes.startAsClass(onlinePlayer, quirkClass, oldClass)
		}

		Action.sendGameMessage(sender, "Set ${player.name}'s class to ${quirkClass.prettyName}")
	}
}
