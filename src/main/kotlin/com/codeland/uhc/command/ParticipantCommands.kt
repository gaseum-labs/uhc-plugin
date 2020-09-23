package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.Chat
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.team.ColorPair
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
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
		val team = TeamData.playersTeam(sender as Player)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		var realNewName = newName
		if (realNewName.startsWith('"') && realNewName.endsWith('"') && realNewName.length > 1) {
			realNewName = realNewName.substring(1, realNewName.length - 1)
		}

		team.displayName = realNewName

		/* broadcast change to all teammates */
		team.members.forEach { member ->
			member.player?.sendMessage("Your team name has been changed to \"${team.colorPair.colorString(realNewName)}\"")
		}
	}

	@CommandAlias("color")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color0: ChatColor) {
		changeTeamColor(sender, color0, null)
	}

	@CommandAlias("color")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color0: ChatColor, color1: ChatColor) {
		changeTeamColor(sender, color0, color1)
	}

	private fun changeTeamColor(sender: CommandSender, color0: ChatColor, color1: ChatColor?) {
		val team = TeamData.playersTeam(sender as Player)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		fun colorError(color: ChatColor) {
			Commands.errorMessage(sender, "${color}${Util.colorPrettyNames[color.ordinal]} ${ChatColor.RESET}is not a valid team color!")
		}

		if (!Team.isValidColor(color0)) return colorError(color0)
		if (color1 != null && !Team.isValidColor(color1)) return colorError(color1)

		val colorPair = ColorPair(color0, color1)

		if (colorPair.color0 == colorPair.color1)
			return Commands.errorMessage(sender, "Invalid color combination!")

		if (TeamData.teamExists(colorPair))
			return Commands.errorMessage(sender, "That color combination is already being used by another team!")

		/* change team name to be default name for new color if no custom name has been set */
		if (team.isDefaultName()) team.displayName = colorPair.getName()
		team.colorPair = colorPair

		val message = "Your team color has been changed to ${colorPair.colorString(colorPair.getName())}"

		/* broadcast change to all teammates */
		team.members.forEach { member ->
			val player = member.player
			if (player != null) GameRunner.sendGameMessage(player, message)
		}
	}

	@CommandAlias("mobcaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		sender as Player

		GameRunner.sendGameMessage(sender, "Monster spawn limit: ${sender.world.monsterSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Animal spawn limit: ${sender.world.animalSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Ambient spawn limit: ${sender.world.ambientSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Water animal spawn limit: ${sender.world.waterAnimalSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Water ambient spawn limit: ${sender.world.waterAmbientSpawnLimit}")
	}
}
