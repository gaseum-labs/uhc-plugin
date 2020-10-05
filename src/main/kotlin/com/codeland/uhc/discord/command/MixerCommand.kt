package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.TimeUnit

abstract class MixerCommand(val requiresAdmin: Boolean) {
	fun Any?.unit() = Unit
	fun Any?.void() = null

	abstract fun isCommand(content: String): Boolean

	abstract fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot)

	companion object {
		fun errorMessage(event: GuildMessageReceivedEvent, text: String) {
			event.channel.sendMessage(text).queue { sent -> sent.delete().queueAfter(5, TimeUnit.SECONDS) }
			event.message.delete().queueAfter(5, TimeUnit.SECONDS)
		}
	}
}
