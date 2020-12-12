package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.command.ubt.PartialUBT
import com.codeland.uhc.command.ubt.UBT
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.KillReward
import com.codeland.uhc.phase.*
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.quirk.quirks.LowGravity
import com.codeland.uhc.team.ColorPair
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.team.TeamMaker
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.block.data.BlockData
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {
	@Subcommand("start")
	@Description("start the UHC")
	fun startGame(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		GameRunner.sendGameMessage(sender, "Starting UHC...")

		val errMessage = GameRunner.uhc.startUHC(sender)
		if (errMessage != null) Commands.errorMessage(sender, errMessage)
	}

	@Subcommand("startAll")
	@Description("start the UHC with no teams")
	fun startGameAll(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		/* first make everyone participate */
		GameRunner.uhc.playerDataList.forEach { (uuid, playerData) ->
			if (!playerData.optingOut) playerData.participating = true
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

	@Subcommand("participate")
	@Description("adds a player to the game without adding them to a team")
	fun participateCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		if (GameRunner.uhc.isOptingOut(player.uniqueId))
			return Commands.errorMessage(sender, "${player.name} has opted out of participating!")

		GameRunner.uhc.setParticipating(player.uniqueId, true)

		GameRunner.sendGameMessage(sender, "${player.name} is now participating")
	}

	@Subcommand("addLate")
	@Description("adds a player to the game after it has already started")
	fun addLate(sender: CommandSender, playerName: String, teammateName: String) {
		if (Commands.opGuard(sender)) return

		val player = Bukkit.getPlayer(playerName) ?: return Commands.errorMessage(sender, "Can't find player $playerName")
		val teammate = Bukkit.getPlayer(teammateName) ?: return Commands.errorMessage(sender, "Can't find player $teammateName")

		if (!GameRunner.uhc.isGameGoing()) return Commands.errorMessage(sender, "Game needs to be going!")
		if (GameRunner.uhc.isOptingOut(player.uniqueId)) return Commands.errorMessage(sender, "${player.name} is opting out of participating!")

		val joinTeam = TeamData.playersTeam(teammate.uniqueId) ?: return Commands.errorMessage(sender, "${teammate.name} has no team to join!")

		lateTeamTeleport(sender, player, teammate.location, TeamData.addToTeam(joinTeam, player.uniqueId, true))
	}

	@Subcommand("addLate")
	@Description("adds a player to the game after it has already started")
	fun addLate(sender: CommandSender, playerName: String) {
		if (Commands.opGuard(sender)) return

		val player = Bukkit.getPlayer(playerName) ?: return Commands.errorMessage(sender, "Can't find player ${playerName}")

		if (!GameRunner.uhc.isGameGoing()) return Commands.errorMessage(sender, "Game needs to be going!")
		if (GameRunner.uhc.isOptingOut(player.uniqueId)) return Commands.errorMessage(sender, "${player.name} is opting out of participating!")

		val world = Bukkit.getWorlds()[0]
		val teleportLocation = GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
			?: return Commands.errorMessage(sender, "No suitible teleport location found!")

		var teamColorPairs = TeamMaker.getColorList(1) ?: return Commands.errorMessage(sender, "There are already the maximum amount of teams (${TeamData.MAX_TEAMS})")

		lateTeamTeleport(sender, player, teleportLocation, TeamData.addToTeam(teamColorPairs[0], player.uniqueId, true))
	}

	private fun lateTeamTeleport(sender: CommandSender, player: Player, location: Location, team: Team) {
		GameRunner.uhc.setAlive(player.uniqueId, true)
		GameRunner.uhc.setParticipating(player.uniqueId, true)

		player.teleportAsync(location).thenAccept {
			player.gameMode = GameMode.SURVIVAL

			/* make sure the player doesn't die when they get teleported */
			player.fallDistance = 0f
			player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 10, true))

			GameRunner.sendGameMessage(sender, "${player.name} successfully added to team ${team.colorPair.colorString(team.displayName)}")
		}
	}
}
