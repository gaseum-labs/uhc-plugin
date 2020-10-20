package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.Chat
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.team.*
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext

@CommandAlias("uhc")
class ParticipantCommands : BaseCommand() {
	@CommandAlias("gui")
	@Description("get the current setup as the gui")
	fun getCurrentSetupGui(sender: CommandSender) {
		GameRunner.uhc.gui.inventory.open(sender as Player)
	}

	@CommandAlias("optOut")
	@Description("opt out from participating")
	fun optOutCommand(sender: CommandSender) {
		sender as Player

		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			Commands.errorMessage(sender, "The game has already started!")

		} else if (GameRunner.uhc.isOptingOut(sender.uniqueId)) {
			Commands.errorMessage(sender, "You have already opted out!")

		} else {
			GameRunner.uhc.setOptOut(sender.uniqueId, true)
			GameRunner.uhc.setParticipating(sender.uniqueId, false)

			val team = TeamData.playersTeam(sender.uniqueId)
			if (team != null) TeamData.removeFromTeam(team, sender.uniqueId, true) {}

			GameRunner.sendGameMessage(sender, "You have opted out of participating")
		}
	}

	@CommandAlias("optIn")
	@Description("opt back into participating")
	fun optInCommand(sender: CommandSender) {
		sender as Player

		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			Commands.errorMessage(sender, "The game has already started!")

		} else if (!GameRunner.uhc.isOptingOut(sender.uniqueId)) {
			Commands.errorMessage(sender, "You already aren't opting out!")

		} else {
			GameRunner.uhc.setOptOut(sender.uniqueId, false)

			GameRunner.sendGameMessage(sender, "You have opted back into participating")
		}
	}

	@CommandAlias("name")
	@Description("change the name of your team")
	fun teamName(sender: CommandSender, newName: String) {
		val team = TeamData.playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		var realNewName = newName
		if (realNewName.startsWith('"') && realNewName.endsWith('"') && realNewName.length > 1) {
			realNewName = realNewName.substring(1, realNewName.length - 1)
		}

		team.displayName = realNewName

		/* broadcast change to all teammates */
		team.members.forEach { uuid ->
			val player = Bukkit.getPlayer(uuid)
			player?.sendMessage("Your team name has been changed to \"${team.colorPair.colorString(realNewName)}\"")
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

	@CommandAlias("color random")
	@Description("change your team color")
	fun teamColor(sender: CommandSender) {
		val colors = TeamMaker.getColorList(1)

		if (colors == null)
			Commands.errorMessage(sender, "Could not make you a new random color!")
		else
			changeTeamColor(sender, colors[0].color0, colors[0].color1)
	}

	private fun changeTeamColor(sender: CommandSender, color0: ChatColor, color1: ChatColor?) {
		val team = TeamData.playersTeam((sender as Player).uniqueId)
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
		team.members.forEach { uuid ->
			val player = Bukkit.getPlayer(uuid)

			if (player != null) {
				GameRunner.sendGameMessage(player, message)
				NameManager.updateName(player)
			}
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
