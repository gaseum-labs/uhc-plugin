package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.component.ComponentAction.uhcMessage
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.component.UHCComponent.Companion
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.database.DataManager
import org.gaseumlabs.uhc.database.DataManager.NotFoundException
import org.gaseumlabs.uhc.database.DataManager.OfflineException
import org.gaseumlabs.uhc.util.Action
import java.util.UUID

class LinkCommands : BaseCommand() {
	@CommandAlias("link")
	@Description("Use to link your Minecraft account with your Uhcsaturday account")
	fun linkCommand(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return

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
			Action.sendGameMessage(player, "After linking, please wait up to a minute for the link to take effect")

		}.exceptionally { ex ->
			DataManager.clientFacingErrorMessage(
				ex,
				mapOf(400 to "The code you entered was incorrect or expired")
			) { Commands.errorMessage(player, it) }
		}
	}

	@CommandAlias("unlink")
	@Description("If you are already linked, your uhcsaturday account is unlinked from your Minecraft account")
	fun unlinkCommand(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return

		UHC.dataManager.postRequest("/api/bot/unlink/${player.uniqueId}").thenAccept { response ->
			UHC.dataManager.linkData.updateLink(player.uniqueId, null)

			val discordUsername = JsonParser.parseString(response.body()).asJsonObject.get("discordUsername").asString
			Action.sendGameMessage(player, "You have been unlinked from account $discordUsername")

		}.exceptionally { ex ->
			DataManager.clientFacingErrorMessage(
				ex,
				mapOf(404 to "You are not linked")
			) { Commands.errorMessage(player, it) }
		}
	}

	@CommandAlias("uslinked")
	@Description("(Admins only) is this player linked")
	@CommandCompletion("@uhcplayer")
	fun isLinkedCommand(sender: CommandSender, targetPlayer: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		if (UHC.dataManager.linkData.isLinked(targetPlayer.uniqueId)) {
			Action.sendGameMessage(sender, "${targetPlayer.name} is Linked")
		} else {
			Commands.errorMessage(sender, "${targetPlayer.name} is not Linked")
		}
	}
}
