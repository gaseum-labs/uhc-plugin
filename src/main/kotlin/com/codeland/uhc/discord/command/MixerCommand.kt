package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

abstract class MixerCommand(val requiresAdmin: Boolean) {
	abstract fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean
	abstract fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot)

	companion object {
		const val prefix = "%"

		fun errorMessage(event: GuildMessageReceivedEvent, text: String?) {
			event.channel.sendMessage(text ?: "Unknown error").queue()
		}

		fun keywordFilter(content: String, keyword: String): Boolean {
			return content.startsWith(prefix + keyword, true)
		}

		fun afterKeyword(content: String, keyword: String): String {
			return content.substring(prefix.length + keyword.length).trimStart()
		}

		fun replyingToDataFilter(
			event: GuildMessageReceivedEvent,
			needsReplacement: Boolean,
			isChannel: (TextChannel) -> Boolean,
		): Boolean {
			/* the message being replied to */
			val reference = event.message.referencedMessage ?: return false

			/* this will probably always be true */
			val channel = event.message.channel as? TextChannel ?: return false

			/* both messages have data and are in the correct channel */
			return (!needsReplacement || event.message.attachments.isNotEmpty()) &&
			reference.attachments.isNotEmpty() &&
			isChannel(channel)
		}
	}
}
