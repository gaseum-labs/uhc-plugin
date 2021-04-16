package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.team.TeamData
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ParticipantTeamCommands : BaseCommand() {
	@CommandAlias("teamName")
	@Description("change the name of your team")
	fun teamName(sender: CommandSender, newName: String) {
		val team = TeamData.playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		team.name = newName

		/* broadcast change to all teammates */
		val message = Component.text("Your team name has been changed to ").append(team.apply(newName))
		team.members.forEach { uuid -> Bukkit.getPlayer(uuid)?.sendMessage(message) }
	}

	@CommandAlias("teamColor")
	@Description("get a new team color")
	fun teamColor(sender: CommandSender) {
		val team = TeamData.playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		val (color1, color2) = TeamData.teamColor.pickTeam() ?: return Commands.errorMessage(sender, "Could not pick a new color")

		TeamData.teamColor.removeTeam(team.color1, team.color2)

		team.color1 = color1
		team.color2 = color2

		val message = team.apply("Updated your team's color")

		team.members.mapNotNull { Bukkit.getPlayer(it) }.forEach {
			NameManager.updateName(it)
			it.sendMessage(message)
		}
	}
}