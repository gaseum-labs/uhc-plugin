package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.HelpCommand
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phaseType.PhaseType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uhc")
class ParticipantCommands : BaseCommand() {

	fun phaseString(phaseType: PhaseType): String {
		val variant = GameRunner.uhc.getVariant(phaseType)

		val ret = "${phaseType.prettyName} variant : ${variant.prettyName}"

		val time = GameRunner.uhc.getTime(phaseType)

		return if (time == 0)
			ret
		else
			"$ret | $time seconds"
	}

	@CommandAlias("setup")
	@Description("get the current setup")
	fun getCurrentSetup(sender: CommandSender) {
		sender as Player
		val uhc = GameRunner.uhc

		GameRunner.sendPlayer(sender, "Starting radius : ${uhc.startRadius.toInt()} blocks")
		GameRunner.sendPlayer(sender, "Ending radius : ${uhc.endRadius.toInt()} blocks")
		GameRunner.sendPlayer(sender, "Nether closes after shrinking : ${uhc.netherToZero}")
		GameRunner.sendPlayer(sender, "Spawn cap coefficient : ${uhc.mobCapCoefficient}")
		GameRunner.sendPlayer(sender, "Team Kill Bounty : ${uhc.killReward}")

		GameRunner.sendPlayer(sender, "----------------PHASES----------------")

		GameRunner.sendPlayer(sender, phaseString(PhaseType.WAITING))
		GameRunner.sendPlayer(sender, phaseString(PhaseType.GRACE))
		GameRunner.sendPlayer(sender, phaseString(PhaseType.SHRINK))
		GameRunner.sendPlayer(sender, phaseString(PhaseType.FINAL))
		GameRunner.sendPlayer(sender, phaseString(PhaseType.GLOWING))
		GameRunner.sendPlayer(sender, phaseString(PhaseType.ENDGAME))
		GameRunner.sendPlayer(sender, phaseString(PhaseType.POSTGAME))
	}

	@CommandAlias("gui")
	@Description("get the current setup as the gui")
	fun getCurrentSetupGui(sender: CommandSender) {
		sender as Player
		val uhc = GameRunner.uhc

		Gui.open(sender)
	}

	@CommandAlias("spectate")
	@Description("start spectating")
	fun startSpecting(sender: CommandSender) {
		sender as Player

		if (sender.gameMode != GameMode.SPECTATOR) {
			val team = GameRunner.playersTeam(sender.name)

			if (team != null) {
				TeamData.removeFromTeam(team, sender.name)
			}

			if (!GameRunner.uhc.isPhase(PhaseType.POSTGAME) && !GameRunner.uhc.isPhase(PhaseType.WAITING)) {
				sender.gameMode = GameMode.SPECTATOR

				GameRunner.playerDeath(sender)
			}
		}
	}

	@CommandAlias("name")
	@Description("change the name of your team")
	fun teamName(sender: CommandSender, newName: String) {
		val team = GameRunner.playersTeam(sender.name)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		var realNewName = newName
		if (realNewName.startsWith('"') && realNewName.endsWith('"') && realNewName.length > 1) {
			realNewName = realNewName.substring(1, realNewName.length - 1)
		}

		GameRunner.bot?.renameTeam(team, realNewName) {}

		team.displayName = realNewName

		val message = TextComponent("Your team name has been changed to \"$realNewName\"")
		message.color = team.color.asBungee()
		message.isBold = true

		/* broadcast change to all teammates */
		team.entries.forEach { entry ->
			Bukkit.getServer().getPlayer(entry)?.sendMessage(message)
		}
	}

	@CommandAlias("color")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color: ChatColor) {
		val team = GameRunner.playersTeam(sender.name)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		if (!TeamData.isValidColor(color))
			return Commands.errorMessage(sender, "That color is not allowed!")

		if (Bukkit.getServer().scoreboardManager.mainScoreboard.teams.any { team ->
				return@any team.color == color
		})
			return Commands.errorMessage(sender, "That color is already being used by another team!")

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

	@CommandAlias("mobcaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		sender as Player

		GameRunner.sendPlayer(sender, "Monster spawn limit: " + sender.world.monsterSpawnLimit)
		GameRunner.sendPlayer(sender, "Animal spawn limit: " + sender.world.animalSpawnLimit)
		GameRunner.sendPlayer(sender, "Ambient spawn limit: " + sender.world.ambientSpawnLimit)
		GameRunner.sendPlayer(sender, "Water animal spawn limit: " + sender.world.waterAnimalSpawnLimit)
	}

	@CommandAlias("help")
	@Description("display a list of commands and how to use them")
	@HelpCommand("bruh what?")
	fun help(sender: CommandSender) {
		sender as Player

		GameRunner.sendPlayer(sender, "Commands:")

		this.registeredCommands.forEach { command ->
			GameRunner.sendPlayer(sender, "${command.getSyntaxText()} > ${command.getHelpText()}")
		}
	}
}
