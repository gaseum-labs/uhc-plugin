package com.codeland.uhc.discord

import com.codeland.uhc.discord.command.GeneralCommand
import com.codeland.uhc.discord.command.LinkCommand
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.command.SummaryCommand
import com.codeland.uhc.team.ColorPair
import com.codeland.uhc.team.Team
import com.codeland.uhc.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.String.*

class MixerBot(
	private val discordDataPath: String,
	val linkDataPath: String,
	val token: String,
	var guildID: String,
	var voiceCategoryID: String,
	var voiceChannelID: String,
	ip: String
) : ListenerAdapter() {
	private val bot: JDA = JDABuilder.createDefault(token).build()

	val discordIDs: ArrayList<String> = ArrayList()
	val minecraftIDs: ArrayList<String> = ArrayList()

	var guild: Guild? = null
	var voiceCategory: Category? = null
	var voiceChannel: VoiceChannel? = null

	val commands = arrayOf(
		GeneralCommand(),
		LinkCommand(),
		SummaryCommand()
	)

	init {
		bot.presence.activity = Activity.playing("UHC at $ip")
		bot.addEventListener(this)

		readLinkData(linkDataPath)
	}

	companion object {
		const val NO_ID = "NONE"

		fun createMixerBot(discordDataPath: String, linkDataPath: String, ip: String): MixerBot {
			val discordDataFile = File(discordDataPath)

			if (discordDataFile.exists()) {
				val reader = BufferedReader(FileReader(discordDataFile))

				val token = if (reader.ready()) reader.readLine() else null
				var anyLineFailed = false

				val guildID = if (reader.ready()) reader.readLine() else { anyLineFailed = true; NO_ID }
				val categoryID = if (reader.ready()) reader.readLine() else { anyLineFailed = true; NO_ID }
				val channelID = if (reader.ready()) reader.readLine() else { anyLineFailed = true; NO_ID }

				reader.close()

				if (token == null || anyLineFailed) writeDummyDiscordData(discordDataPath, token)
				if (token == null) throw Exception("No token found in $discordDataPath")

				return MixerBot(discordDataPath, linkDataPath, token, guildID, categoryID, channelID, ip)
			} else {
				writeDummyDiscordData(discordDataPath)

				throw Exception("No Discord data file found, created the template file, $discordDataPath")
			}
		}

		private fun writeDummyDiscordData(discordDataPath: String, token: String? = null) {
			val writer = FileWriter(File(discordDataPath), false)

			writer.write(
				"${token ?: "BOT TOKEN GOES ON THIS LINE"}\n" +
				"GUILD ID ON THIS LINE\n" +
				"VOICE CHANNEL CATEGORY ID ON THIS LINE\n" +
				"VOICE CHANNEL ID ON THIS LINE\n"
			)

			writer.close()
		}
	}

	class SummaryEntry(val place: String, val name: String, val killedBy: String)

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		val message = event.message
		val content = message.contentRaw
		val member = event.member ?: return

		commands.any { command ->
			if (command.isCommand(content)) {
				if (command.requiresAdmin && !member.permissions.contains(Permission.ADMINISTRATOR))
					MixerCommand.errorMessage(event, "You must be an administrator to use this command!")
				else
					command.onCommand(content, event, this)

				true
			} else {
				false
			}
		}
	}

	override fun onReady(event: ReadyEvent) {
		try {
			guild = if (guildID == NO_ID) null else bot.getGuildById(guildID)
			voiceCategory = if (voiceCategoryID == NO_ID) null else bot.getCategoryById(voiceCategoryID)
			voiceChannel = if (voiceChannelID == NO_ID) null else bot.getVoiceChannelById(voiceChannelID)
		} catch (ex: Exception) {
			guild = null
			voiceCategory = null
			voiceChannel = null
		}

		clearTeamVCs()
	}

	/* disk data reading writing */

	fun saveDiscordData() {
		val writer = FileWriter(File(discordDataPath), false)

		writer.write("${token}\n$guildID\n$voiceCategoryID\n$voiceChannelID")

		writer.close()
	}

	fun saveLinkData() {
		val writer = FileWriter(File(linkDataPath), false)

		for (i in discordIDs.indices) {
			writer.write("${discordIDs[i]} ${minecraftIDs[i]}${if (i == discordIDs.lastIndex) "" else "\n"}")
		}

		writer.close()
	}

	fun readLinkData(linkDataPath: String) {
		discordIDs.clear()
		minecraftIDs.clear()

		val file = File(linkDataPath)

		if (file.exists()) {
			val reader = BufferedReader(FileReader(linkDataPath))

			while (reader.ready()) {
				val line = reader.readLine()

				if (line.contains(" ")) {
					discordIDs.add(line.substring(0, line.indexOf(" ")))
					minecraftIDs.add(line.substring(line.lastIndexOf(" ") + 1))
				}
			}
		} else {
			writeDummyLinkData(linkDataPath)
		}
	}

	private fun writeDummyLinkData(linkDataPath: String) {
		val writer = FileWriter(File(linkDataPath), false).close()
	}

	/*=******************************/
	/*         TEAM UTILITY         */
	/*=******************************/

	/**
	 * @return whether or not the team channel could be destroyed
	 */
	fun destroyTeamChannel(team: Team): Boolean {
		val generalChannel = voiceChannel ?: return false
		val guild = guild ?: return false

		val teamChannel = getTeamChannel(team.colorPair) ?: return false

		teamChannel.members.forEach { member ->
			try {
				guild.moveVoiceMember(member, generalChannel).complete()
			} catch (ex: IllegalStateException) {}
		}

		teamChannel.delete().complete()

		return true
	}

	/**
	 * @return whether or not the player could be added to the team channel
	 */
	fun addToTeamChannel(team: Team, uniqueID: UUID): Boolean {
		val guild = guild ?: return false

		val userIndex = getMinecraftUserIndex(uniqueID)
		if (userIndex == -1) return false
		val member = guild.getMemberById(discordIDs[userIndex])

		return if (member?.voiceState?.inVoiceChannel() == true) {
			val teamChannel = getTeamChannel(team.colorPair)

			try {
				guild.moveVoiceMember(member, teamChannel).complete()
			} catch (ex: IllegalStateException) {}

			true

		} else {
			false
		}
	}

	fun updateTeamChannel(oldColor: ColorPair, newColor: ColorPair): Boolean {
		val channel = getTeamChannel(oldColor) ?: return false

		channel.manager.setName(newColor.getName()).complete()

		return true
	}

	fun moveToGeneral(uniqueID: UUID): Boolean {
		val general = voiceChannel ?: return false
		val guild = guild ?: return false

		val userIndex = getMinecraftUserIndex(uniqueID)
		if (userIndex == -1) return false

		val member = guild.getMemberById(discordIDs[userIndex])

		return if (member?.voiceState?.inVoiceChannel() == true) {
			guild.moveVoiceMember(member, general).complete()
			true

		} else {
			false
		}
	}

	fun isLinked(uuid: UUID): Boolean {
		val idString = uuidToString(uuid)

		return minecraftIDs.any { id ->
			id == idString
		}
	}

	/**
	 * will create a new voice channel for
	 * the team if none currently exists
	 *
	 * asynchronous function
	 * get channel from callback
	 *
	 * @return a voice channel corresponding to a team's color
	 */
	private fun getTeamChannel(colorPair: ColorPair): VoiceChannel? {
		val category = voiceCategory ?: return null
		val teamChannelName = colorPair.getName()

		return category.voiceChannels.find { channel ->
			channel.name == teamChannelName
		} ?: category.createVoiceChannel(teamChannelName).complete()
	}

	fun clearTeamVCs() {
		val category = voiceCategory ?: return
		val generalChannel = voiceChannel ?: return

		val channels = category.voiceChannels

		channels.forEach { channel ->
			if (channel != generalChannel) {
				channel.members.forEach { vcer ->
					guild?.moveVoiceMember(vcer, generalChannel)?.complete()
				}

				channel.delete().complete()
			}
		}
	}

	/* random utility */

	fun sendGameSummary(channel: MessageChannel, gameNumber: Int, day: Int, month: Int, year: Int, matchTime: Int, winners: ArrayList<SummaryEntry>, losers: ArrayList<SummaryEntry>) {
		val embed = EmbedBuilder()
			.setColor(0xe7c93c)
			.setTitle("Summary of UHC #$gameNumber on $month/$day/$year")
			.setDescription("Lasted ${Util.timeString(matchTime)}")
			.addField("Winners", if (winners.isEmpty()) "No winners" else winners.fold("") { accum, winner -> accum + "${winner.place}: ${winner.name}\n" }, false)
			.addField("Losers", if (losers.isEmpty()) "No losers" else losers.foldIndexed("") { index, accum, loser -> accum + "${loser.place}: ${loser.name} | killed by ${loser.killedBy}\n"}, false)
			.build()

		channel.sendMessage(embed).queue()
	}

	private fun close() {
		bot.shutdown()
	}

	fun getDiscordUserIndex(discordID: String): Int {
		for (i in discordIDs.indices)
			if (discordID == discordIDs[i]) return i

		return -1
	}

	fun uuidToString(uuid: UUID): String {
		return "${String.Companion.format("%016x", uuid.mostSignificantBits)}${String.Companion.format("%016x", uuid.leastSignificantBits)}"
	}

	private fun getMinecraftUserIndex(uuid: UUID): Int {
		val uuidString = uuidToString(uuid)

		for (i in discordIDs.indices)
			if (uuidString == minecraftIDs[i]) return i

		return -1
	}
}
