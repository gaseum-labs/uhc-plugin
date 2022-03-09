package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.UHC
import com.codeland.uhc.database.DataManager
import com.codeland.uhc.database.file.LinkDataFile
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MojangAPI
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Util.void
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class UnlinkCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: MessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, "unlink")
	}

	override fun onCommand(content: String, event: MessageReceivedEvent, bot: MixerBot) {
		fun message(message: String) = event.channel.sendMessage(message).queue()

		val mentionedUser = event.message.mentionedUsers.firstOrNull()

		if (mentionedUser != null) {
			val removedUuid = UHC.dataManager.linkData.revokeLink(mentionedUser.idLong)
			if (removedUuid != null) {
				UHC.dataManager.push(DataManager.linkDataFile, LinkDataFile.LinkEntry(null, null, mentionedUser.idLong))
				message("Unlinked ${mentionedUser.name} from $removedUuid")

			} else {
				message("${mentionedUser.name} is not linked")
			}

		} else {
			val inputUsername = content.split(' ').filter { it.isNotEmpty() }.getOrNull(1)
				?: return message("Please provide a Minecraft username or mention a Discord user")

			MojangAPI.getUUIDFromUsername(inputUsername).thenAccept { result ->
				when (result) {
					is Good -> {
						val (uuid, name) = result.value
						val removedDiscordId = UHC.dataManager.linkData.revokeLink(uuid)

						if (removedDiscordId != null) {
							UHC.dataManager.push(DataManager.linkDataFile, LinkDataFile.LinkEntry(uuid, null, null))
							message("Unlinked $name from $removedDiscordId")

						} else {
							message("$name is not linked")
						}
					}
					is Bad -> message(result.error)
				}
			}.exceptionally { message("An error occurred").void() }
		}
	}
}
