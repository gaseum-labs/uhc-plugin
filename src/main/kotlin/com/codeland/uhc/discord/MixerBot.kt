package com.codeland.uhc.discord

import com.codeland.uhc.discord.command.EditFileCommand
import com.codeland.uhc.discord.command.GeneralCommand
import com.codeland.uhc.discord.command.LinkCommand
import com.codeland.uhc.discord.command.SummaryCommand
import com.codeland.uhc.discord.filesystem.DataManager
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
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import org.bukkit.ChatColor
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

class MixerBot(
	val jda: JDA,
	val dataManager: DataManager,
	val token: String,
	var guildId: Long,
	ip: String
) : ListenerAdapter() {
	companion object {
		const val DISCORD_DATA_PATH = "./discordData.txt"

		fun createMixerBot(ip: String, onMixerBot: (MixerBot) -> Unit, onError: (String) -> Unit) {
			val discordDataFile = File(DISCORD_DATA_PATH)

			/* load the bot token and UHC Server Id from disk */
			if (discordDataFile.exists()) {
				try {
					val reader = BufferedReader(FileReader(discordDataFile))

					val token = if (reader.ready()) reader.readLine() else null
					val guildID = (if (reader.ready()) reader.readLine() else null)?.toLongOrNull()

					reader.close()

					if (token == null || guildID == null) {
						writeDummyDiscordData(token)
						return onError("No token found in $DISCORD_DATA_PATH")
					}

					val jda = JDABuilder.createDefault(token).enableIntents(
						GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.GUILD_VOICE_STATES,
						GatewayIntent.GUILD_MEMBERS,
						GatewayIntent.GUILD_EMOJIS,
						GatewayIntent.GUILD_MESSAGE_REACTIONS
					).build()

					/* temporary event listener just for the ready */
					jda.addEventListener(object : ListenerAdapter() {
						override fun onReady(event: ReadyEvent) {
							/* data manager requires the bot to be ready */
							val dataManager = DataManager.createDataManager(jda, guildID) {
								Util.log("${ChatColor.RED}$it")
							}
								?: return onError("Data Manager could not be created")

							/* now the bot can have its main listener */
							onMixerBot(MixerBot(
								jda,
								dataManager,
								token,
								guildID,
								ip
							))
						}
					})
				} catch (ex: Exception) {
					onError(ex.message ?: "Unknown Error")
				}
			} else {
				writeDummyDiscordData()
				onError("No Discord Data file found, created the template file, $DISCORD_DATA_PATH")
			}
		}

		private fun writeDummyDiscordData(token: String? = null) {
			val writer = FileWriter(File(DISCORD_DATA_PATH), false)

			writer.write(
				"${token ?: "BOT TOKEN GOES ON THIS LINE"}\n" +
				"GUILD ID ON THIS LINE\n"
			)

			writer.close()
		}
	}

	val commands = arrayOf(
		GeneralCommand(),
		LinkCommand(),
		SummaryCommand(),
		EditFileCommand()
	)

	init {
		jda.presence.activity = Activity.playing("UHC at $ip")
		jda.addEventListener(this)

		clearTeamVCs()
	}

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		val message = event.message
		val content = message.contentRaw
		val member = event.member ?: return

		commands.any { command ->
			if (command.isCommand(content, event, this)) {
				if (command.requiresAdmin && !isAdmin(member))
					MixerCommand.errorMessage(event, "You must be an administrator to use this command!")
				else
					command.onCommand(content, event, this)

				true
			} else {
				false
			}
		}
	}

	/* disk data reading writing */

	fun saveDiscordData() {
		val writer = FileWriter(File(DISCORD_DATA_PATH), false)

		writer.write("${token}\n${guildId}")

		writer.close()
	}

	private class Awaiter(val num: Int, val onComplete: () -> Unit) {
		var completed = 0

		fun queue(action: RestAction<Void>) {
			action.queue({ if (++completed == num) onComplete() }, { if (++completed == num) onComplete() })
		}
	}

	/* ---------------------------- */
	/*         TEAM UTILITY         */
	/* ---------------------------- */

	fun destroyTeamChannel(team: Team) {
		val guild = guild() ?: return

		getTeamChannel(team.id) { teamChannel ->
			destroyTeamChannel(guild, teamChannel)
		}
	}

	/**
	 * removes an arbitrary amount of players from a team channel,
	 * will delete the team channel if everyone is removed
	 * @param teamSize the number of players on this team before removal
	 * @param players a list of player UUIDs on the team to move to general
	 */
	fun removeFromTeamChannel(team: Team, teamSize: Int, players: ArrayList<UUID>) {
		val guild = guild() ?: return
		val voiceChannel = generalVoiceChannel() ?: return

		/* should never be above but just in case */
		/* remove everyone, delete channel */
		if (players.size >= teamSize) {
			destroyTeamChannel(team)

		/* remove only certain players, don't delete channel */
		} else {
			voiceMembersFromPlayers(guild, players).forEach { member ->
				guild.moveVoiceMember(member, voiceChannel).queue()
			}
		}
	}

	fun addToTeamChannel(team: Team, players: ArrayList<UUID>) {
		val guild = guild() ?: return

		getTeamChannel(team.id) { teamChannel ->
			voiceMembersFromPlayers(guild, players).forEach { member ->
				guild.moveVoiceMember(member, teamChannel).queue()
			}
		}
	}

	/* --------------------------------------------------------------- */

	/**
	 * will create a new voice channel for
	 * the team if none currently exists
	 *
	 * the voiceChannel is passed into onVoiceChannel
	 * callback is not called if there was an error
	 */
	private fun getTeamChannel(id: Int, onVoiceChannel: (VoiceChannel) -> Unit) {
		val category = voiceCategory() ?: return
		val teamChannelName = "Team $id"

		val existingChannel = category.voiceChannels.find { channel -> channel.name == teamChannelName }

		if (existingChannel != null)
			onVoiceChannel(existingChannel)
		else
			category.createVoiceChannel(teamChannelName).queue { onVoiceChannel(it) }
	}

	private fun destroyTeamChannel(guild: Guild, teamChannel: VoiceChannel) {
		if (teamChannel.members.isEmpty()) {
			teamChannel.delete().queue()

		} else {
			val awaiter = Awaiter(teamChannel.members.size) {
				teamChannel.delete().queue()
			}

			/* move members to general before deleting team channel */
			teamChannel.members.forEach { member ->
				awaiter.queue(guild.moveVoiceMember(member, generalVoiceChannel()))
			}
		}
	}

	fun clearTeamVCs() {
		val voiceCategory = voiceCategory() ?: return
		val voiceChannel = generalVoiceChannel() ?: return
		val guild = guild() ?: return

		val channels = voiceCategory.voiceChannels

		channels.forEach { channel ->
			if (channel != voiceChannel) destroyTeamChannel(guild, channel)
		}
	}

	/* utility */

	fun guild(): Guild? = jda.getGuildById(guildId)
	fun voiceCategory(): Category? = jda.getCategoryById(dataManager.ids.voiceCategoryId)
	fun generalVoiceChannel(): VoiceChannel? = jda.getVoiceChannelById(dataManager.ids.generalVoiceChannelId)

	class SummaryEntry(val place: String, val name: String, val killedBy: String)

	fun sendGameSummary(channel: MessageChannel, gameNumber: Int, day: Int, month: Int, year: Int, matchTime: Int, winners: ArrayList<SummaryEntry>, losers: ArrayList<SummaryEntry>) {
		channel.sendMessage(EmbedBuilder()
			.setColor(0xe7c93c)
			.setTitle("Summary of UHC #$gameNumber on $month/$day/$year")
			.setDescription("Lasted ${Util.timeString(matchTime)}")
			.addField("Winners", if (winners.isEmpty()) "No winners" else winners.fold("") { accum, winner -> accum + "${winner.place}: ${winner.name}\n" }, false)
			.addField("Losers", if (losers.isEmpty()) "No losers" else losers.foldIndexed("") { index, accum, loser -> accum + "${loser.place}: ${loser.name} | killed by ${loser.killedBy}\n"}, false)
			.build()
		).queue()
	}

	fun getDiscordUserIndex(discordID: Long) = dataManager.linkData.discordIds.indexOf(discordID)

	private fun getMinecraftUserIndex(uuid: UUID) = dataManager.linkData.minecraftIds.indexOf(uuid)

	fun isLinked(uuid: UUID) = dataManager.linkData.minecraftIds.contains(uuid)

	private fun voiceMembersFromPlayers(guild: Guild, players: ArrayList<UUID>): List<Member> {
		return players.map { getMinecraftUserIndex(it) }
			.filter { it != -1 }
			.mapNotNull { guild.getMemberById(dataManager.linkData.discordIds[it]) }
			.filter { it.voiceState?.inVoiceChannel() == true }
	}

	fun isAdmin(member: Member): Boolean {
		return member.hasPermission(Permission.ADMINISTRATOR) || member.roles.any { it.idLong == dataManager.ids.adminRoleId }
	}
}
