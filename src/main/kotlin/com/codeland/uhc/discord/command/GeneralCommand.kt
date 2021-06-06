package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MixerCommand
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class GeneralCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return content.startsWith("%general")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val member = event.member ?: return
		val message = event.message

		val voiceChannel = member.voiceState?.channel
			?: return event.channel.sendMessage("You must be in a vc to use this command!").queue().unit()

		val category = voiceChannel.parent
			?: return event.channel.sendMessage("Voice channel ${voiceChannel.name} must be in a category!").queue().unit()

		/* save channel ids */
		bot.guildId = message.guild.idLong
		bot.saveDiscordData()

		val ids = bot.dataManager.ids

		ids.voiceCategoryId = category.idLong
		ids.generalVoiceChannelId = voiceChannel.idLong

		DiscordFilesystem.idsFile.save(event.guild, ids)

		event.channel.sendMessage("${voiceChannel.name} successfully set as general channel!").queue().void()
	}
}
