package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.team.NameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ParticipantTeamCommands : BaseCommand() {
	@CommandAlias("teamName")
	@Description("change the name of your team")
	fun teamName(sender: CommandSender, newName: String) {
		val filteredName = newName.trim()

		if (filteredName.length !in 3..20) {
			return Commands.errorMessage(sender, "Please enter a name 3 to 20 characters long")
		}

		val team = UHC.getTeams().playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		team.giveName(filteredName)

		/* broadcast change to all teammates */
		val message = Component.text("Your team name has been changed to ").append(team.apply(filteredName))
		team.members.forEach { uuid -> Bukkit.getPlayer(uuid)?.sendMessage(message) }
	}

	@CommandAlias("teamColor random")
	@Description("get a new team color")
	fun teamColor(sender: CommandSender) {
		val team = UHC.getTeams().playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		val (color0, color1) = UHC.colorCube.pickTeam()
			?: return Commands.errorMessage(sender, "Could not pick a new color")

		UHC.colorCube.removeTeam(team.colors)

		team.colors[0] = color0
		team.colors[1] = color1

		val message = team.apply("Updated your team's color")

		team.members.mapNotNull { Bukkit.getPlayer(it) }.forEach {
			NameManager.updateNominalTeams(it, team, false)
			it.sendMessage(message)
		}
	}

	@CommandAlias("teamColor")
	@CommandCompletion("@range:0-1")
	fun teamColor(sender: CommandSender, slot: Int, colorIndex: Int) {
		val colorCube = UHC.colorCube

		if (slot !in 0..1) return Commands.errorMessage(sender, "Teams can only have colors for 0 and 1")
		val otherSlot = if (slot == 1) 0 else 1

		if (colorIndex !in 0 until colorCube.subdivisions * colorCube.subdivisions * colorCube.subdivisions) {
			return Commands.errorMessage(sender, "Color out of range")
		}

		val team = UHC.getTeams().playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		val currentIndex = colorCube.indexFromColor(team.colors[slot])
		val otherIndex = colorCube.indexFromColor(team.colors[otherSlot])

		if (currentIndex == colorIndex) {
			return Commands.errorMessage(sender, "You are already using this color in this slot")
		}

		if (otherIndex == colorIndex) {
			/* swap team colors */
			val temp = team.colors[slot]
			team.colors[slot] = team.colors[otherSlot]
			team.colors[otherSlot] = temp

		} else if (colorCube.taken(colorIndex)) {
			return Commands.errorMessage(sender, "This color has already been taken")

		} else {
			/* replace color in this slot */
			colorCube.switchColor(currentIndex, colorIndex)
			team.colors[slot] = colorCube.colorFromIndex(colorIndex)
		}

		team.members.mapNotNull { Bukkit.getPlayer(it) }.forEach { NameManager.updateNominalTeams(it, team, false) }

		sender.performCommand("colorPicker $slot")
	}

	@CommandAlias("colorPicker")
	@CommandCompletion("@range:0-1")
	fun colorpicker(sender: CommandSender, slot: Int) {
		if (slot !in 0..1) return Commands.errorMessage(sender, "Teams can only have colors for 0 and 1")
		val otherSlot = if (slot == 1) 0 else 1

		val colorCube = UHC.colorCube
		val sub = colorCube.subdivisions

		val taken = 0x2b1c.toChar()
		val available = 0x2b1b.toChar()
		val selected = 0x20de.toChar()

		val team = UHC.getTeams().playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		for (b in 0 until sub) {
			var component = Component.empty()

			for (rg in 0 until sub * sub) {
				val r = rg / sub
				val g = rg % sub

				val colorIndex = colorCube.indexFromPosition(r, g, b)
				val color = colorCube.colorFromPosition(r, g, b)

				val char = when {
					colorCube.indexFromColor(team.colors[slot]) == colorIndex -> selected
					colorCube.indexFromColor(team.colors[otherSlot]) == colorIndex -> available
					colorCube.taken(colorIndex) -> taken
					else -> available
				}

				var colorBlock = Component.text(char, TextColor.color(color))
				if (char == available) colorBlock =
					colorBlock.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
						"/teamColor $slot $colorIndex"))

				component = component.append(colorBlock)
			}

			sender.sendMessage(component)
		}
	}
}