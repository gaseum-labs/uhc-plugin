package com.codeland.uhc.discord

import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.command.*
import com.codeland.uhc.discord.command.commands.*
import com.codeland.uhc.discord.storage.Channels
import com.codeland.uhc.discord.storage.DiscordStorage
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Util
import com.codeland.uhc.util.Util.void
import com.codeland.uhc.util.WebAddress
import com.codeland.uhc.util.extensions.RestActionExtensions
import com.codeland.uhc.util.extensions.RestActionExtensions.submitAsync
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
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

class MixerBot(val jda: JDA, val guild: Guild) : ListenerAdapter() {
	companion object {
		fun createMixerBot(configFile: ConfigFile): CompletableFuture<MixerBot> {
			return CompletableFuture.supplyAsync {
				val serverId = configFile.serverId ?: throw Exception("No serverId in config file")
				val botToken = configFile.botToken ?: throw Exception("No botToken in config file")

				val jda = JDABuilder.createDefault(botToken).enableIntents(
					GatewayIntent.GUILD_MESSAGES,
					GatewayIntent.GUILD_VOICE_STATES,
					GatewayIntent.GUILD_MEMBERS,
					GatewayIntent.GUILD_EMOJIS,
					GatewayIntent.GUILD_MESSAGE_REACTIONS
				).build().awaitReady()

				val guild = jda.getGuildById(serverId)

				if (guild == null) {
					throw Exception("Could not find the guild by id ${configFile.serverId}")
				} else {
					MixerBot(jda, guild)
				}
			}
		}
	}

	val commands = arrayOf(
		LinkCommand(),
		EditSummaryCommand(),
		PublishSummaryCommand()
	)

	var serverIcon: String? = null

	val SummaryManager: SummaryManager = SummaryManager(this)

	init {
		val ip = WebAddress.getLocalAddress()

		jda.presence.activity = Activity.playing("UHC at $ip")
		jda.addEventListener(this)

		DiscordStorage.load(guild)

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

	fun stop() {
		jda.shutdownNow()
	}

	/* ---------------------------- */
	/*         TEAM UTILITY         */
	/* ---------------------------- */

	/**
	 * removes an arbitrary amount of players from a team channel,
	 * will delete the team channel if everyone is removed
	 * @param teamSize the number of players on this team after removal
	 * @param players a list of player UUIDs on the team to move to general
	 */
	fun removeFromTeamChannel(teamId: Int, teamSize: Int, players: List<UUID>) {
		Channels.getCategoryVoiceChannel(guild, Channels.VOICE_CATEGORY_NAME, Channels.GENERAL_VOICE_CHANNEL_NAME)
			.thenAccept { (category, general) ->
				/* remove everyone, delete channel */
				if (teamSize <= 0) {
					getTeamChannel(category, teamId).thenAccept { teamChannel ->
						destroyTeamChannel(guild, teamChannel, general)
					}

				/* remove only certain players, don't delete channel */
				} else {
					voiceMembersFromPlayers(guild, players).forEach { member ->
						guild.moveVoiceMember(member, general).queue()
					}
				}
			}
	}

	fun addToTeamChannel(teamId: Int, players: List<UUID>) {
		getTeamChannel(teamId).thenAccept { teamChannel ->
			voiceMembersFromPlayers(guild, players).forEach { member ->
				guild.moveVoiceMember(member, teamChannel).queue()
			}
		}
	}

	fun clearTeamVCs() {
		Channels.getCategoryVoiceChannel(guild, Channels.VOICE_CATEGORY_NAME, Channels.GENERAL_VOICE_CHANNEL_NAME)
			.thenAccept { (category, general) ->
				category.voiceChannels.forEach { channel ->
					if (channel.idLong != general.idLong)  {
						destroyTeamChannel(guild, channel, general)
					}
				}
			}
	}

	/* --------------------------------------------------------------- */

	private fun teamChannelName(teamId: Int): String {
		return "Team $teamId"
	}

	private fun getTeamChannel(category: Category, teamdId: Int): CompletableFuture<VoiceChannel> {
		return Channels.getVoiceChannel(category, teamChannelName(teamdId))
	}

	private fun getTeamChannel(teamdId: Int): CompletableFuture<VoiceChannel> {
		return Channels.getCategoryVoiceChannel(guild, Channels.VOICE_CATEGORY_NAME, "Team $teamdId")
			.thenApply { (_, channel) -> channel }
	}

	private fun destroyTeamChannel(guild: Guild, teamChannel: VoiceChannel, generalChannel: VoiceChannel) {
		if (teamChannel.members.isEmpty()) {
			teamChannel.delete().queue()

		} else {
			RestActionExtensions.allOf(
				teamChannel.members.map { member -> guild.moveVoiceMember(member, generalChannel).submitAsync() }
			).thenAccept {
				teamChannel.delete().queue()
			}
		}
	}

	/* utility */

	fun isLinked(uuid: UUID) = if (UHC.dataManager.isOnline()) UHC.dataManager.linkData.minecraftToDiscord.containsKey(uuid) else true

	private fun voiceMembersFromPlayers(guild: Guild, players: List<UUID>): List<Member> {
		return players.mapNotNull { UHC.dataManager.linkData.minecraftToDiscord[it] }
			.mapNotNull { guild.getMemberById(it) }
			.filter { it.voiceState?.inVoiceChannel() == true }
	}

	private fun isAdmin(member: Member): Boolean {
		return member.hasPermission(Permission.ADMINISTRATOR) || member.roles.any { it.idLong == DiscordStorage.adminRole }
	}
}
