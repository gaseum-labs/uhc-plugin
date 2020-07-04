package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.*
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.scoreboard.Team

@CommandAlias("uhca")
class AdminCommands : BaseCommand() {

	private val gameRunner = GameRunner
	private val teamColours : Array<ChatColor> = arrayOf(
			ChatColor.BLUE,
			ChatColor.RED,
			ChatColor.GREEN,
			ChatColor.AQUA,
			ChatColor.LIGHT_PURPLE,
			ChatColor.YELLOW,
			ChatColor.DARK_RED,
			ChatColor.DARK_AQUA,
			ChatColor.DARK_PURPLE,
			ChatColor.GRAY,
			ChatColor.DARK_BLUE,
			ChatColor.DARK_GREEN,
			ChatColor.DARK_GRAY,
			ChatColor.BLACK
	)

	@CommandAlias("start")
	@Description("start the UHC")
	fun startGame(sender : CommandSender) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		gameRunner.startGame(sender)
	}

	@CommandAlias("team clear")
	@Description("remove all current teams")
	fun clearTeams(sender : CommandSender) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		val scoreboard = sender.server.scoreboardManager.mainScoreboard
		scoreboard.teams.forEach {
			it.unregister()
		}
	}

	@CommandAlias("team create")
	@Description("create a new team")
	fun createTeam(sender : CommandSender, teamName : String) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		var team = sender.server.scoreboardManager.mainScoreboard.registerNewTeam(teamName)
		team.color = teamColours[(sender.server.scoreboardManager.mainScoreboard.teams.size - 1) % teamColours.size]
	}

	@CommandAlias("team add")
	@Description("add a player to a team")
	fun addPlayerToTeam(sender : CommandSender, teamName : String, player : OfflinePlayer) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		sender.server.scoreboardManager.mainScoreboard.getEntryTeam(player.name!!)?.removeEntry(player.name!!)
		sender.server.scoreboardManager.mainScoreboard.getTeam(teamName)?.addEntry(player.name!!)
	}

	@CommandAlias("team random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		val onlinePlayers = sender.server.onlinePlayers
		val scoreboard = sender.server.scoreboardManager.mainScoreboard
		val playerArray = ArrayList<String>()
		onlinePlayers.forEach {
			if (scoreboard.getEntryTeam(it.name) == null) {
				playerArray.add(it.name)
			}
		}
		val teams = TeamMaker().getTeamsRandom(playerArray, teamSize)
		val numPreMadeTeams = scoreboard.teams.size
		teams.forEachIndexed { index, players ->
			val teamName = "team" + (numPreMadeTeams + index)
			createTeam(sender, teamName)
			val team = scoreboard.getTeam(teamName)!!
			team.displayName = teamColours[numPreMadeTeams + index].name + " Team"
			players.forEach {
				if (it != null) {
					team.addEntry(it)
				}
			}
		}
	}

	@CommandAlias("modify phase variant grace")
	@Description("set grace period variant")
	fun setGlowingMode(sender : CommandSender, type : GraceType) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.graceType = type
	}

	@CommandAlias("modify phase variant shrink")
	@Description("set shrink phase variant")
	fun setShrinkMode(sender : CommandSender, type : ShrinkType) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.shrinkType = type
	}

	@CommandAlias("modify phase variant waiting")
	@Description("set waiting phase variant")
	fun setShrinkMode(sender : CommandSender, type : FinalType) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.finalType = type
	}

	@CommandAlias("modify phase variant glowing")
	@Description("set glowing phase variant")
	fun setShrinkMode(sender : CommandSender, type : GlowType) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.glowType = type
	}

	@CommandAlias("modify phase variant endgame")
	@Description("set endgame phase variant")
	fun setShrinkMode(sender : CommandSender, type : EndgameType) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.endgameType = type
	}

	@CommandAlias("modify phase length")
	@Description("set the length of a phase")
	fun setPhaseLength(sender : CommandSender, type : UHCPhase, length : Long) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		if (type == UHCPhase.GRACE) {
			GameRunner.uhc.phaseDurations[0] = length
		} else if (type == UHCPhase.SHRINKING) {
			GameRunner.uhc.phaseDurations[1] = length
		} else if (type == UHCPhase.WAITING) {
			GameRunner.uhc.phaseDurations[2] = length
		} else if (type == UHCPhase.GLOWING) {
			GameRunner.uhc.phaseDurations[3] = length
		}
	}

	@CommandAlias("modify radius start")
	@Description("set the starting radius")
	fun setStartRadius(sender : CommandSender, radius : Double) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.startRadius = radius
	}

	@CommandAlias("modify radius end")
	@Description("set the final radius")
	fun setEndRadius(sender : CommandSender, radius : Double) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.endRadius = radius
	}

	@CommandAlias("modify mobCoefficient")
	@Description("change the mob spawn cap coefficient")
	fun modifyMobCapCoefficient(sender : CommandSender, coefficient : Double) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
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
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.netherToZero = netherCloses
	}

	@CommandAlias("modify killBounty")
	@Description("change the reward for killing a team")
	fun setKillBounty(sender : CommandSender, reward : KillReward) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.killReward = reward
	}

	@CommandAlias("modify verbose")
	@Description("set all details of the UHC")
	fun modifyVerbose(sender : CommandSender, startRadius: Double, endRadius: Double, graceTime: Long, shrinkTime: Long, finalTime : Long) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		GameRunner.uhc.startRadius = startRadius
		GameRunner.uhc.endRadius = endRadius
		GameRunner.uhc.phaseDurations[0] = graceTime
		GameRunner.uhc.phaseDurations[1] = shrinkTime
		GameRunner.uhc.phaseDurations[2] = finalTime
	}

	@CommandAlias("test end")
	@Description("Check to see if the game should be over")
	fun testEnd(sender : CommandSender) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}

		if (GameRunner.remainingTeams() == 1) {
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
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}

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
		GameRunner.phase = UHCPhase.WAITING
	}

}