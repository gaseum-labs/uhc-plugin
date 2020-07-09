package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.*
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.scoreboard.Team

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {

	/* COMMANDS */

	@CommandAlias("start")
	@Description("start the UHC")
	fun startGame(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		GameRunner.startGame(sender)
	}

	@CommandAlias("team clear")
	@Description("remove all current teams")
	fun clearTeams(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		val scoreboard = sender.server.scoreboardManager.mainScoreboard

		scoreboard.teams.forEach {
			GameRunner.discordBot?.destroyTeam(it)
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

	@CommandAlias("modify phase variant")
	@Description("set variant")
	fun setPhase(sender: CommandSender, factory: PhaseFactory) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.phaseFactories[factory.type.ordinal] = factory
	}

	@CommandAlias("modify phase length")
	@Description("set the length of a phase")
	fun setPhaseLength(sender: CommandSender, type: PhaseType, length: Long) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		if (!type.hasTimer)
			return Commands.errorMessage(sender, "${type.prettyName} does not have a timer")

		GameRunner.uhc.setTiming(type, length)
	}

	@CommandAlias("modify radius start")
	@Description("set the starting radius")
	fun setStartRadius(sender : CommandSender, radius : Double) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.startRadius = radius
	}

	@CommandAlias("modify radius end")
	@Description("set the final radius")
	fun setEndRadius(sender : CommandSender, radius : Double) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.endRadius = radius
	}

	@CommandAlias("modify all")
	@Description("set all details of the UHC")
	fun modifyAll(sender : CommandSender, startRadius: Double, endRadius: Double, graceTime: Long, shrinkTime: Long, finalTime: Long, glowingTime: Long) {
		if (Commands.opGuard(sender)) return
		if (Commands.waitGuard(sender)) return

		GameRunner.uhc.startRadius = startRadius
		GameRunner.uhc.endRadius = endRadius

		GameRunner.uhc.setTiming(PhaseType.GRACE, graceTime)
		GameRunner.uhc.setTiming(PhaseType.SHRINK, shrinkTime)
		GameRunner.uhc.setTiming(PhaseType.FINAL, finalTime)
		GameRunner.uhc.setTiming(PhaseType.GLOWING, glowingTime)
	}

	@CommandAlias("test end")
	@Description("Check to see if the game should be over")
	fun testEnd(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		if (GameRunner.quickRemainingTeams() == 1) {
			var aliveTeam : Team? = null
			for (team in Bukkit.getServer().scoreboardManager.mainScoreboard.teams) {
				for (entry in team.entries) {
					val player = Bukkit.getPlayer(entry)
					if (player != null && player.gameMode == GameMode.SURVIVAL) {
						aliveTeam = team
						break
					}
				}
				if (aliveTeam != null) {
					break
				}
			}
			if (aliveTeam != null) {
				GameRunner.endUHC(aliveTeam)
			}
		}
	}

	@CommandAlias("test reset")
	@Description("reset things to the waiting stage")
	fun testReset(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		for (world in Bukkit.getServer().worlds) {
			world.setSpawnLocation(10000, 70, 10000)
			world.worldBorder.setCenter(10000.0, 10000.0)
			world.worldBorder.size = 50.0
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
			world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
			world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false) // could cause issue with dynamic spawn limit if true
			world.time = 1000
			world.difficulty = Difficulty.NORMAL
		}

		for (player in Bukkit.getServer().onlinePlayers) {
			player.exp = 0.0F
			player.health = 20.0
			player.location.set(10000.0, 100.0, 10000.0)
			player.gameMode = GameMode.ADVENTURE
		}

		GameRunner.uhc.startPhase(PhaseType.WAITING)
	}
}