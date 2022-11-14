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
	fun shareCoords(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return
		internalShareCoords(player, sender as OfflinePlayer)
	}

	@CommandAlias("sharecoords")
	@Description("shares your coordinates with your teammates")
	@CommandCompletion("@uhcteamplayer")
	fun shareCoords(sender: CommandSender, teammate: OfflinePlayer) {
		val player = Commands.playerGuard(sender) ?: return
		internalShareCoords(player, teammate)
	}

	private fun internalShareCoords(senderPlayer: Player, locationPlayer: OfflinePlayer) {
		val game = UHC.game ?: return Commands.errorMessage(senderPlayer, "Game is not going")

		val senderTeam = game.teams.playersTeam(senderPlayer.uniqueId)
			?: return Commands.errorMessage(senderPlayer, "You're not playing")

		if (game.teams.playersTeam(locationPlayer.uniqueId) !== senderTeam)
			return Commands.errorMessage(senderPlayer, "That player is not on your team")

		if (!PlayerData.get(locationPlayer).alive)
			return Commands.errorMessage(senderPlayer, "That player is dead")

		val location = Action.getPlayerLocation(locationPlayer.uniqueId)
			?: return Commands.errorMessage(senderPlayer, "Can't get that player's position")

		val message = senderTeam.apply(locationPlayer.name ?: "Unknown")
			.append(Component.text(" is at ${location.blockX}, ${location.blockY}, ${location.blockZ}"))

		/* tell everyone on the team where player is */
		senderTeam.members.forEach { Bukkit.getPlayer(it)?.sendMessage(message) }
	}
}
