package org.gaseumlabs.uhc.event

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.FancyText
import org.gaseumlabs.uhc.util.SchedulerUtil
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.*
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Chat : Listener {
	companion object {
		fun defaultGenerator(string: String): TextComponent {
			return Component.empty().append(Component.text(string, NamedTextColor.BLUE))
		}

		interface Mention {
			fun matches(): String
			fun includes(player: Player): Boolean
			fun generate(string: String): TextComponent
			fun needsOp(): Boolean
		}

		val mentionEveryone = object : Mention {
			override fun matches() = "everyone"
			override fun includes(player: Player) = true
			override fun generate(string: String) = defaultGenerator(string)
			override fun needsOp() = true
		}

		val mentionParticipating = object : Mention {
			override fun matches() = "participating"
			override fun includes(player: Player) = PlayerData.get(player.uniqueId).participating
			override fun generate(string: String) = defaultGenerator(string)
			override fun needsOp() = true
		}

		val mentionSpectating = object : Mention {
			override fun matches() = "spectating"
			override fun includes(player: Player) =
				!PlayerData.get(player.uniqueId).participating && player.gameMode == GameMode.SPECTATOR

			override fun generate(string: String) = defaultGenerator(string)
			override fun needsOp() = false
		}

		val mentionOp = object : Mention {
			override fun matches() = "op"
			override fun includes(player: Player) = player.isOp
			override fun generate(string: String) = defaultGenerator(string)
			override fun needsOp() = false
		}

		class PlayerMention(val player: Player) : Mention {
			override fun matches() = player.name
			override fun includes(player: Player) = this.player === player
			override fun generate(string: String) =
				UHC.getTeams().playersTeam(player.uniqueId)?.apply(string) ?: defaultGenerator(string)

			override fun needsOp() = false
		}

		class NickMention(val player: Player, val nickname: String) : Mention {
			override fun matches() = nickname
			override fun includes(player: Player) = this.player === player
			override fun generate(string: String) =
				UHC.getTeams().playersTeam(player.uniqueId)?.apply(string) ?: defaultGenerator(string)

			override fun needsOp() = false
		}
	}

	/**
	 * @param startIndex the index of the @ of the mention in the string
	 * @return a pair of the mention found and the corresponding endIndex for the matched substring in message,
	 *         or null if no mention watch found
	 */
	private fun matchMention(message: String, startIndex: Int, checkList: ArrayList<Mention>): Pair<Mention, Int>? {
		/* a list tracking if each mention in checklist is still matching the message */
		/* elements are set to false when the corresponding mention fails to matach the message */
		var numRemaining = checkList.size
		val remaining = Array(numRemaining) { true }

		/* go over characters in the message starting at startIndex */
		for (iMessage in startIndex + 1 until message.length) {
			val iMatch = iMessage - startIndex - 1

			/* compare the current character to all remaining mentions */
			for (j in remaining.indices) if (remaining[j]) {
				val mention = checkList[j]

				/* character-wise comparison, converted to lowercase */
				if (mention.matches()[iMatch].code.or(0x20) != message[iMessage].code.or(0x20)) {
					remaining[j] = false
					if (--numRemaining == 0) return null

				} else if (iMatch == mention.matches().lastIndex) {
					if (numRemaining == 1) {
						return Pair(mention, iMessage + 1)

					} else {
						remaining[j] = false
						if (--numRemaining == 0) return null
					}
				}
			}
		}

		return null
	}

	data class MentionInstance(val mention: Mention, val startIndex: Int, val endIndex: Int)

	private fun collectMentions(message: String, checkList: ArrayList<Mention>): ArrayList<MentionInstance> {
		val mentionList = ArrayList<MentionInstance>()
		var iterIndex = 0

		while (iterIndex < message.length) {
			if (message[iterIndex] == '@') {
				val matched = matchMention(message, iterIndex, checkList)

				if (matched != null) {
					val (mention, endIndex) = matched
					mentionList.add(MentionInstance(mention, iterIndex, endIndex))

					iterIndex = endIndex

				} else {
					++iterIndex
				}
			} else {
				++iterIndex
			}
		}

		return mentionList
	}

	/**
	 * splits the message into parts based on the mentions collected
	 * mentions will be applied normally
	 * mention components will be replaced with underlines when sent to the players
	 */
	private fun divideMessage(
		message: String,
		collected: ArrayList<MentionInstance>,
		color: TextColor,
	): List<Pair<TextComponent, Mention?>> {
		return (0..collected.size).flatMap { i ->
			val current = collected.getOrNull(i)

			val lastEnd = collected.getOrNull(i - 1)?.endIndex ?: 0
			val nextStart = current?.startIndex ?: message.length

			listOfNotNull(
				if (nextStart - lastEnd > 0)
					Component.text(message.substring(lastEnd, nextStart), color) to null
				else null,
				current?.mention?.generate(message.substring(current.startIndex, current.endIndex))?.to(current.mention)
			)
		}
	}

	private fun messageForPlayer(
		player: Player,
		parts: List<Pair<TextComponent, Mention?>>,
	): Pair<Component, Boolean> {
		var mentioned = false

		val component = parts.fold(Component.empty()) { acc, (component, mention) ->
			if (mention?.includes(player) == true) {
				mentioned = true
				acc.append(component.style(Style.style(TextDecoration.UNDERLINED)))
			} else {
				acc.append(component)
			}
		}

		return component to mentioned
	}

	private fun generateMentions(): ArrayList<Mention> {
		val list = arrayListOf(
			mentionEveryone,
			mentionOp,
			mentionParticipating,
			mentionSpectating
		)

		list.addAll(Bukkit.getOnlinePlayers().map { PlayerMention(it) })

		UHC.dataManager.nicknames.allNicks().forEach { (uuid, nicknames) ->
			val player = Bukkit.getPlayer(uuid)

			if (player != null) {
				list.addAll(nicknames.map { NickMention(player, it) })
			}
		}

		return list
	}

	private fun playerComponent(team: Team?, player: Player): Component {
		val str = "<${player.name}> "
		return team?.apply(str) ?: Component.text(str)
	}

	@EventHandler
	fun onMessage(event: AsyncChatEvent) {
		event.isCancelled = true

		val message = (event.originalMessage() as? TextComponent)?.content() ?: return

		val gameTeam = UHC.game?.teams?.playersTeam(event.player.uniqueId)
		val playerComponent = playerComponent(gameTeam, event.player)

		val collected = collectMentions(message, generateMentions())

		/* ! by itself is just a message and won't go to global chat */
		val exclaiming = message.startsWith('!') && message.length >= 2

		/* fancy text */
		/* team chat */
		/* global chat */
		val (parts, recipients) = if (message.startsWith('$')) {
			divideMessage(FancyText.make(message.substring(1)),
				collected,
				NamedTextColor.WHITE) to Bukkit.getOnlinePlayers()

		} else if (gameTeam != null && collected.firstOrNull()?.startIndex != 0 && !exclaiming) {
			divideMessage(message,
				collected,
				TextColor.color(gameTeam.colors.last().color.asRGB())) to gameTeam.members.mapNotNull {
				Bukkit.getPlayer(it)
			}

		} else {
			divideMessage(
				if (exclaiming) message.substring(1) else message,
				collected,
				NamedTextColor.WHITE
			) to Bukkit.getOnlinePlayers()
		}

		recipients.forEach { player ->
			val (playerMessage, mentioned) = messageForPlayer(player, parts)
			player.sendMessage(playerComponent.append(playerMessage))

			if (mentioned) {
				player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.707107f)
				SchedulerUtil.later(5) {
					player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.943874f)
				}
			}
		}
	}
}
