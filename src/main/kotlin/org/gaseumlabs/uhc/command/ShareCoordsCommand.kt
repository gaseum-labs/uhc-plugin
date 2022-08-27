package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.util.Action
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.PlayerData

class ShareCoordsCommand : BaseCommand() {
	@CommandAlias("sharecoords")
	@Description("shares your coordinates with your teammates")
	@CommandCompletion("@uhcteamplayer")
	fun shareCoords(sender: CommandSender, teammate: OfflinePlayer?) {
		sender as Player
		val game = UHC.game ?: return Commands.errorMessage(sender, "Game is not going")
		val locationPlayer = teammate ?: sender

		val senderTeam = game.teams.playersTeam(sender.uniqueId)
			?: return Commands.errorMessage(sender, "You're not playing")

		if (game.teams.playersTeam(locationPlayer.uniqueId) !== senderTeam)
			return Commands.errorMessage(sender, "That player is not on your team")

		if (!PlayerData.get(locationPlayer).alive)
			return Commands.errorMessage(sender, "That player is dead")

		val location = Action.getPlayerLocation(locationPlayer.uniqueId)
			?: return Commands.errorMessage(sender, "Can't get that player's position")

		val message = senderTeam.apply(teammate?.name ?: sender.name)
			.append(Component.text(" is at ${location.blockX}, ${location.blockY}, ${location.blockZ}"))

		/* tell everyone on the team where player is */
		senderTeam.members.forEach { Bukkit.getPlayer(it)?.sendMessage(message) }
	}
}
