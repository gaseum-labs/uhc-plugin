package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MixerCommand
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.util.Util
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.ChatColor
import java.io.BufferedReader
import java.io.InputStreamReader

class EditFileCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		val message = event.message

		/* message has a file to replace the discord file contents with */
		if (message.attachments.isEmpty()) return false

		val reference = message.referencedMessage

		/* replying to a file message */
		return reference != null && reference.attachments.isNotEmpty()
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val reference = event.message.referencedMessage ?: return

		val header = DiscordFilesystem.messageHeader(reference) ?:
			return errorMessage(event, "This is not a discord file message")

		DiscordFilesystem.messageStream(event.message).thenAccept { stream ->
			var errorMessage = ""

			/* set the internal data associated with the discord file */
			val valid = DiscordFilesystem.updateMessage(header, stream) {
				errorMessage += "${it}\n"
			}

			stream.close()

			/* report on any defects in the given data */
			if (errorMessage.isNotEmpty()) errorMessage(event, errorMessage)

			/* delete old message, sent message will be treated as new one */
			if (valid) {
				reference.delete().queue()
			} else {
				Util.log("${ChatColor.RED}Invalid data sent")
				event.message.delete().queue()
			}

		}.exceptionally {
			errorMessage(event, it.message ?: "Unknown error").void()
		}
	}
}
