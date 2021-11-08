package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ShareCoordsCommand : BaseCommand() {
	@CommandAlias("sharecoords")
	@Description("shares your coordinates with your teammates")
	fun shareCoords(sender : CommandSender) {
		sender as Player

		val game = UHC.game ?: return Commands.errorMessage(sender, "Game is not going")

		/* sharecoords command can only be used when playing */
		if (!PlayerData.isParticipating(sender.uniqueId))
			return Commands.errorMessage(sender, "You are not participating in the game")

		val team = game.teams.playersTeam(sender.uniqueId)
		val location = sender.location

		/* different message based on teams or no teams */
		/* should never happen */
		if (team == null) {
		   sender.sendMessage("${GOLD}You are at ${location.blockX}, ${location.blockY}, ${location.blockZ}")

		} else {
			val message = team.apply(sender.name).append(Component.text(" is at ${location.blockX}, ${location.blockY}, ${location.blockZ}"))

			/* tell everyone on the team where player is */
			team.members.forEach { Bukkit.getPlayer(it)?.sendMessage(message) }
		}
	}
}
