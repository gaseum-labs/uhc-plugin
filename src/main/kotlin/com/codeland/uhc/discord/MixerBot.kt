package com.codeland.uhc.discord

import com.codeland.uhc.discord.command.GeneralCommand
import com.codeland.uhc.discord.command.LinkCommand
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.command.SummaryCommand
import com.codeland.uhc.team.Team
import com.codeland.uhc.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit
import java.io.*
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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

		fun createMixerBot(discordDataPath: String, linkDataPath: String): MixerBot {
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

				val niceWebsite = URL("http://checkip.amazonaws.com")
				val `in` = BufferedReader(InputStreamReader(niceWebsite.openStream()))
				val ip = `in`.readLine().trim { it <= ' ' }

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

	/* utility */

	fun destroyTeam(team: Team, accept: (Boolean) -> Unit) {
		getTeamChannel(team) { channel ->
			channel ?: return@getTeamChannel accept(false)

			val generalChannel = voiceChannel ?: return@getTeamChannel accept(false)
			val guild = guild ?: return@getTeamChannel accept(false)

			if (channel.members.size == 0) {
				channel.delete().queue { accept(true) }
			} else {
				var hasAccepted = false

				val numMoves = channel.members.size
				var moveCount = 0

				val onCompleteMove = {
					++moveCount

					if (moveCount == numMoves) channel.delete().queue { accept(true) }
				}

				channel.members.forEach { member ->
					guild.moveVoiceMember(member, generalChannel).queue({ onCompleteMove() }, {
						if (!hasAccepted) {
							hasAccepted = true
							accept(false)
						}
					})
				}
			}
		}
	}

	fun addPlayerToTeam(team: Team, uniqueID: UUID, accept: (Boolean) -> Unit) {
		val guild = guild ?: return accept(false)

		val userIndex = getMinecraftUserIndex(uniqueID)
		if (userIndex == -1) return accept(false)

		val member = guild.getMemberById(discordIDs[userIndex])

		if (member?.voiceState?.inVoiceChannel() == true) {
			getTeamChannel(team) { teamChannel ->
				teamChannel ?: return@getTeamChannel

				guild.moveVoiceMember(member, teamChannel).queue { accept(true) }
			}

		} else {
			accept(false)
		}
	}

	fun renameTeam(team: Team, newName: String, accept: (Boolean) -> Unit) {
		getTeamChannel(team) { channel ->
			channel ?: return@getTeamChannel accept(false)

			channel.manager.setName(newName).queue { accept(true) }
		}
	}

	fun moveToGeneral(uniqueID: UUID, accept: (Boolean) -> Unit) {
		val general = voiceChannel ?: return accept(false)
		val guild = guild ?: return accept(false)

		val userIndex = getMinecraftUserIndex(uniqueID)
		if (userIndex == -1) return accept(false)

		val member = guild.getMemberById(discordIDs[userIndex])

		if (member?.voiceState?.inVoiceChannel() == true)
			guild.moveVoiceMember(member, general).queue { accept(true) }
		else
			accept(false)
	}

	fun isLinked(uuid: UUID): Boolean {
		val idString = uuidString(uuid)

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
	 */
	fun getTeamChannel(team: Team, accept: (VoiceChannel?) -> Unit) {
		val category = voiceCategory ?: return accept(null)

		category.voiceChannels.find { channel ->
			if (channel.name == team.colorPair.getName()) { accept(channel); true } else false
		} ?: category.createVoiceChannel(team.colorPair.getName()).queue { created -> accept(created) }
	}

	fun clearTeamVCs() {
		val category = voiceCategory ?: return
		val generalChannel = voiceChannel ?: return

		val channels: List<VoiceChannel> = category.voiceChannels

		channels.forEach { channel ->
			if (channel != generalChannel) channel.delete().queue()
		}
	}

	fun updateRankings(players: ArrayList<ScoredPlayer>) {
		val msg = StringBuilder()
		msg.append("> ").append(1).append(". ").append(players[0].username).append("\n")
		msg.append("> score: ").append(players[0].score).append("\n")
		msg.append("> games played: ").append(players[0].gameCount)
		for (i in 1 until players.size) {
			msg.append("\n\n")
			msg.append("> ").append(i + 1).append(". ").append(players[i].username).append("\n")
			msg.append("> score: ").append(players[i].score).append("\n")
			msg.append("> games played: ").append(players[i].gameCount)
		}
		val pc: TextChannel? = bot.getTextChannelById(704143873823342613L)
		if (pc == null) {
			println("channel ID is null")
		} else {
			pc.editMessageById(704144607876874340L, msg.toString()).complete()
		}
	}

	override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
		val fromUserID: Long = event.author.idLong
		if (fromUserID == 244188985138741249L || fromUserID == 258485243038662657L) { //from varas or balduvian
			if (event.message.contentRaw == "die") {
				clearTeamVCs()
				close()
			} else if (event.message.contentRaw == "kill teams") {
				clearTeamVCs()
			}
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

	private fun close() {
		bot.shutdownNow()
	}

	fun getDiscordUserIndex(discordID: String): Int {
		for (i in discordIDs.indices)
			if (discordID == discordIDs[i]) return i

		return -1
	}

	fun uuidString(uuid: UUID): String {
		return "${String.format("%016x", uuid.mostSignificantBits)}${String.format("%016x", uuid.leastSignificantBits)}"
	}

	fun getMinecraftUserIndex(uuid: UUID): Int {
		val uuidString = uuidString(uuid)

		for (i in discordIDs.indices)
			if (uuidString == minecraftIDs[i]) return i

		return -1
	}
}