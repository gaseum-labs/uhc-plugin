package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import net.md_5.bungee.api.ChatColor as ChatColorBungee
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

@CommandAlias("uhc")
class ParticipantCommands : BaseCommand() {

	@CommandAlias("setup")
	@Description("get the current setup")
	fun getCurrentSetup(sender: CommandSender) {
		val player = Bukkit.getPlayer(sender.name)!!
		GameRunner.sendPlayer(player, "Starting radius : " + GameRunner.uhc.startRadius.toInt() + " blocks")
		GameRunner.sendPlayer(player, "Ending radius : " + GameRunner.uhc.endRadius.toInt() + " blocks")
		GameRunner.sendPlayer(player, "Nether closes after shrinking : " + GameRunner.uhc.netherToZero)
		GameRunner.sendPlayer(player, "Spawn cap coefficient : " + GameRunner.uhc.mobCapCoefficient)
		GameRunner.sendPlayer(player, "Team Kill Bounty : " + GameRunner.uhc.killReward)
		GameRunner.sendPlayer(player, "----------------PHASES----------------")
		GameRunner.sendPlayer(player, "Grace period variant : " + GameRunner.uhc.graceType + " " + GameRunner.uhc.phaseDurations[0] + " seconds")
		GameRunner.sendPlayer(player, "Shrinking period variant : " + GameRunner.uhc.shrinkType + " " + GameRunner.uhc.phaseDurations[1] + " seconds")
		GameRunner.sendPlayer(player, "Final zone variant : " + GameRunner.uhc.finalType + " " + GameRunner.uhc.phaseDurations[2] + " seconds")
		GameRunner.sendPlayer(player, "Glowing period variant : " + GameRunner.uhc.glowType + " " + GameRunner.uhc.phaseDurations[3] + " seconds")
		GameRunner.sendPlayer(player, "Endgame variant : " + GameRunner.uhc.endgameType)
	}

	@CommandAlias("name")
	@Description("change the name of your team")
	fun teamName(sender: CommandSender, newName : String) {
		val team = GameRunner.playersTeam(sender.name)
		if (team != null) {
			team.displayName = newName
			for (entry in team.entries) {
				val player = Bukkit.getPlayer(entry)
				val introComp = TextComponent("Team name is now ")
				introComp.color = ChatColorBungee.GOLD
				val teamName = TextComponent(newName)
				teamName.color = team.color.asBungee()
				teamName.isBold = true
				player?.sendMessage(introComp, teamName)
			}
		} else {
			sender.sendMessage("you are not on a team")
		}
	}

	@CommandAlias("color")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color : ChatColor) {
		if (color == ChatColor.WHITE || color == ChatColor.GOLD) {
			val comp = TextComponent("that color is not allowed")
			comp.color = ChatColorBungee.RED
			sender.sendMessage(comp)
			return
		}
		for (team in Bukkit.getServer().scoreboardManager.mainScoreboard.teams) {
			if (team.color.equals(color)) {
				val comp = TextComponent("that color is already being used by another team")
				comp.color = ChatColorBungee.RED
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

	@CommandAlias("mobcaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		val player = sender.server.getPlayer(sender.name)!!
		GameRunner.sendPlayer(player, "Monster spawn limit: " + player.world.monsterSpawnLimit)
		GameRunner.sendPlayer(player, "Animal spawn limit: " + player.world.animalSpawnLimit)
		GameRunner.sendPlayer(player, "Ambient spawn limit: " + player.world.ambientSpawnLimit)
		GameRunner.sendPlayer(player, "Water animal spawn limit: " + player.world.waterAnimalSpawnLimit)
	}

	@CommandAlias("help")
	@Description("display a list of commands and how to use them")
	fun help(sender: CommandSender) {

	}
}
