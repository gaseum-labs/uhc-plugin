package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.component.ComponentAction.uhcMessage
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.component.UHCComponent.Companion
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

		UHC.dataManager.postRequest(
			"/api/bot/createVerifyLink",
			body
		).thenAccept { response ->
			val link = (JsonParser.parseString(response.body()) as JsonObject).get("link").asString

			player.uhcMessage(UHCComponent.text("Please visit ", UHCColor.U_WHITE)
				.and(Companion.link(link, link, UHCColor.U_GOLD)).and(" to link", UHCColor.U_WHITE))

		}.exceptionally { ex ->
			when (ex) {
				is OfflineException -> Commands.errorMessage(player, "The server is in offline mode")
				else -> Commands.errorMessage(player, "The code you entered was incorrect or expired")
			}
			null
		}
	}
}
