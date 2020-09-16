package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.PhaseType
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

		GameRunner.sendGameMessage(sender, "Starting radius : ${uhc.startRadius.toInt()} blocks")
		GameRunner.sendGameMessage(sender, "Ending radius : ${uhc.endRadius.toInt()} blocks")
		GameRunner.sendGameMessage(sender, "Spawn cap coefficient : ${uhc.mobCapCoefficient}")
		GameRunner.sendGameMessage(sender, "Team Kill Bounty : ${uhc.killReward}")

		GameRunner.sendGameMessage(sender, "----------------PHASES----------------")

		GameRunner.sendGameMessage(sender, phaseString(PhaseType.WAITING))
		GameRunner.sendGameMessage(sender, phaseString(PhaseType.GRACE))
		GameRunner.sendGameMessage(sender, phaseString(PhaseType.SHRINK))
		GameRunner.sendGameMessage(sender, phaseString(PhaseType.ENDGAME))
		GameRunner.sendGameMessage(sender, phaseString(PhaseType.POSTGAME))
	}

	@CommandAlias("gui")
	@Description("get the current setup as the gui")
	fun getCurrentSetupGui(sender: CommandSender) {
		GameRunner.uhc.gui.inventory.open(sender as Player)
	}

	@CommandAlias("spectate")
	@Description("start spectating")
	fun startSpecting(sender: CommandSender) {
		sender as Player

		if (sender.gameMode != GameMode.SPECTATOR && GameRunner.uhc.isGameGoing()) {
			sender.gameMode = GameMode.SPECTATOR
			GameRunner.playerDeath(sender, true)
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
	fun teamColor(sender: CommandSender, newColor: ChatColor) {
		val team = GameRunner.playersTeam(sender.name)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		if (!TeamData.isValidColor(newColor))
			return Commands.errorMessage(sender, "That color is not allowed!")

		if (Bukkit.getServer().scoreboardManager.mainScoreboard.teams.any { team ->
				return@any team.color == newColor
		})
			return Commands.errorMessage(sender, "That color is already being used by another team!")

		/* change team name to be default name for new color if no custom name has been set */
		if (team.displayName == TeamData.prettyTeamName(team.color))
			team.displayName = TeamData.prettyTeamName(newColor)

		/* now finally change color */
		team.color = newColor

		/* broadcast change to all teammates */
		team.entries.forEach { entry ->
			Bukkit.getServer().getPlayer(entry)?.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}Your team color has been changed to ${newColor}${TeamData.colorPrettyNames[newColor.ordinal].toLowerCase()}")
		}
	}

	@CommandAlias("mobcaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		sender as Player

		GameRunner.sendGameMessage(sender, "Monster spawn limit: " + sender.world.monsterSpawnLimit)
		GameRunner.sendGameMessage(sender, "Animal spawn limit: " + sender.world.animalSpawnLimit)
		GameRunner.sendGameMessage(sender, "Ambient spawn limit: " + sender.world.ambientSpawnLimit)
		GameRunner.sendGameMessage(sender, "Water animal spawn limit: " + sender.world.waterAnimalSpawnLimit)
		GameRunner.sendGameMessage(sender, "Water ambient spawn limit: " + sender.world.waterAmbientSpawnLimit)
	}
}
