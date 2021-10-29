package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.TimeUnit

abstract class MixerCommand(val requiresAdmin: Boolean) {
	fun Any?.unit() = Unit
	fun Any?.void() = null

	abstract fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean
	abstract fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot)

	companion object {
		fun errorMessage(event: GuildMessageReceivedEvent, text: String?) {
			event.channel.sendMessage(text ?: "Unknown error").queue { sent -> sent.delete().queueAfter(5, TimeUnit.SECONDS) }
			event.message.delete().queueAfter(10, TimeUnit.SECONDS)
		}

		fun prefix(production: Boolean): String {
			return if (production) "%" else "$%"
		}

		fun keywordFilter(content: String, bot: MixerBot, keyword: String): Boolean {
			return content.startsWith("${prefix(bot.production)}${keyword}")
		}

		fun optionalDebugFilter(content: String, bot: MixerBot): Boolean {
			val startsWithDebug = content.startsWith(prefix(false))

			return if (bot.production) {
				!startsWithDebug
			} else {
				startsWithDebug
			}
		}

		fun replyingToDataFilter(event: GuildMessageReceivedEvent, needsReplacement: Boolean, isChannel: (TextChannel) -> Boolean): Boolean {
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
