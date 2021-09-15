package com.codeland.uhc.event

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Action
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.team.Team
import com.codeland.uhc.util.FancyText
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*
import kotlin.collections.ArrayList

class Chat : Listener {
	companion object {
		fun addNick(player: UUID, nickname: String) {
			getNicks(player).add(nickname)

			val bot = UHC.bot ?: return
			DiscordFilesystem.nicknamesFile.save(bot.guild()!!, DataManager.nicknames)
		}

		fun removeNick(player: UUID, nickname: String): Boolean {
			val lowerNickname = nickname.lowercase()

			val removed = getNicks(player).removeIf { it.lowercase() == lowerNickname }

			val bot = UHC.bot ?: return removed
			DiscordFilesystem.nicknamesFile.save(bot.guild()!!, DataManager.nicknames)

			return removed
		}

		fun getNicks(uuid: UUID): ArrayList<String> {
			val playerIndex = DataManager.nicknames.minecraftIds.indexOf(uuid)

			/* put into list for a new player*/
			return if (playerIndex == -1) {
				DataManager.nicknames.minecraftIds.add(uuid)
				DataManager.nicknames.nicknames.add(ArrayList())

				DataManager.nicknames.nicknames.last()

			/* nickname list for existing player */
			} else {
				DataManager.nicknames.nicknames[playerIndex]
			}
		}

		fun defaultGenerator(string: String): Component {
			return Component.empty().append(Component.text(string, NamedTextColor.BLUE))
		}

		interface Mention {
			fun matches(): String
			fun includes(player: Player): Boolean
			fun generate(string: String): Component
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
			override fun includes(player: Player) = PlayerData.isParticipating(player.uniqueId)
			override fun generate(string: String) = defaultGenerator(string)
			override fun needsOp() = true
		}

		val mentionSpectating = object : Mention {
			override fun matches() = "spectating"
			override fun includes(player: Player) = !PlayerData.isParticipating(player.uniqueId) && player.gameMode == GameMode.SPECTATOR
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
			override fun generate(string: String) = TeamData.playersTeam(player.uniqueId)?.apply(string) ?: defaultGenerator(string)
			override fun needsOp() = false
		}

		class TeamMention(val team: Team) : Mention {
			override fun matches() = team.gameName()
			override fun includes(player: Player) = team.members.contains(player.uniqueId)
			override fun generate(string: String) = team.apply(string)
			override fun needsOp() = false
		}

		class NickMention(val player: Player, val nickname: String) : Mention {
			override fun matches() = nickname
			override fun includes(player: Player) = this.player === player
			override fun generate(string: String) = TeamData.playersTeam(player.uniqueId)?.apply(string) ?: defaultGenerator(string)
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
				if (mention.matches()[iMatch].toInt().or(0x20) != message[iMessage].toInt().or(0x20)) {
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

	private fun collectMentions(message: String, checkList: ArrayList<Mention>): ArrayList<Triple<Mention, Int, Int>> {
		val mentionList = ArrayList<Triple<Mention, Int, Int>>()
		var iterIndex = 0

		while (iterIndex < message.length) {
			if (message[iterIndex] == '@') {
				val matched = matchMention(message, iterIndex, checkList)

				if (matched != null) {
					val (mention, endIndex) = matched
					mentionList.add(Triple(mention, iterIndex, endIndex))

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
	private fun divideMessage(message: String, collected: ArrayList<Triple<Mention, Int, Int>>, chatColor: TextColor): ArrayList<Component> {
		val componentList = arrayListOf(
			Component.text(message.substring(0, collected.firstOrNull()?.second ?: message.length), chatColor) as Component
		)

		for (i in collected.indices) {
			val (mention, startIndex, endIndex) = collected[i]

			/* add the mention, which is replaced */
			componentList.add(mention.generate(message.substring(startIndex, endIndex)))

			/* the part after the mention until the next mention, or until the end of the message */
			componentList.add(Component.text(message.substring(endIndex, collected.getOrNull(i + 1)?.second ?: message.length), chatColor))
		}

		return componentList
	}

	private fun messageForPlayer(player: Player, mentions: ArrayList<Triple<Mention, Int, Int>>, components: ArrayList<Component>): Component {
		return components.foldIndexed(Component.empty()) { i, acc, component ->
			/* regular components */
			if (i % 2 == 0) acc.append(component)

			/* mention components */
			else acc.append(
				if (mentions[i / 2].first.includes(player))
					component.style(Style.style(TextDecoration.UNDERLINED))
				else
					component
			)
		}
	}

	private fun generateMentions(): ArrayList<Mention> {
		val list = arrayListOf(
			mentionEveryone,
			mentionOp,
			mentionParticipating,
			mentionSpectating
		)

		list.addAll(Bukkit.getOnlinePlayers().map { PlayerMention(it) })
		list.addAll(TeamData.teams.map { TeamMention(it) })

		for (i in DataManager.nicknames.minecraftIds.indices) {
			val player = Bukkit.getPlayer(DataManager.nicknames.minecraftIds[i])

			if (player != null) {
				val nicks = DataManager.nicknames.nicknames[i]
				list.addAll(nicks.map { NickMention(player, it) })
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

		val message = (event.message() as? TextComponent)?.content() ?: return

		val team = TeamData.playersTeam(event.player.uniqueId)
		val playerComponent = playerComponent(team, event.player)

		val collected = collectMentions(message, generateMentions())


		if (message.startsWith('$')) {
			val cleanMessage = FancyText.make(message.substring(1))

			Bukkit.getOnlinePlayers().forEach { player ->
				player.sendMessage(playerComponent.append(Component.text(cleanMessage)))
			}

		/* filter messages only to teammates */
		/* if the sending player is on a team */
		/* if the message does not start with a mention */
		/* and the message does not start with ! */
		} else if (UHC.game != null && team != null && collected.firstOrNull()?.second != 0 && !message.startsWith("!")) {
			val messageParts = divideMessage(message, collected, team.colors.last())

			team.members.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
				player.sendMessage(playerComponent.append(messageForPlayer(player, collected, messageParts)))
			}

		/* regular chat behavior before game */
		} else {
			val cleanMessage = if (message.startsWith("!")) message.substring(1) else message

			if (cleanMessage.isNotEmpty()) {
				val messageParts = divideMessage(cleanMessage, collected, NamedTextColor.WHITE)

				Bukkit.getOnlinePlayers().forEach { player ->
					player.sendMessage(playerComponent.append(messageForPlayer(player, collected, messageParts)))
				}
			}
		}
	}
}
