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
	fun teamName(sender: CommandSender, newName: String) {
		val team = GameRunner.playersTeam(sender.name)
			?: return TeamData.errorMessage(sender, "You are not on a team!")

		team.displayName = newName

		val message = TextComponent("Your team name has been changed to ${newName}")
		message.color = team.color.asBungee();
		message.isBold = true;

		/* broadcast change to all teammates */
		team.entries.forEach { entry ->
			Bukkit.getServer().getPlayer(entry)?.sendMessage(message)
		}
	}

	@CommandAlias("color")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color: ChatColor) {
		val team = GameRunner.playersTeam(sender.name)
			?: return TeamData.errorMessage(sender, "You are not on a team!")

		if (!TeamData.isValidColor(color))
			return TeamData.errorMessage(sender, "That color is not allowed!")

		if (Bukkit.getServer().scoreboardManager.mainScoreboard.teams.any { team ->
				return@any team.color == color
		})
			return TeamData.errorMessage(sender, "That color is already being used by another team!")

		/* now finally change color */
		team.color = color

		val message = TextComponent("Your team color has been changed to ${TeamData.colorPrettyNames[color.ordinal].toLowerCase()}")
		message.color = color.asBungee()
		message.isBold = true

		/* broadcast change to all teammates */
		team.entries.forEach { entry ->
			Bukkit.getServer().getPlayer(entry)?.sendMessage(message)
		}
	}

	/*@CommandAlias("style")
	@Description("change your team name's display style")
	fun teamStyle(sender: CommandSender, style : ChatColor) {
		val team = GameRunner.playersTeam(sender.name)
			?: return TeamData.errorMessage(sender, "You are not on a team!")

		if (!style.isFormat)
			return TeamData.errorMessage(sender, "Please choose a style and not a color!")

		/* now finally change style (badly) */
		team.displayName = "${ChatColor.COLOR_CHAR}${style.char}${team.displayName}"

		val message = TextComponent("Your team style has been changed to ${TeamData.colorPrettyNames[style.ordinal].toLowerCase()}")
		message.color = team.color.asBungee()
		message.isBold = style == ChatColor.BOLD
		message.isItalic = style == ChatColor.ITALIC
		message.isObfuscated = style == ChatColor.MAGIC
		message.isUnderlined = style == ChatColor.UNDERLINE
		message.isStrikethrough = style == ChatColor.STRIKETHROUGH

		/* broadcast change to all teammates */
		team.entries.forEach { entry ->
			Bukkit.getServer().getPlayer(entry)?.sendMessage(message)
		}
	}*/

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
