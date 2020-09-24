package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.KillReward
import com.codeland.uhc.phase.*
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.quirk.quirks.LowGravity
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.team.TeamMaker
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {

	/* COMMANDS */

	@CommandAlias("start")
	@Description("start the UHC")
	fun startGame(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		GameRunner.sendGameMessage(sender, "Starting UHC...")

		val errMessage = GameRunner.uhc.startUHC(sender)

		if (errMessage != null) Commands.errorMessage(sender, errMessage)
	}

	@CommandAlias("team clear")
	@Description("remove all current teams")
	fun clearTeams(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		TeamData.removeAllTeams()

		GameRunner.sendGameMessage(sender, "Cleared all teams")
	}

	@CommandAlias("team add")
	@Description("add a player to a team")
	fun addPlayerToTeamCommand(sender: CommandSender, index: Int, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val colorPair = TeamData.colorPairFromIndex(index)
			?: return Commands.errorMessage(sender, "Index out of range, should be at least 0 and at most ${TeamData.MAX_TEAMS - 1}")

		/* apparently players can not have names */
		val playerName = player.name ?: return Commands.errorMessage(sender, "Player doesn't exist!")

		val team = TeamData.addToTeam(colorPair, player)

		GameRunner.sendGameMessage(sender, "${ChatColor.RESET}Added ${player.name} to team ${colorPair.colorString(team.displayName)}")
	}

	@CommandAlias("team random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		doRandomTeams(sender, teamSize)
	}

	@CommandAlias("team swap")
	@Description("swap the teams of two players")
	fun swapTemas(sender: CommandSender, player1: OfflinePlayer, player2: OfflinePlayer) {
		val team1 = TeamData.playersTeam(player1) ?: return Commands.errorMessage(sender, "${player1.name} is not on a team!")
		val team2 = TeamData.playersTeam(player2) ?: return Commands.errorMessage(sender, "${player2.name} is not on a team!")

		TeamData.addToTeam(team2, player1)
		TeamData.addToTeam(team1, player2)

		GameRunner.sendGameMessage(sender, "${team2.colorPair.colorString(player1.name ?: "unknown")} ${ChatColor.GOLD}${ChatColor.BOLD}and ${team1.colorPair.colorString(player2.name ?: "unknown")} ${ChatColor.GOLD}${ChatColor.BOLD}sucessfully swapped teams!")
	}

	private fun doRandomTeams(sender: CommandSender, teamSize: Int) {
		if (Commands.opGuard(sender)) return

		val onlinePlayers = sender.server.onlinePlayers
		val playerArray = ArrayList<OfflinePlayer>()

		onlinePlayers.forEach { player ->
			if (TeamData.playersTeam(player) == null)
				playerArray.add(player)
		}

		val teams = TeamMaker.getTeamsRandom(playerArray, teamSize)
		val numPreMadeTeams = teams.size

		val teamColorPairs = TeamMaker.getColorList(numPreMadeTeams)
			?: return Commands.errorMessage(sender, "Team Maker could not make enough teams!")

		teams.forEachIndexed { index, players ->
			players.forEach { player ->
				if (player != null) TeamData.addToTeam(teamColorPairs[index], player)
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

	@CommandAlias("setLength")
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
	fun modifyAll(sender: CommandSender, startRadius: Double, endRadius: Double, graceTime: Int, shrinkTime: Int) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return

		GameRunner.uhc.updatePreset(startRadius, endRadius, graceTime, shrinkTime)
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

			GameRunner.sendGameMessage(sender, "${player.name} successfully added to team ${team.colorPair.colorString(team.displayName)}")
		}
	}

	@CommandAlias("addLate")
	@Description("adds a player to the game after it has already started")
	fun addLate(sender: CommandSender, playerName: String, teammateName: String) {
		val player = Bukkit.getPlayer(playerName) ?: return Commands.errorMessage(sender, "Can't find player $playerName")
		val teammate = Bukkit.getPlayer(teammateName) ?: return Commands.errorMessage(sender, "Can't find player $teammateName")

		if (Commands.opGuard(sender)) return

		if (!GameRunner.uhc.isGameGoing()) return Commands.errorMessage(sender, "Game needs to be going!")

		val joinTeam = TeamData.playersTeam(teammate) ?: return Commands.errorMessage(sender, "${teammate.name} has no team to join!")
		TeamData.addToTeam(joinTeam, player)

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

		var teamColorPairs = TeamMaker.getColorList(1) ?: return Commands.errorMessage(sender, "There are already the maximum amount of teams (${TeamData.MAX_TEAMS})")

		val joinTeam = TeamData.addToTeam(teamColorPairs[0], player)

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

	@CommandAlias("test teams")
	@Description("gives an overview of teams")
	fun testTeams(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val teams = TeamData.teams

		teams.forEach { team ->
			GameRunner.sendGameMessage(sender, team.colorPair.colorString(team.displayName))
			team.members.forEach { member ->
				GameRunner.sendGameMessage(sender, team.colorPair.colorString(member.name ?: "unknown"))
			}
		}
	}
}
