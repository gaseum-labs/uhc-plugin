package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MojangAPI
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.database.DataManager
import com.codeland.uhc.database.file.LinkDataFile
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Util.void
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class LinkCommand : MixerCommand(false) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, "link")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		fun message(message: String) = event.channel.sendMessage(message).queue()

		val inputUsername = content.split(' ').filter { it.isNotEmpty() }.getOrNull(1)
			?: return message("Please provide a Minecraft username")

		MojangAPI.getUUIDFromUsername(inputUsername).thenAccept { result ->
			when (result) {
				is Good -> {
					val (uuid, name) = result.value
					val discordId = event.author.idLong

					when (UHC.dataManager.linkData.addLink(discordId, uuid)) {
						LinkDataFile.LinkData.LinkStatus.SUCCESSFUL -> {
							UHC.dataManager.push(DataManager.linkDataFile, LinkDataFile.LinkEntry(uuid, name, discordId))
							message("Successfully linked as $name")
						}
						LinkDataFile.LinkData.LinkStatus.TAKEN -> {
							message("Someone else has already linked as $name")
						}
						LinkDataFile.LinkData.LinkStatus.ALREADY_LINKED -> {
							message("You are already linked as $name")
						}
					}
				}
				is Bad -> message(result.error)
			}
		}.exceptionally { message("An error occurred").void() }
	}
}
