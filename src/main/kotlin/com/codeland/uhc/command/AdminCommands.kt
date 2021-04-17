package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.Preset
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.team.TeamData
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {
	@Subcommand("start")
	@Description("start the UHC")
	fun startGame(sender : CommandSender) {
		if (Commands.opGuard(sender)) return
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) return Commands.errorMessage(sender, "Game has already started!")

		GameRunner.sendGameMessage(sender, "Starting UHC...")

		val errMessage = GameRunner.uhc.startUHC(sender)
		if (errMessage != null) Commands.errorMessage(sender, errMessage)
	}

	@Subcommand("startAll")
	@Description("start the UHC with no teams")
	fun startGameAll(sender : CommandSender) {
		if (Commands.opGuard(sender)) return
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) return Commands.errorMessage(sender, "Game has already started!")

		/* stage everyone */
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (!playerData.optingOut) playerData.staged = true
		}

		GameRunner.sendGameMessage(sender, "Starting UHC...")

		val errMessage = GameRunner.uhc.startUHC(sender)
		if (errMessage != null) Commands.errorMessage(sender, errMessage)
	}

	@Subcommand("reset")
	@Description("reset things to the waiting stage")
	fun testReset(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		GameRunner.uhc.startPhase(PhaseType.WAITING)
	}


	@Subcommand("mobCoefficient")
	@Description("change the mob spawn cap coefficient")
	fun modifyMobCapCoefficient(sender : CommandSender, coefficient : Double) {
		if (Commands.opGuard(sender)) return

		GameRunner.uhc.mobCapCoefficient = coefficient
	}

	@Subcommand("setLength")
	@Description("set the length of a phase")
	fun setPhaseLength(sender: CommandSender, type: PhaseType, length: Int) {
		if (Commands.opGuard(sender)) return
		if (GameRunner.uhc.isPhase(type)) {
			Commands.errorMessage(sender, "Cannot modify the phase you are in!")
			return
		}

		if (!type.hasTimer)
			return Commands.errorMessage(sender, "${type.prettyName} does not have a timer")

		GameRunner.uhc.updateTime(type, length)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@Subcommand("startRadius")
	@Description("set the starting radius")
	fun setStartRadius(sender: CommandSender, radius: Int) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updateStartRadius(radius)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@Subcommand("endRadius")
	@Description("set the final radius")
	fun setEndRadius(sender: CommandSender, radius: Int) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updateEndRadius(radius)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@Subcommand("preset")
	@Description("set all details of the UHC")
	fun modifyAll(sender: CommandSender, startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updatePreset(startRadius, endRadius, graceTime, shrinkTime)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@Subcommand("preset")
	@Description("set all details of the UHC")
	fun modifyAll(sender: CommandSender, preset: Preset) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updatePreset(preset)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@Subcommand("stage")
	@Description("adds a player to the game without adding them to a team")
	fun participateCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		if (playerData.optingOut) return Commands.errorMessage(sender, "${player.name} has opted out of participating!")

		playerData.staged = true

		GameRunner.sendGameMessage(sender, "${player.name} is staged for participating")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("addLate")
	@Description("adds a player to the game after it has already started")
	fun addLate(sender: CommandSender, offlinePlayer: OfflinePlayer) {
		if (Commands.opGuard(sender)) return
		if (!GameRunner.uhc.isGameGoing()) return Commands.errorMessage(sender, "Game needs to be going!")
		if (PlayerData.isOptingOut(offlinePlayer.uniqueId)) return Commands.errorMessage(sender, "${offlinePlayer.name} is opting out of participating!")

		/* teleport will be to an alive team member if player is on a team */
		/* will be to a random location if not on a team or no playing team members */
		val team = TeamData.playersTeam(offlinePlayer.uniqueId)

		fun randomLocation(): Location? {
			val world = GameRunner.uhc.getDefaultWorld()
			return GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
		}

		val teleportLocation = if (team == null) {
			randomLocation()

		} else {
			/* find a team member who is not the added player, and who is participating */
			val teammate = team.members.filter { it != offlinePlayer.uniqueId }.find { PlayerData.isParticipating(it) }

			/* teleport to the teammate if possible */
			if (teammate == null)
				randomLocation()
			else
				GameRunner.getPlayerLocation(teammate) ?: randomLocation()

		} ?: return Commands.errorMessage(sender, "No teleport location found")

		GraceDefault.startPlayer(offlinePlayer.uniqueId, teleportLocation)

		GameRunner.sendGameMessage(sender, "Started player ${offlinePlayer.name} late")
	}

	@Subcommand("pregen")
	@Description("Generates all chunks in the playable area")
	fun pregen(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		if (PreGenner.taskID == -1) {
			PreGenner.pregen(sender as Player)
		} else {
			Commands.errorMessage(sender, "Pregen has already started!")
		}
	}

	@CommandCompletion("@uhcplayer @quirkclass")
	@Subcommand("class")
	@Description("override someone's class")
	fun classCommand(sender: CommandSender, player: OfflinePlayer, quirkClass: QuirkClass) {
		sender as Player

		if (Commands.opGuard(sender)) return

		if (!GameRunner.uhc.isEnabled(QuirkType.CLASSES)) return Commands.errorMessage(sender, "Classes are not enabled")

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(sender, "Pick a class")

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val oldClass = Classes.getClass(playerData)

		Classes.setClass(player.uniqueId, quirkClass)

		/* only start them if the game has already started */
		if (GameRunner.uhc.isGameGoing() && playerData.participating) GameRunner.playerAction(player.uniqueId) { onlinePlayer ->
			Classes.startAsClass(onlinePlayer, quirkClass, oldClass)
		}

		GameRunner.sendGameMessage(sender, "Set ${player.name}'s class to ${quirkClass.prettyName}")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tp")
	@Description("teleport to a player's location")
	fun tpCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		sender as Player
		if (Commands.opGuard(sender)) return

		val location = GameRunner.getPlayerLocation(toPlayer.uniqueId)

		if (location == null) {
			Commands.errorMessage(sender, "Could not find that player!")
		} else {
			GameRunner.sendGameMessage(sender, "Teleported to ${toPlayer.name}")
			sender.teleport(location)
		}
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tphere")
	@Description("teleport a player to you")
	fun tpHereCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		sender as Player
		if (Commands.opGuard(sender)) return

		GameRunner.teleportPlayer(toPlayer.uniqueId, sender.location)

		GameRunner.sendGameMessage(sender, "Teleported ${toPlayer.name} to you")
	}
}
