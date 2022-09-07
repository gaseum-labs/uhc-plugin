package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.chc.chcs.classes.Classes
import org.gaseumlabs.uhc.chc.chcs.classes.QuirkClass
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.world.WorldManager
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {
	@Subcommand("reset")
	@Description("destroy the game")
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

		if (PlayerData.get(latePlayer).optingOut) return Commands.errorMessage(sender,
			"${latePlayer.name} is opting out of participating")

		val team = game.teams.playersTeam(latePlayer.uniqueId) ?: return Commands.errorMessage(sender,
			"${latePlayer.name} must be on a team")

		fun randomLocation(): Location? {
			return PlayerSpreader.spreadSinglePlayer(game.world, (game.world.worldBorder.size / 2) - 5)
		}

		/* find a team member who is not the added player, and who is participating */
		val teammate = team.members.filter { it != latePlayer.uniqueId }.find { PlayerData.get(it).participating }

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

		val playerData = PlayerData.get(offlinePlayer.uniqueId)

		if (!playerData.participating) return Commands.errorMessage(sender, "${offlinePlayer.name} is not in the game")
		if (!playerData.alive) return Commands.errorMessage(sender, "${offlinePlayer.name} is already dead")

		game.playerDeath(offlinePlayer.uniqueId, null, playerData, true)
	}

	@Subcommand("pvpCycle")
	fun lobbyCycle(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		ArenaManager.destroyAllArenas()

		WorldManager.refreshWorld(WorldManager.PVP_WORLD_NAME, World.Environment.NORMAL, true)

		Action.sendGameMessage(sender, "Pvp world reset")
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
		val classes = UHC.chc as? Classes ?: return Commands.errorMessage(sender, "Classes are not enabled")

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(sender, "Pick a class")

		val playerData = PlayerData.get(player.uniqueId)
		val oldClass = classes.getClass(playerData)

		classes.setClass(player.uniqueId, quirkClass)

		/* only start them if the game has already started */
		if (playerData.participating) Action.playerAction(player.uniqueId) { onlinePlayer ->
			Classes.startAsClass(onlinePlayer, quirkClass, oldClass)
		}

		Action.sendGameMessage(sender, "Set ${player.name}'s class to ${quirkClass.prettyName}")
	}

	@Subcommand("banPlatform")
	@CommandCompletion("@uhcplayer")
	fun bad(sender: CommandSender, banPlayer: OfflinePlayer) {
		sender as Player
		if (Commands.opGuard(sender)) return

		val removed = GapSlapArena.submittedPlatforms.remove(banPlayer.uniqueId)

		Action.sendGameMessage(sender,
			if (removed == null) "${banPlayer.name} has not submitted a platform"
			else "Removed ${banPlayer.name}'s platform"
		)
	}
}
