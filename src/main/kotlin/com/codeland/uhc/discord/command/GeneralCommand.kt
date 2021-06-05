package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class GeneralCommand : MixerCommand(true) {
	override fun isCommand(content: String): Boolean {
		return content.startsWith("%general")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val member = event.member ?: return
		val message = event.message

		if (!member.permissions.contains(Permission.ADMINISTRATOR))
			return event.channel.sendMessage("You must be in an admin to use this command!").queue().unit()

		val channel = member.voiceState?.channel
			?: return event.channel.sendMessage("You must be in a vc to use this command!").queue().unit()

		val category = channel.parent
			?: return event.channel.sendMessage("Voice channel ${channel.name} must be in a category!").queue().unit()

		bot.guildId = message.guild.id
		bot.voiceCategoryID = category.id
		bot.voiceChannelID = channel.id

		bot.guild = message.guild
		bot.voiceCategory = category
		bot.generalVoiceChannel = channel

		bot.saveDiscordData()

		event.channel.sendMessage("${channel.name} successfully set as general channel!").queue().void()
	}
}