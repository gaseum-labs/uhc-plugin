package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.team.NameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.minecraft.core.BlockPos
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.LoomMenu
import net.minecraft.world.level.block.Blocks
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftHumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType.LOOM
import org.bukkit.inventory.*
import org.bukkit.material.Dye
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.event.TeamShield
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.team.ColorCube
import org.gaseumlabs.uhc.util.Util

class ParticipantTeamCommands : BaseCommand() {
	@CommandAlias("teamname")
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

	@CommandAlias("teamcolorrandom")
	@Description("get a new team color")
	fun teamColor(sender: CommandSender) {
		if (UHC.game != null)
			return Commands.errorMessage(sender, "Game has already started")

		val team = UHC.preGameTeams.playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		val (color0, color1) = UHC.colorCube.pickTeam()
			?: return Commands.errorMessage(sender, "Could not pick a new color")

		UHC.colorCube.removeTeam(team.colors)

		team.colors[0] = color0
		team.colors[1] = color1
		team.bannerPattern = TeamShield.randomBannerPattern(color0, color1)

		val message = team.apply("Updated your team's color")

		team.members.mapNotNull { Bukkit.getPlayer(it) }.forEach {
			NameManager.updateNominalTeams(it, team, false)
			it.sendMessage(message)
		}
	}

	@CommandAlias("teamcolor")
	@CommandCompletion("@range:0-1 @range:0-15")
	fun teamColor(sender: CommandSender, slot: Int, colorIndex: Int) {
		val colorCube = UHC.colorCube

		if (slot !in 0..1) return Commands.errorMessage(sender, "Teams can only have colors for 0 and 1")

		if (colorIndex !in 0 until ColorCube.NUM_COLORS) {
			return Commands.errorMessage(sender, "Color out of range")
		}

		if (UHC.game != null)
			return Commands.errorMessage(sender, "Game has already started")

		val team = UHC.preGameTeams.playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		val currentColor = team.colors[slot]
		val otherColor = team.colors[ColorCube.otherSlot(slot)]
		val nextColor = DyeColor.values()[colorIndex]

		if (nextColor === currentColor) {
			return Commands.errorMessage(sender, "You are already using this color in this slot")
		}

		if (nextColor === otherColor) {
			team.swapColors()

		} else if (colorCube.taken(nextColor, slot)) {
			return Commands.errorMessage(sender, "This color has already been taken")

		} else {
			/* replace color in this slot */
			colorCube.switchColor(currentColor, nextColor, slot)
			team.colors[slot] = nextColor
		}

		team.bannerPattern = null
		team.members.mapNotNull { Bukkit.getPlayer(it) }.forEach { NameManager.updateNominalTeams(it, team, false) }

		sender.performCommand("colorpicker")
	}

	@CommandAlias("colorpicker")
	fun colorPickerCommand(sender: CommandSender) {
		val colorCube = UHC.colorCube

		val team = UHC.getTeams().playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		for (slot in 0..1) {
			var component = Component.text("Color $slot", NamedTextColor.GRAY)
				.append(Component.text(" : ", NamedTextColor.GOLD, BOLD))

			for (color in DyeColor.values()) {
				val char = when {
					team.colors[slot] === color -> CHAR_SELEC
					colorCube.taken(color, slot) -> CHAR_TAKEN
					else -> CHAR_AVAIL
				}

				var colorBlock = Component.text(char, TextColor.color(color.color.asRGB()))
				if (char == CHAR_AVAIL) colorBlock =
					colorBlock.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
						"/teamcolor $slot ${color.ordinal}"))

				component = component.append(colorBlock)
			}

			sender.sendMessage(component)
		}
	}

	@CommandAlias("teambanner")
	fun teamBannerCommand(sender: CommandSender) {
		val player = sender as? Player ?: return

		if (UHC.game != null)
			return Commands.errorMessage(sender, "Game is already going")

		val team = UHC.preGameTeams.playersTeam(player.uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team")

		player.inventory.addItem(ItemStack(TeamShield.dyeColorToBanner(team.colors[0]), 16))
		player.inventory.addItem(ItemStack(TeamShield.dyeColorToBanner(team.colors[1]), 16))
		player.inventory.addItem(ItemStack(TeamShield.dyeColorToDye(team.colors[0]), 64))
		player.inventory.addItem(ItemStack(TeamShield.dyeColorToDye(team.colors[1]), 64))
		val teamBanner = team.bannerPattern
		if (teamBanner != null) {
			player.inventory.addItem(TeamShield.blockDataToBannerMeta(teamBanner))
		}

		val craftHuman = player as CraftHumanEntity
		val location = craftHuman.location

		val level = craftHuman.handle.level
		val pos = BlockPos(location.blockX, location.blockY, location.blockZ)
		val menuProvider = SimpleMenuProvider({ syncId, inventory, _ ->
			LoomMenu(syncId, inventory, ContainerLevelAccess.create(level, pos))
		}, Util.nmsGradientString("Customize team banner", team.colors[0].color.asRGB(), team.colors[1].color.asRGB()))
		craftHuman.handle.openMenu(menuProvider)
		craftHuman.handle.containerMenu.checkReachable = false
	}

	companion object {
		private const val CHAR_TAKEN = 0x2b1c.toChar()
		private const val CHAR_AVAIL = 0x2b1b.toChar()
		private const val CHAR_SELEC = 0x20de.toChar()
	}
}