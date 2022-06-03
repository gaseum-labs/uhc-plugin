package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.google.gson.JsonObject
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.database.DataManager.OfflineException
import org.gaseumlabs.uhc.util.Action

class LinkCommands : BaseCommand() {
	@CommandAlias("link")
	@Description("use to link your minecraft account with your uhcsaturday account")
	fun linkCommand(sender: CommandSender) {
		val player = sender as? Player ?: return

		val body = JsonObject()
		body.addProperty("uuid", player.uniqueId.toString())
		body.addProperty("username", player.name)
		val response = UHC.dataManager.postRequest(
			"/api/bot/createVerifyLink",
			body
		).thenAccept {
			player.sendMessage(it.body())
			val link = it.body().split("\"")[3]
			player.sendMessage("Navigate to the following link to continue: $link")
		}.exceptionally { ex ->
			when (ex) {
				is OfflineException -> Commands.errorMessage(player, "The server is in offline mode")
				else -> Commands.errorMessage(player, "The code you entered was incorrect or expired")
			}
			null
		}
	}
}
