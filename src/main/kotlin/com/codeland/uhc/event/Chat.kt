package com.codeland.uhc.event

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.team.Team
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList

class Chat : Listener {
	companion object {
		val NICKANAME_FILEPATH = "./nicknames.txt"

		val nickMap = mutableMapOf<UUID, MutableList<String>>()

		fun loadFile() {
			val file = File(NICKANAME_FILEPATH)
			if (!file.exists()) return

			FileReader(file).use { reader ->
				val lines = reader.readLines().map { it.split(",") }

				lines.forEach { line ->
					nickMap[UUID.fromString(line.first())] = MutableList(line.size - 1) { line[it + 1] }
				}
			}
		}

		fun saveFile() {
			FileWriter(File(NICKANAME_FILEPATH), false).use { writer ->
				nickMap.entries.forEach { (key, value) -> writer.append("${key},${value.joinToString(",")}\n") }
			}
		}

		fun addNick(player: UUID, nickname: String) {
			getNicks(player).add(nickname)
		}

		fun removeNick(player: UUID, nickname: String) {
			val lowerNickname = nickname.toLowerCase()

			getNicks(player).removeIf { it.toLowerCase() == lowerNickname }
		}

		fun getNicks(player: UUID): MutableList<String> {
			return nickMap.getOrPut(player) { mutableListOf() }
		}

		fun defaultGenerator(string: String): Component {
			return Component.text(string).style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))
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

		val mentionPvp = object : Mention {
			override fun matches() = "pvp"
			override fun includes(player: Player) = PlayerData.getLobbyPvp(player.uniqueId).inPvp
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

				if (iMatch == mention.matches().length) {
					if (numRemaining == 1) {
						return Pair(mention, iMessage)

					} else {
						remaining[j] = false
						if (--numRemaining == 0) return null
					}
				} else if (mention.matches()[iMatch] != message[iMessage]) {
					remaining[j] = false
					if (--numRemaining == 0) return null
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

	private fun replaceMentions(message: String, player: Player, collected: ArrayList<Triple<Mention, Int, Int>>, chatColor: TextColor): Component {
		val used = collected.filter { (mention, _, _) -> mention.includes(player) }

		/* ping player, they are mentioned */
		if (used.isNotEmpty()) player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5f, 5f)

		/* begin with the part before the first mention */
		var replacedMessage = Component.text(message.substring(0, if (used.isEmpty()) message.length else used[0].second)).style(Style.style(chatColor))

		for (i in used.indices) {
			val (mention, startIndex, endIndex) = collected[i]

			/* add the mention, which is replaced */
			replacedMessage = replacedMessage.append(mention.generate(message.substring(startIndex, endIndex)))

			/* the part after the mention until the next mention, or until the end of the message */
			.append(Component.text(message.substring(endIndex, if (i == used.lastIndex) message.length else used[i + 1].second))).style(Style.style(chatColor))
		}

		return replacedMessage
	}

	private fun generateMentions(): ArrayList<Mention> {
		val list = arrayListOf(
			mentionEveryone,
			mentionOp,
			mentionParticipating,
			mentionPvp,
			mentionSpectating
		)

		list.addAll(Bukkit.getOnlinePlayers().map { PlayerMention(it) })

		list.addAll(TeamData.teams.map { TeamMention(it) })

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

		/* filter messages only to teammates */
		/* if the sending player is on a team */
		/* if the message does not start with a mention */
		/* and the message does not start with ! */
		if (GameRunner.uhc.isGameGoing() && team != null && collected.firstOrNull()?.second != 0 && !message.startsWith("!")) {
			team.members.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
				player.sendMessage(playerComponent.append(replaceMentions(message, player, collected, team.color1)))
			}

		/* regular chat behavior before game */
		} else {
			Bukkit.getOnlinePlayers().forEach { player ->
				player.sendMessage(playerComponent.append(replaceMentions(if (message.startsWith("!")) message.substring(1) else message, player, collected, NamedTextColor.WHITE)))
			}
		}
	}
}
