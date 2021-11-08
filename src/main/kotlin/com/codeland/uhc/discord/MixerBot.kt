package com.codeland.uhc.discord

import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.command.*
import com.codeland.uhc.discord.command.commands.*
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Util
import com.codeland.uhc.util.Util.void
import com.codeland.uhc.util.WebAddress
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
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
import javax.imageio.ImageIO

class MixerBot(
	val jda: JDA,
	val guild: Guild,
	val production: Boolean,
	val token: String,
) : ListenerAdapter() {
	companion object {
		fun createMixerBot(configFile: ConfigFile, onMixerBot: (MixerBot) -> Unit, onError: (String) -> Unit) {
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
						val guild = jda.getGuildById(configFile.serverId)
						if (guild == null) {
							onError("Could not find the guild by id ${configFile.serverId}")

						} else {
							/* create the bot once jda is ready */
							onMixerBot(MixerBot(
								jda,
								guild,
								configFile.production,
								configFile.botToken,
							))
						}
					}
				})
			} catch (ex: Exception) {
				onError(ex.message ?: "Unknown Error")
			}
		}
	}

	val commands = arrayOf(
		LinkCommand(),
		GeneralCommand(),
		EditSummaryCommand(),
		PublishSummaryCommand()
	)

	var serverIcon: String? = null

	val SummaryManager: SummaryManager = SummaryManager(this)

	init {
		val ip = WebAddress.getLocalAddress()

		if (production) jda.presence.activity = Activity.playing("UHC at $ip")
		jda.addEventListener(this)

		clearTeamVCs()

		val iconUrl = guild.iconUrl
		if (iconUrl != null) {
			val request = HttpRequest.newBuilder(URI(iconUrl)).GET().build()
			val client = HttpClient.newHttpClient()

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

	fun stop() {
		jda.shutdownNow()
	}

	/* ---------------------------- */
	/*         TEAM UTILITY         */
	/* ---------------------------- */

	fun destroyTeamChannel(teamId: Int) {
		getTeamChannel(teamId) { teamChannel ->
			destroyTeamChannel(guild, teamChannel)
		}
	}

	/**
	 * removes an arbitrary amount of players from a team channel,
	 * will delete the team channel if everyone is removed
	 * @param teamSize the number of players on this team after removal
	 * @param players a list of player UUIDs on the team to move to general
	 */
	fun removeFromTeamChannel(teamId: Int, teamSize: Int, players: List<UUID>) {
		val voiceChannel = generalVoiceChannel() ?: return

		/* remove everyone, delete channel */
		if (teamSize <= 0) {
			destroyTeamChannel(teamId)

		/* remove only certain players, don't delete channel */
		} else {
			voiceMembersFromPlayers(guild, players).forEach { member ->
				guild.moveVoiceMember(member, voiceChannel).queue()
			}
		}
	}

	fun addToTeamChannel(teamId: Int, players: List<UUID>) {
		getTeamChannel(teamId) { teamChannel ->
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
	private fun getTeamChannel(teamdId: Int, onVoiceChannel: (VoiceChannel) -> Unit) {
		val category = voiceCategory() ?: return
		val teamChannelName = "Team $teamdId"

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

		val channels = voiceCategory.voiceChannels

		channels.forEach { channel ->
			if (channel != voiceChannel) destroyTeamChannel(guild, channel)
		}
	}

	/* utility */

	fun isLinked(uuid: UUID) = if (UHC.dataManager.isOnline()) UHC.dataManager.linkData.minecraftToDiscord.containsKey(uuid) else true

	private fun voiceCategory(): Category? = jda.getCategoryById(UHC.dataManager.ids.voiceCategory)

	private fun generalVoiceChannel(): VoiceChannel? = jda.getVoiceChannelById(UHC.dataManager.ids.generalVoiceChannel)

	private fun voiceMembersFromPlayers(guild: Guild, players: List<UUID>): List<Member> {
		return players.mapNotNull { UHC.dataManager.linkData.minecraftToDiscord[it] }
			.mapNotNull { guild.getMemberById(it) }
			.filter { it.voiceState?.inVoiceChannel() == true }
	}

	private fun isAdmin(member: Member): Boolean {
		return member.hasPermission(Permission.ADMINISTRATOR) || member.roles.any { it.idLong == UHC.dataManager.ids.adminRole }
	}
}
