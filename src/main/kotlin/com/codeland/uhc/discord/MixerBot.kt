package com.codeland.uhc.discord

import com.codeland.uhc.command.TeamData
import com.codeland.uhc.core.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.scoreboard.Team
import java.io.*
import java.lang.Exception
import java.net.URL
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

	private val mcUserNames: ArrayList<String> = ArrayList()
	private val discordIDs: ArrayList<Long> = ArrayList()

	var guild: Guild? = null
	var voiceCategory: Category? = null
	var voiceChannel: VoiceChannel? = null

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

	private fun saveDiscordData() {
		val writer = FileWriter(File(discordDataPath), false)

		writer.write("${token}\n$guildID\n$voiceCategoryID\n$voiceChannelID")

		writer.close()
	}

	private fun saveLinkData() {
		val writer = FileWriter(File(linkDataPath), false)

		for (i in mcUserNames.indices) {
			writer.write("${mcUserNames[i]} ${discordIDs[i]}\n")
		}

		writer.close()
	}

	fun readLinkData(linkDataPath: String) {
		mcUserNames.clear()
		discordIDs.clear()

		val file = File(linkDataPath)

		if (file.exists()) {
			val reader = BufferedReader(FileReader(linkDataPath))

			while (reader.ready()) {
				val line = reader.readLine()

				if (line.contains(" ")) {
					mcUserNames.add(line.substring(0, line.indexOf(" ")))
					discordIDs.add(line.substring(line.lastIndexOf(" ") + 1).toLong())
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

	fun addPlayerToTeam(team: Team, name: String, accept: (Boolean) -> Unit) {
		val guild = guild ?: return accept(false)

		for (i in mcUserNames.indices) {
			if (mcUserNames[i] == name) {
				val member = guild.getMemberById(discordIDs[i])

				if (member?.voiceState?.inVoiceChannel() == true) {
					getTeamChannel(team) { teamChannel ->
						teamChannel ?: return@getTeamChannel

						guild.moveVoiceMember(member, teamChannel).queue { accept(true) }
					}
				}

				return
			}
		}

		accept(false)
	}

	fun renameTeam(team: Team, newName: String, accept: (Boolean) -> Unit) {
		getTeamChannel(team) { channel ->
			channel ?: return@getTeamChannel accept(false)

			channel.manager.setName(newName).queue { accept(true) }
		}
	}

	fun moveToGeneral(playerUsername: String, accept: (Boolean) -> Unit) {
		val general = voiceChannel ?: return accept(false)
		val guild = guild ?: return accept(false)

		for (i in mcUserNames.indices) {
			if (mcUserNames[i] == playerUsername) {
				val member = guild.getMemberById(discordIDs[i]) ?: break

				if (member.voiceState?.inVoiceChannel() == true)
					return guild.moveVoiceMember(member, general).queue { accept(true) }
				else
					break
			}
		}

		accept(false)
	}

	fun isLinked(username: String): Boolean {
		return mcUserNames.any { name ->
			name == username
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
			if (channel.name == TeamData.prettyTeamName(team.color)) { accept(channel); true } else false
		} ?: category.createVoiceChannel(team.displayName).queue { created -> accept(created) }
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

	class SummaryEntry(val name: String, val killedBy: String)

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		val message = event.message
		val content = message.contentRaw

		val member = event.member ?: return
		if (!content.startsWith("%")) return

		/* safely exit and return expressions */
		fun Any?.unit() = Unit
		fun Any?.void() = null

		if (content.startsWith("%link ")) {
			val userID = event.author.idLong

			val username = content.substring(1 + content.lastIndexOf(' '))
			for (i in discordIDs.indices) {
				if (discordIDs[i] == userID) {
					mcUserNames[i] = username
					saveLinkData()

					event.channel.sendMessage("changed minecraft username from \"${mcUserNames[i]}\" to \"$username\"").queue()
					return
				}
			}

			mcUserNames.add(username)
			discordIDs.add(userID)
			event.channel.sendMessage("set minecraft username to \"$username\"").complete()

			try {
				val fr = FileWriter(File(linkDataPath), true)
				fr.write("\n$username $userID")
				fr.close()
			} catch (e: IOException) {
				e.printStackTrace()
			}

		} else if (content.startsWith("%general")) {
			if (!member.permissions.contains(Permission.ADMINISTRATOR))
				return event.channel.sendMessage("You must be in an admin to use this command!").queue().unit()

			val channel = member.voiceState?.channel
				?: return event.channel.sendMessage("You must be in a vc to use this command!").queue().unit()

			val category = channel.parent
				?: return event.channel.sendMessage("Voice channel ${channel.name} must be in a category!").queue().unit()

			guildID = message.guild.id
			voiceCategoryID = category.id
			voiceChannelID = channel.id

			guild = message.guild
			voiceCategory = category
			voiceChannel = channel

			saveDiscordData()

			event.channel.sendMessage("${channel.name} successfully set as general channel!").queue().void()
		} else if (content.startsWith("%summary")) {
			val errResolve = { finalText: String ->
				event.channel.sendMessage(finalText).queue { sent -> sent.delete().queueAfter(5, TimeUnit.SECONDS) }.void()
				message.delete().queueAfter(5, TimeUnit.SECONDS).void()
			}

			if (!member.permissions.contains(Permission.ADMINISTRATOR))
				return errResolve("You must be in an admin to use this command!").unit()

			val attachments = message.attachments

			if (attachments.size != 1 || attachments[0].isImage || attachments[0].fileExtension != "txt")
				return errResolve("Please attach a summary .txt file").unit()

			val filenameParts = attachments[0].fileName.split('_')
			if (filenameParts.size != 4)
				return errResolve("Summary filename incorrectly formatted\nShould be NUM_DAY_MONTH_YEAR.txt").unit()

			val filenameNumberErrString = "Filename encountered an incorrectly formatted number"

			val gameNumber = filenameParts[0].toIntOrNull() ?: return errResolve(filenameNumberErrString).unit()
			val day = filenameParts[1].toIntOrNull() ?: return errResolve(filenameNumberErrString).unit()
			val month = filenameParts[2].toIntOrNull() ?: return errResolve(filenameNumberErrString).unit()
			val year = filenameParts[3].substring(0, filenameParts[3].indexOf('.')).toIntOrNull() ?: return errResolve(filenameNumberErrString).unit()

			attachments[0].retrieveInputStream().thenAccept{ stream ->
				val winningPlayers = ArrayList<SummaryEntry>()
				val losingPlayers = ArrayList<SummaryEntry>()
				var matchTime = 0

				val reader = BufferedReader(InputStreamReader(stream))

				val lines = reader.lineSequence()
				val readerErr = lines.any { line ->
					val parts = line.split(' ')

					if (parts.size != 4)
						true
					else {
						if (parts[0].startsWith("1")) {
							matchTime = parts[2].toIntOrNull() ?: return@any true
							winningPlayers
						} else {
							losingPlayers
						}.add(SummaryEntry(parts[1], parts[3]))

						false
					}
				}

				reader.close()

				if (readerErr) {
					errResolve("Error reading summary file")
				} else {
					message.delete().submit().thenAccept {
						sendGameSummary(message.channel, gameNumber, day, month, year, matchTime, Array(winningPlayers.size) { i -> winningPlayers[i] }, Array(losingPlayers.size) { i -> losingPlayers[i] })
					}.exceptionally { err ->
						errResolve(err.message ?: "Unknown error")
					}
				}

			}.exceptionally {
				errResolve("Something went wrong with the connection")
			}
		}
	}

	fun sendGameSummary(channel: MessageChannel, gameNumber: Int, day: Int, month: Int, year: Int, matchTime: Int, winners: Array<SummaryEntry>, losers: Array<SummaryEntry>) {
		val embed = EmbedBuilder()
			.setColor(0xe7c93c)
			.setTitle("Summary of UHC #$gameNumber on $month/$day/$year")
			.setDescription("Lasted ${Util.timeString(matchTime)}")
			.addField("Winners", if (winners.isEmpty()) "No winners" else winners.fold("") { accum, winner -> accum + "1: ${winner.name}\n" }, false)
			.addField("Losers", if (losers.isEmpty()) "No losers" else losers.foldIndexed("") { index, accum, loser -> accum + "${index + 1 + winners.size}: ${loser.name} | killed by ${loser.killedBy}\n"}, false)
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

	/* unused */

	private fun stripTeams(m: Member) {
		for (r in m.roles) {
			if (r.name.startsWith("Team ")) {
				m.guild.removeRoleFromMember(m, r).complete()
			}
		}
	}

	private fun getDiscordID(username: String): Long {
		for (i in mcUserNames.indices) {
			if (username == mcUserNames[i]) {
				return discordIDs[i]
			}
		}
		return -1
	}
}