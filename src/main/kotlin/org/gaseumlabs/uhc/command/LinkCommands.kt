package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.database.DataManager.OfflineException
import org.gaseumlabs.uhc.util.Action

class LinkCommands : BaseCommand() {
	@CommandAlias("link")
	@Description("enter the code you received from the website")
	fun linkCommand(sender: CommandSender, code: String) {
		val player = sender as? Player ?: return

		UHC.dataManager.verifyCode(player, code).thenAccept {
			Action.sendGameMessage(player, "Successfully linked your minecraft account")
		}.exceptionally { ex ->
			when (ex) {
				is OfflineException -> Commands.errorMessage(player, "The server is in offline mode")
				else -> Commands.errorMessage(player, "The code you entered was incorrect or expired")
			}
			null
		}
	}
}
