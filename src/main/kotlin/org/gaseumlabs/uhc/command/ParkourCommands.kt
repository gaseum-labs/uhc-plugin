package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.Platform
import org.gaseumlabs.uhc.lobbyPvp.PlatformStorage
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena
import org.gaseumlabs.uhc.lobbyPvp.arena.ParkourArena
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.BlockPos
import org.gaseumlabs.uhc.world.WorldManager

@CommandAlias("uhc")
class ParkourCommands : BaseCommand() {
	@Subcommand("parkour test")
	fun parkourTest(sender: CommandSender) {
		sender as Player

		val arena = ArenaManager.playersArena(sender.uniqueId) as? ParkourArena ?: return
		arena.enterPlayer(sender, sender.gameMode === GameMode.CREATIVE, false)
	}

	@Subcommand("parkour checkpoint")
	fun parkourCheckpoint(sender: CommandSender) {
		sender as Player

		val arena = ArenaManager.playersArena(sender.uniqueId) as? ParkourArena ?: return
		arena.enterPlayer(sender, true, true)
	}

	@Subcommand("parkour reset")
	fun parkourReset(sender: CommandSender) {
		sender as Player

		val arena = ArenaManager.playersArena(sender.uniqueId) as? ParkourArena ?: return

		val data = arena.getParkourData(sender.uniqueId)
		data.checkpoint = arena.startPosition
		data.timer = 0
		data.timerGoing = false
		arena.enterPlayer(sender, true, true)
	}

	@CommandCompletion("@uhcblockx @uhcblocky @uhcblockz @uhcblockx @uhcblocky @uhcblockz")
	@Subcommand("definePlatform")
	fun definePlatformCommand(
		sender: CommandSender,
		x0: Int,
		y0: Int,
		z0: Int,
		x1: Int,
		y1: Int,
		z1: Int,
		name: String,
	) {
		val player = sender as? Player ?: return

		val filteredName = name.trim()
		if (filteredName.length !in 3..36) return Commands.errorMessage(player,
			"Arena name must be 3 to 36 characters long")

		val arena = ArenaManager.playersArena(player.uniqueId) ?: return Commands.errorMessage(player,
			"You must be in your parkour arena")

		if (arena !is ParkourArena || arena.owner != player.uniqueId)
			return Commands.errorMessage(player, "You must be in your parkour arena")

		val world = player.world
		if (world !== WorldManager.pvpWorld) return Commands.errorMessage(player, "How did you get here")

		if (!arena.inBorder(x0, z0) || !arena.inBorder(x1, z1)) return Commands.errorMessage(player,
			"Please define a shape inside your border")

		val newStorage = PlatformStorage(player.uniqueId, BlockPos(x0, y0, z0), BlockPos(x1, y1, z1), name)

		val platform = Platform.fromStorage(WorldManager.pvpWorld, newStorage)

		GapSlapArena.submittedPlatforms[player.uniqueId] = platform

		Action.sendGameMessage(player, "Updated your gap slap arena")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("parkour premiere")
	fun parkourPremiereCommand(
		sender: CommandSender,
		parkourOwner: OfflinePlayer,
	) {
		if (Commands.opGuard(sender)) return

		val arena = ArenaManager.ongoingOf<ParkourArena>().find { it.owner == parkourOwner.uniqueId }
			?: return Commands.errorMessage(sender, "That player doesn't own a parkour arena")

		ParkourArena.premiereArena = arena
		Action.sendGameMessage(sender, "Set the premiere parkour arena to ${parkourOwner.name}'s arena")
	}
}