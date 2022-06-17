package org.gaseumlabs.uhc.discord

import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.gaseumlabs.uhc.core.ConfigFile
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.discord.command.MixerCommand
import org.gaseumlabs.uhc.discord.storage.DiscordStorage
import org.gaseumlabs.uhc.util.WebAddress
import org.gaseumlabs.uhc.util.extensions.RestActionExtensions
import org.gaseumlabs.uhc.util.extensions.RestActionExtensions.submitAsync
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.net.URI
import java.net.http.*
import java.util.*
import java.util.concurrent.*
import javax.imageio.ImageIO

class MixerBot(val jda: JDA, val guild: Guild, address: String?) : ListenerAdapter() {
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
					MixerBot(jda, guild, configFile.ddnsDomain)
				}
			}
		}
	}

	val commands = emptyArray<MixerCommand>()

	val SummaryManager: SummaryManager = SummaryManager(this)

	init {
		val ip = address ?: WebAddress.getLocalAddress()

		jda.presence.activity = Activity.playing("UHC at $ip")
		jda.addEventListener(this)

		DiscordStorage.load(guild)

		clearTeamVCs()
	}

	fun loadIcon(): CompletableFuture<BufferedImage> {
		val iconUrl = guild.iconUrl ?: return CompletableFuture.failedFuture(Exception("Server has no icon"))

		val request = HttpRequest.newBuilder(URI(iconUrl)).GET().build()
		val client = HttpClient.newHttpClient()

		return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply { response ->
			val size = 64
			val baseImage = ImageIO.read(response.body())
			val scaledImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)

			val graphics = scaledImage.createGraphics()
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
			graphics.drawImage(baseImage, 0, 0, size, size, 0, 0, baseImage.width, baseImage.height, null)
			graphics.dispose()

			scaledImage
		}
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
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
					if (channel.idLong != general.idLong) {
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

	private fun voiceMembersFromPlayers(guild: Guild, players: List<UUID>): List<Member> {
		return players.mapNotNull { UHC.dataManager.linkData.getDiscordId(it) }
			.mapNotNull { guild.getMemberById(it) }
			.filter { it.voiceState?.inAudioChannel() == true }
	}

	private fun isAdmin(member: Member): Boolean {
		return member.hasPermission(Permission.ADMINISTRATOR) || member.roles.any { it.idLong == DiscordStorage.adminRole }
	}
}
