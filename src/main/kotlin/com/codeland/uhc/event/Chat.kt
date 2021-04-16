package com.codeland.uhc.event

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.lobbyPvp.PvpData
import com.codeland.uhc.team.Team
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
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

	private fun addMentions(event: AsyncChatEvent) {
		fun findAll(message: String, name: String): MutableList<Int> {
			val lowerMessage = message.toLowerCase()
			val lowerName = name.toLowerCase()
			val list = mutableListOf<Int>()
			var index = lowerMessage.indexOf(lowerName)
			while (index != -1) {
				if (index + name.length >= lowerMessage.length || lowerMessage[index + name.length] !in 'a'..'z')
					list.add(index)
				index = lowerMessage.indexOf(lowerName, index + 1)
			}
			return list
		}

		fun colorAll(message: String, coloring: Coloring, target: String): String {
			val mentions = findAll(message, target)
			var offset = 0
			var newMessage = message
			for (m in mentions) {
				newMessage = newMessage.substring(0, m + offset) + coloring(target) + ChatColor.WHITE + newMessage.substring(m + offset + target.length, newMessage.length)
				offset += coloring(target).length - target.length + 2
			}
			return newMessage
		}

		event.isCancelled = true

		val sendersTeam = TeamData.playersTeam(event.player.uniqueId)
		val playerPart = if (sendersTeam == null)
			"<${event.player.name}>"
		else
			sendersTeam.colorPair.colorString("<${event.player.name}>")

		for (p in Bukkit.getOnlinePlayers()) {
			var message = event.message
			var mentioned = false
			for (dynamic in mentions()) {
				val mention = "@" + dynamic.name
				val mentions = findAll(message, mention)
				if (mentions.size > 0 && (!dynamic.needsOp || event.player.isOp)) {
					var coloring = dynamic.coloring
					var c: Coloring
					c = if (dynamic.includes(p)) {
						mentioned = true
						{ underline(coloring(it)) }
					} else coloring
					message = colorAll(message, c, mention)
				}
			}

			if (mentioned) p.playSound(p.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5f, 5f)
			p.sendMessage("$playerPart ${ChatColor.RESET}$message")
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

	private fun replaceMentions(message: String, player: Player, mentions: ArrayList<Triple<Mention, Int, Int>>): Component {
		val usedMentions = mentions.filter { (mention, _, _) -> mention.includes(player) }

		/* begin with the part before the first mention */
		var replacedMessage = Component.text(message.substring(0, if (usedMentions.isEmpty()) message.length else usedMentions[0].second))

		for (i in usedMentions.indices) {
			val (mention, startIndex, endIndex) = mentions[i]

			/* add the mention, which is replaced */
			replacedMessage = replacedMessage.append(mention.generate(message.substring(startIndex, endIndex)))

			/* the part after the mention until the next mention, or until the end of the message */
			.append(Component.text(message.substring(endIndex, if (i == usedMentions.lastIndex) message.length else usedMentions[i + 1].second)))
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

	@EventHandler
	fun onMessage(event: AsyncChatEvent) {
		/* only modify chat when game is running */
		if (!GameRunner.uhc.isGameGoing()) {
			addMentions(event)
			return
		}

		/* only modify chat behavior with players on teams */
		val team = TeamData.playersTeam(event.player.uniqueId) ?: return

		fun firstIsMention(message: String): Boolean {
			if (message.startsWith("@")) {
				for (dynamic in mentions()) {
					if (message.length >= dynamic.name.length + 1 && message.substring(0, dynamic.name.length + 1) == "@" + dynamic.name) return true
				}
			}
			return false
		}

		if (event.message.startsWith("!") || firstIsMention(event.message)) {
			/* prevent blank global messages */
			if (event.message.length == 1)
				event.isCancelled = true
			else
				if (event.message.startsWith("!"))
					event.message = event.message.substring(1)

			addMentions(event)

		} else {
			event.isCancelled = true

			val component = "${team.colorPair.colorString("<${event.player.name}>")} ${team.colorPair.color0}${event.message}"

			team.members.forEach { member ->
				val player = Bukkit.getPlayer(member)
				player?.sendMessage(component)
			}
		}
	}
}