package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.blockfix.LeavesFix
import com.codeland.uhc.core.KillReward
import com.codeland.uhc.phase.*
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.quirk.quirks.LowGravity
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {

	/* COMMANDS */

	@CommandAlias("start")
	@Description("start the UHC")
	fun startGame(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		val errMessage = GameRunner.uhc.startUHC(sender)

		if (errMessage == null)
			GameRunner.sendGameMessage(sender, "Starting UHC...")
		else
			Commands.errorMessage(sender, errMessage)
	}

	@CommandAlias("team clear")
	@Description("remove all current teams")
	fun clearTeams(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		val scoreboard = sender.server.scoreboardManager.mainScoreboard

		scoreboard.teams.forEach { team ->
			if (GameRunner.uhc.usingBot) GameRunner.bot?.destroyTeam(team) {}
			team.unregister()
		}

		GameRunner.sendGameMessage(sender, "Cleared all teams")
	}

	@CommandAlias("team add")
	@Description("add a player to a team")
	fun addPlayerToTeamCommand(sender : CommandSender, teamColor : ChatColor, player : OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		/* make sure no kek team colors */
		if (!TeamData.isValidColor(teamColor)) {
			Commands.errorMessage(sender, "Invalid team color!")
			return
		}

		/* apparently players can not have names */
		val playerName = player.name ?: return Commands.errorMessage(sender, "Player doesn't exist!")

		TeamData.addToTeam(sender.server.scoreboardManager.mainScoreboard, teamColor, playerName)

		GameRunner.sendGameMessage(sender, "${ChatColor.RESET}Added ${player.name} to ${teamColor}${TeamData.prettyTeamName(teamColor)}")
	}

	@CommandAlias("team random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		doRandomTeams(sender, teamSize)
	}

	@CommandAlias("team random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender) {
		doRandomTeams(sender, 1)
	}

	@CommandAlias("team swap")
	@Description("swap the teams of two players")
	fun swapTemas(sender: CommandSender, player1: Player, player2: Player) {
		val team1Color = GameRunner.playersTeam(player1.name)?.color ?: return Commands.errorMessage(sender, "${player1.name} is not on a team!")
		val team2Color = GameRunner.playersTeam(player2.name)?.color ?: return Commands.errorMessage(sender, "${player2.name} is not on a team!")

		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

		TeamData.addToTeam(scoreboard, team2Color, player1.name)
		TeamData.addToTeam(scoreboard, team1Color, player2.name)

		GameRunner.sendGameMessage(sender, "${ChatColor.RESET}${team2Color}${player1.name} ${ChatColor.RESET}${ChatColor.GOLD}${ChatColor.BOLD}and ${team1Color}${player2.name} ${ChatColor.RESET}${ChatColor.GOLD}${ChatColor.BOLD}sucessfully swapped teams!")
	}

	private fun doRandomTeams(sender: CommandSender, teamSize: Int) {
		if (Commands.opGuard(sender)) return

		val onlinePlayers = sender.server.onlinePlayers
		val scoreboard = sender.server.scoreboardManager.mainScoreboard
		val playerArray = ArrayList<String>()

		onlinePlayers.forEach { player ->
			if (scoreboard.getEntryTeam(player.name) == null)
				playerArray.add(player.name)
		}

		val teams = TeamMaker.getTeamsRandom(playerArray, teamSize)
		val numPreMadeTeams = teams.size

		val teamColors = TeamMaker.getColorList(numPreMadeTeams, scoreboard)
			?: return Commands.errorMessage(sender, "Team Maker could not make enough teams!")

		teams.forEachIndexed { index, playerNames ->
			playerNames.forEach {
				if (it != null) {
					TeamData.addToTeam(scoreboard, teamColors[index], it)
				}
			}
		}

		GameRunner.sendGameMessage(sender, "Created ${teams.size} teams with a team size of ${teamSize}!")
	}

	@CommandAlias("modify mobCoefficient")
	@Description("change the mob spawn cap coefficient")
	fun modifyMobCapCoefficient(sender : CommandSender, coefficient : Double) {
		if (Commands.opGuard(sender)) return

		GameRunner.uhc.mobCapCoefficient = coefficient
	}

	@CommandAlias("modify killBounty")
	@Description("change the reward for killing a team")
	fun setKillBounty(sender : CommandSender, reward : KillReward) {
		if (Commands.opGuard(sender)) return

		GameRunner.uhc.killReward = reward
	}

	@CommandAlias("modify variant")
	@Description("set variant")
	fun setPhase(sender: CommandSender, variant: PhaseVariant) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updateVariant(variant)
		GameRunner.uhc.gui.variantCylers[variant.type.ordinal].updateDisplay()
	}

	@CommandAlias("modify timing")
	@Description("set the length of a phase")
	fun setPhaseLength(sender: CommandSender, type: PhaseType, length: Int) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		if (!type.hasTimer)
			return Commands.errorMessage(sender, "${type.prettyName} does not have a timer")

		GameRunner.uhc.updateTime(type, length)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@CommandAlias("modify startRadius")
	@Description("set the starting radius")
	fun setStartRadius(sender: CommandSender, radius: Double) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updateStartRadius(radius)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@CommandAlias("modify endRadius")
	@Description("set the final radius")
	fun setEndRadius(sender: CommandSender, radius: Double) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updateEndRadius(radius)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@CommandAlias("modify all")
	@Description("set all details of the UHC")
	fun modifyAll(sender: CommandSender, startRadius: Double, endRadius: Double, graceTime: Int, shrinkTime: Int, finalTime: Int, glowingTime: Int) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updatePreset(startRadius, endRadius, graceTime, shrinkTime, finalTime)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	@CommandAlias("preset")
	@Description("set all details of the UHC")
	fun modifyAll(sender: CommandSender, preset: Preset) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updatePreset(preset)
		GameRunner.uhc.gui.presetCycler.updateDisplay()
	}

	fun lateTeamTeleport(sender: CommandSender, player: Player, location: Location, team: Team) {
		player.teleportAsync(location).thenAccept {
			player.gameMode = GameMode.SURVIVAL

			/* make sure the player doesn't die when they get teleported */
			player.fallDistance = 0f
			player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 10, true))

			GameRunner.sendGameMessage(sender, "${player.name} successfully added to team ${ChatColor.RESET}${team.color}${team.displayName}")
		}
	}

	@CommandAlias("addLate")
	@Description("adds a player to the game after it has already started")
	fun addLate(sender: CommandSender, playerName: String, teammateName: String) {
		val player = Bukkit.getPlayer(playerName) ?: return Commands.errorMessage(sender, "Can't find player ${playerName}")
		val teammate = Bukkit.getPlayer(teammateName) ?: return Commands.errorMessage(sender, "Can't find player ${teammateName}")

		if (Commands.opGuard(sender)) return

		if (!GameRunner.uhc.isGameGoing()) return Commands.errorMessage(sender, "Game needs to be going!")

		val joinTeam = GameRunner.playersTeam(teammate.name) ?: return Commands.errorMessage(sender, "${teammate.name} has no team to join!")
		TeamData.addToTeam(Bukkit.getScoreboardManager().mainScoreboard, joinTeam.color, player.name)

		lateTeamTeleport(sender, player, teammate.location, joinTeam)
	}

	@CommandAlias("addLate")
	@Description("adds a player to the game after it has already started")
	fun addLate(sender: CommandSender, playerName: String) {
		val player = Bukkit.getPlayer(playerName) ?: return Commands.errorMessage(sender, "Can't find player ${playerName}")

		if (Commands.opGuard(sender)) return

		if (!GameRunner.uhc.isGameGoing()) return Commands.errorMessage(sender, "Game needs to be going!")

		val world = Bukkit.getWorlds()[0]
		val teleportLocation = GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
			?: return Commands.errorMessage(sender, "No suitible teleport location found!")

		val maxTeams = TeamData.teamColors.size
		val teams = Bukkit.getScoreboardManager().mainScoreboard.teams
		if (teams.size == maxTeams)
			return Commands.errorMessage(sender, "There are already the maximum amount of teams (${maxTeams})")

		/* all available team colors */
		val taken = Array(maxTeams) { false }
		teams.forEach { team ->
			val index = TeamData.teamColorIndices[team.color.ordinal]
			if (index != -1) taken[index] = true
		}

		var colorIndex = (Math.random() * maxTeams).toInt()
		while (taken[colorIndex]) {
			colorIndex = (colorIndex + 1) % maxTeams
		}

		val joinTeam = TeamData.addToTeam(Bukkit.getScoreboardManager().mainScoreboard, TeamData.teamColors[colorIndex], player.name)

		lateTeamTeleport(sender, player, teleportLocation, joinTeam)
	}

	@CommandAlias("test end")
	@Description("Check to see if the game should be over")
	fun testEnd(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		var (remainingTeams, lastRemaining, _) = GameRunner.remainingTeams()

		if (lastRemaining != null || remainingTeams == 0)
			GameRunner.uhc.endUHC(lastRemaining)
	}

	@CommandAlias("test next")
	@Description("Manually go to the next round")
	fun testNext(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		if (GameRunner.uhc.isPhase(PhaseType.WAITING))
			Commands.errorMessage(sender, "In waiting phase, use /start instead")
		else
			GameRunner.uhc.startNextPhase()
	}

	@CommandAlias("test gravity")
	@Description("change the gravity constant")
	fun testGravity(sender: CommandSender, gravity: Double) {
		LowGravity.gravity = gravity
	}

	@CommandAlias("reset")
	@Description("reset things to the waiting stage")
	fun testReset(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		GameRunner.uhc.startPhase(PhaseType.WAITING)
	}

	@CommandAlias("test insomnia")
	@Description("get the insomnia of the sender")
	fun testExhaustion(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player
		sender.sendMessage("${sender.name}'s insomnia: ${sender.getStatistic(Statistic.TIME_SINCE_REST)}")
	}

	@CommandAlias("test blockFix")
	@Description("gets when the next apple will drop for you")
	fun testBlockFix(sender: CommandSender, blockFixType: BlockFixType) {
		if (Commands.opGuard(sender)) return
		sender as Player

		blockFixType.blockFix.getInfoString(sender) { info ->
			GameRunner.sendGameMessage(sender, info)
		}
	}

	@CommandAlias("test elapsed")
	@Description("gets how long this UHC has been going for")
	fun testElapsed(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player

		GameRunner.sendGameMessage(sender, "Elapsed time: ${GameRunner.uhc.elapsedTime}")
	}
}
