package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class GeneralCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, bot, "general")
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
		ConfigFile.save(bot.production, bot.token, bot.guildId.toString())

		val ids = DataManager.ids

		ids.voiceCategoryId = category.idLong
		ids.generalVoiceChannelId = voiceChannel.idLong

		DiscordFilesystem.idsFile.save(event.guild, ids)

		event.channel.sendMessage("${voiceChannel.name} successfully set as general channel!").queue().void()
	}
}
