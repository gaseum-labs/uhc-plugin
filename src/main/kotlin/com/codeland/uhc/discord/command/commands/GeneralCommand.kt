package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.DataManager
import com.codeland.uhc.discord.database.file.IdsFile
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class GeneralCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, bot, "general")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val member = event.member ?: return
		val message = event.message

		val voiceChannel = member.voiceState?.channel
			?: return event.channel.sendMessage("You must be in a vc to use this command!").queue()

		val category = voiceChannel.parent
			?: return event.channel.sendMessage("Voice channel ${voiceChannel.name} must be in a category!").queue()


		val ids = UHC.dataManager.ids

		ids.voiceCategory = category.idLong
		ids.generalVoiceChannel = voiceChannel.idLong

		val connection = UHC.dataManager.connection
		if (connection != null) {
			DataManager.idsFile.push(connection, IdsFile.IdsEntry(voiceCategory = ids.voiceCategory, generalVoiceChannel = ids.generalVoiceChannel))
		}

		event.channel.sendMessage("${voiceChannel.name} successfully set as general channel!").queue()
	}
}
