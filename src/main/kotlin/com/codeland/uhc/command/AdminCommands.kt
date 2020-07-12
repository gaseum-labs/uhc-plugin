package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.phaseType.*
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {

	/* COMMANDS */

	@CommandAlias("start")
	@Description("start the UHC")
	fun startGame(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		val errMessage = GameRunner.uhc.startUHC(sender)

		if (errMessage != null)
			Commands.errorMessage(sender, errMessage)
	}

	@CommandAlias("team clear")
	@Description("remove all current teams")
	fun clearTeams(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		val scoreboard = sender.server.scoreboardManager.mainScoreboard

		scoreboard.teams.forEach {
			GameRunner.bot.destroyTeam(it)
			it.unregister()
		}
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
		val playerName = player.name ?: return Commands.errorMessage(sender, "Player doesn't exist!");

		TeamData.addToTeam(sender.server.scoreboardManager.mainScoreboard, teamColor, playerName);
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

	private fun doRandomTeams(sender: CommandSender, teamSize: Int) {
		if (Commands.opGuard(sender)) return;

		val onlinePlayers = sender.server.onlinePlayers
		val scoreboard = sender.server.scoreboardManager.mainScoreboard
		val playerArray = ArrayList<String>()

		onlinePlayers.forEach {
			if (scoreboard.getEntryTeam(it.name) == null) {
				playerArray.add(it.name)
			}
		}

		val teams = TeamMaker.getTeamsRandom(playerArray, teamSize)
		val numPreMadeTeams = teams.size

		val teamColors = TeamMaker.getColorList(numPreMadeTeams, scoreboard)
				?: return Commands.errorMessage(sender, "Team Maker could not make enough teams!");

		teams.forEachIndexed { index, playerNames ->
			playerNames.forEach {
				if (it != null) {
					TeamData.addToTeam(scoreboard, teamColors[index], it)
				}
			}
		}
	}

	@CommandAlias("modify mobCoefficient")
	@Description("change the mob spawn cap coefficient")
	fun modifyMobCapCoefficient(sender : CommandSender, coefficient : Double) {
		if (Commands.opGuard(sender)) return

		for (w in Bukkit.getServer().worlds) {
			w.monsterSpawnLimit = (w.monsterSpawnLimit * (coefficient / GameRunner.uhc.mobCapCoefficient)).toInt()
			w.animalSpawnLimit = (w.animalSpawnLimit * (coefficient / GameRunner.uhc.mobCapCoefficient)).toInt()
			w.ambientSpawnLimit = (w.ambientSpawnLimit * (coefficient / GameRunner.uhc.mobCapCoefficient)).toInt()
			w.waterAnimalSpawnLimit = (w.waterAnimalSpawnLimit * (coefficient / GameRunner.uhc.mobCapCoefficient)).toInt()
		}
		GameRunner.uhc.mobCapCoefficient = coefficient
	}

	@CommandAlias("modify netherCloses")
	@Description("specify how the nether ends")
	fun setNetherSolution(sender : CommandSender, netherCloses : Boolean) {
		if (Commands.opGuard(sender)) return

		GameRunner.uhc.netherToZero = netherCloses
	}

	@CommandAlias("modify killBounty")
	@Description("change the reward for killing a team")
	fun setKillBounty(sender : CommandSender, reward : KillReward) {
		if (Commands.opGuard(sender)) return

		GameRunner.uhc.killReward = reward
	}

	@CommandAlias("modify variant")
	@Description("set variant")
	fun setPhase(sender: CommandSender, factory: PhaseVariant) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.setVariant(factory)
	}

	@CommandAlias("modify timing")
	@Description("set the length of a phase")
	fun setPhaseLength(sender: CommandSender, type: PhaseType, length: Long) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		if (!type.hasTimer)
			return Commands.errorMessage(sender, "${type.prettyName} does not have a timer")

		GameRunner.uhc.phaseTimes[type.ordinal] = length
	}

	@CommandAlias("modify startRadius")
	@Description("set the starting radius")
	fun setStartRadius(sender : CommandSender, radius : Double) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.preset.startRadius = radius
	}

	@CommandAlias("modify endRadius")
	@Description("set the final radius")
	fun setEndRadius(sender : CommandSender, radius : Double) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.preset.endRadius = radius
	}

	@CommandAlias("modify all")
	@Description("set all details of the UHC")
	fun modifyAll(sender : CommandSender, startRadius: Double, endRadius: Double, graceTime: Long, shrinkTime: Long, finalTime: Long, glowingTime: Long) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.preset.startRadius = startRadius
		GameRunner.uhc.preset.endRadius = endRadius

		val phaseTimes = GameRunner.uhc.phaseTimes

		phaseTimes[PhaseType.GRACE.ordinal] = graceTime
		phaseTimes[PhaseType.SHRINK.ordinal] = shrinkTime
		phaseTimes[PhaseType.FINAL.ordinal] = finalTime
		phaseTimes[PhaseType.GLOWING.ordinal] = glowingTime
	}

	@CommandAlias("preset")
	@Description("set all details of the UHC")
	fun modifyAll(sender : CommandSender, preset: Preset) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.updatePreset(preset)
	}

	@CommandAlias("test end")
	@Description("Check to see if the game should be over")
	fun testEnd(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		var (remainingTeams, lastRemaining, _) = GameRunner.remainingTeams()

		if (lastRemaining != null || remainingTeams == 0)
			GameRunner.uhc.endUHC(lastRemaining)
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
		sender as Player
		sender.sendMessage("${sender.name}'s insomnia: ${sender.getStatistic(Statistic.TIME_SINCE_REST)}")
	}
}