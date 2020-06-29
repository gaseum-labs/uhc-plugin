package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import net.md_5.bungee.api.ChatColor as ChatColorBungee
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.scoreboard.Team

@CommandAlias("uhc")
class CommandSetup : BaseCommand() {

	private val gameRunner = GameRunner
	private val teamColours : Array<ChatColor> = arrayOf(ChatColor.BLUE, ChatColor.RED, ChatColor.GREEN, ChatColor.AQUA, ChatColor.DARK_RED, ChatColor.DARK_AQUA, ChatColor.GRAY, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN)

	@CommandAlias("UHCsetup")
	@Description("Setup the UHC round")
	fun onSetup(sender: CommandSender, startRadius: Double, endRadius: Double, graceTime: Double, shrinkTime: Double, glowTime : Double): Boolean {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return false
		}
		val uhc = UHC(startRadius, endRadius, graceTime, shrinkTime, glowTime)
		gameRunner.setUhc(uhc)

		Bukkit.getServer().dispatchCommand(sender, "scoreboard objectives add hp health")
		Bukkit.getServer().dispatchCommand(sender, "scoreboard objectives setdisplay list hp")

		var textComponent = TextComponent("UHC has been setup")
		textComponent.isBold = true
		textComponent.color = ChatColorBungee.GOLD
		for (onlinePlayer in Bukkit.getServer().onlinePlayers) {
			onlinePlayer.sendMessage(textComponent)
			textComponent = TextComponent("Starting radius : " + startRadius.toInt() + " blocks")
			textComponent.isBold = true
			textComponent.color = ChatColorBungee.GOLD
			onlinePlayer.sendMessage(textComponent)
			textComponent = TextComponent("Ending radius : " + endRadius.toInt() + " blocks")
			textComponent.isBold = true
			textComponent.color = ChatColorBungee.GOLD
			onlinePlayer.sendMessage(textComponent)
			textComponent = TextComponent("Grace period : " + graceTime.toInt() + " seconds")
			textComponent.isBold = true
			textComponent.color = ChatColorBungee.GOLD
			onlinePlayer.sendMessage(textComponent)
			textComponent = TextComponent("Shrinking period : " + shrinkTime.toInt() + " seconds")
			textComponent.isBold = true
			textComponent.color = ChatColorBungee.GOLD
			onlinePlayer.sendMessage(textComponent)
			textComponent = TextComponent("Time until Glowing : " + glowTime.toInt() + " seconds")
			textComponent.isBold = true
			textComponent.color = ChatColorBungee.GOLD
			onlinePlayer.sendMessage(textComponent)
		}

		return true
	}

	@CommandAlias("UHCStart")
	@Description("start")
	fun startGame(sender : CommandSender) {
		if (!sender.isOp) {
			val msg = TextComponent("You must be a server operator to use this command.")
			msg.color = net.md_5.bungee.api.ChatColor.RED
			sender.sendMessage(msg)
			return
		}
		gameRunner.startGame(sender, sender.server.worlds[0])
	}

	@CommandAlias("UHCClearTeams")
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

	@CommandAlias("UHCCreateTeam")
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

	@CommandAlias("UHCAddToTeam")
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

	@CommandAlias("UHCRandomTeams")
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
		var playerArray = ArrayList<String>()
		onlinePlayers.forEach {
			if (scoreboard.getEntryTeam(it.name) == null) {
				playerArray.add(it.name)
			}
		}
		var teams = TeamMaker().getTeamsRandom(playerArray, teamSize)
		var numPreMadeTeams = scoreboard.teams.size
		teams.forEachIndexed { index, players ->
			val teamName = teamColours[numPreMadeTeams + index].name + " Team"
			createTeam(sender, teamName)
			players.forEach {
				if (it != null) {
					scoreboard.getTeam(teamName)!!.addEntry(it)
				}
			}
		}
	}

	@CommandAlias("teamName")
	@Description("change the name of your team")
	fun teamName(sender: CommandSender, newName : String) {
		val team = gameRunner.playersTeam(sender.name)
		if (team != null) {
			team.displayName = newName
			for (entry in team.entries) {
				val player = Bukkit.getPlayer(entry)
				val introComp = TextComponent("Team name is now ")
				introComp.color = net.md_5.bungee.api.ChatColor.GOLD
				val teamName = TextComponent(newName)
				teamName.color = team.color.asBungee()
				teamName.isBold = true
				player?.sendMessage(introComp, teamName)
			}
		} else {
			sender.sendMessage("you are not on a team")
		}
	}

	@CommandAlias("getMobCaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		var textComponent = TextComponent("Monster spawn limit: " + sender.server.worlds[0].monsterSpawnLimit)
		textComponent.color = ChatColorBungee.GOLD
		sender.sendMessage(textComponent)
		textComponent = TextComponent("Animal spawn limit: " + sender.server.worlds[0].animalSpawnLimit)
		textComponent.color = ChatColorBungee.GOLD
		sender.sendMessage(textComponent)
		textComponent = TextComponent("Ambient spawn limit: " + sender.server.worlds[0].ambientSpawnLimit)
		textComponent.color = ChatColorBungee.GOLD
		sender.sendMessage(textComponent)
		textComponent = TextComponent("Water animal spawn limit: " + sender.server.worlds[0].waterAnimalSpawnLimit)
		textComponent.color = ChatColorBungee.GOLD
		sender.sendMessage(textComponent)
	}

	@CommandAlias("UHCSetGlowingMode")
	@Description("set the type of behavior of glowing, 0 is old, 1 is new")
	fun setGlowingMode(sender: CommandSender, mode : Int) {
		GameRunner.setGlowingMode(mode)
	}

	@CommandAlias("teamColor")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color : ChatColor) {
		if (color == ChatColor.WHITE || color == ChatColor.GOLD) {
			val comp = TextComponent("that color is not allowed")
			comp.color = ChatColor.RED.asBungee()
			sender.sendMessage(comp)
			return
		}
		for (team in Bukkit.getServer().scoreboardManager.mainScoreboard.teams) {
			if (team.color.equals(color)) {
				val comp = TextComponent("that color is already being used by another team")
				comp.color = net.md_5.bungee.api.ChatColor.RED
				sender.sendMessage(comp)
				return
			}
		}
		val team = GameRunner.playersTeam(sender.name)
		if (team != null) {
			team.color = color
			val comp = TextComponent("Your team color has been changed")
			comp.color = color.asBungee()
			for (entry in team.entries) {
				Bukkit.getServer().getPlayer(entry)!!.sendMessage(comp)
			}
		}
	}

	@CommandAlias("UHCTestEnd")
	@Description("Check to see if the game should be over")
	fun testEnd(sender: CommandSender) {
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
}