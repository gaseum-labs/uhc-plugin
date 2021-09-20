package com.codeland.uhc.discord

import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.discord.command.EditFileCommand
import com.codeland.uhc.discord.command.GeneralCommand
import com.codeland.uhc.discord.command.LinkCommand
import com.codeland.uhc.discord.command.SummaryCommand
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DataManager.void
import com.codeland.uhc.team.Team
import com.codeland.uhc.util.Util
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
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
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
import javax.imageio.ImageIO

class MixerBot(
	val jda: JDA,
	val production: Boolean,
	val token: String,
	var guildId: Long,
	ip: String
) : ListenerAdapter() {
	companion object {
		fun createMixerBot(configFile: ConfigFile, ip: String, onMixerBot: (MixerBot) -> Unit, onError: (String) -> Unit) {
			try {
				val jda = JDABuilder.createDefault(configFile.botToken).enableIntents(
					GatewayIntent.GUILD_MESSAGES,
					GatewayIntent.GUILD_VOICE_STATES,
					GatewayIntent.GUILD_MEMBERS,
					GatewayIntent.GUILD_EMOJIS,
					GatewayIntent.GUILD_MESSAGE_REACTIONS
				).build()

				/* temporary event listener just for the ready */
				jda.addEventListener(object : ListenerAdapter() {
					override fun onReady(event: ReadyEvent) {
						/* create the bot once jda is ready */
						onMixerBot(MixerBot(jda, configFile.production, configFile.botToken, configFile.serverId, ip))

						/* data manager requires the bot to be ready */
						DataManager.createDataManager(jda, configFile.serverId).exceptionally {
							onError("Data manager could not be created | ${it.message}").void()
						}
					}
				})
			} catch (ex: Exception) {
				onError(ex.message ?: "Unknown Error")
			}
		}
	}

	val commands = arrayOf(
		GeneralCommand(),
		LinkCommand(),
		SummaryCommand(),
		EditFileCommand()
	)

	var serverIcon: String? = null

	init {
		if (production) jda.presence.activity = Activity.playing("UHC at $ip")
		jda.addEventListener(this)

		clearTeamVCs()

		val iconUrl = guild()?.iconUrl
		if (iconUrl != null) {
			val request = HttpRequest.newBuilder(URI(iconUrl)).GET().build()
			val client = HttpClient.newHttpClient()
			ImageIO.read(URL(iconUrl))
			client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenAccept { response ->
				val bytebuf = Unpooled.buffer()

				try {
					val size = 64
					val baseImage = ImageIO.read(response.body())
					val scaledImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)

					val graphics = scaledImage.createGraphics()
					graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
					graphics.drawImage(baseImage, 0, 0, size, size, 0, 0, baseImage.width, baseImage.height, null)
					graphics.dispose()

					ImageIO.write(scaledImage, "PNG", ByteBufOutputStream(bytebuf))
					bytebuf.setInt(16, size)
					bytebuf.setInt(20, size)
					val bytebuffer = Base64.getEncoder().encode(bytebuf.nioBuffer())
					serverIcon = "data:image/png;base64," + StandardCharsets.UTF_8.decode(bytebuffer)

				} finally {
					bytebuf.release()
				}
			}
		}
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
	fun voiceCategory(): Category? = jda.getCategoryById(DataManager.ids.voiceCategoryId)
	fun generalVoiceChannel(): VoiceChannel? = jda.getVoiceChannelById(DataManager.ids.generalVoiceChannelId)

	class SummaryEntry(val place: String, val name: String, val killedBy: String)

	fun sendGameSummary(channel: MessageChannel, gameNumber: Int, day: Int, month: Int, year: Int, matchTime: Int, winners: ArrayList<SummaryEntry>, losers: ArrayList<SummaryEntry>) {
		channel.sendMessage(EmbedBuilder()
			.setColor(0xe7c93c)
			.setTitle("Summary of UHC #$gameNumber on $month/$day/$year")
			.setDescription("Lasted ${Util.timeString(matchTime / 20)}")
			.addField("Winners", if (winners.isEmpty()) "No winners" else winners.fold("") { accum, winner -> accum + "${winner.place}: ${winner.name}\n" }, false)
			.addField("Losers", if (losers.isEmpty()) "No losers" else losers.foldIndexed("") { index, accum, loser -> accum + "${loser.place}: ${loser.name} | killed by ${loser.killedBy}\n"}, false)
			.build()
		).queue()
	}

	fun getDiscordUserIndex(discordID: Long) = DataManager.linkData.discordIds.indexOf(discordID)

	private fun getMinecraftUserIndex(uuid: UUID) = DataManager.linkData.minecraftIds.indexOf(uuid)

	fun isLinked(uuid: UUID) = DataManager.linkData.minecraftIds.contains(uuid)

	private fun voiceMembersFromPlayers(guild: Guild, players: ArrayList<UUID>): List<Member> {
		return players.map { getMinecraftUserIndex(it) }
			.filter { it != -1 }
			.mapNotNull { guild.getMemberById(DataManager.linkData.discordIds[it]) }
			.filter { it.voiceState?.inVoiceChannel() == true }
	}

	fun isAdmin(member: Member): Boolean {
		return member.hasPermission(Permission.ADMINISTRATOR) || member.roles.any { it.idLong == DataManager.ids.adminRoleId }
	}
}
