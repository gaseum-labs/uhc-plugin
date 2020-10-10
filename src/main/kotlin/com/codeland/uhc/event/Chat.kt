package com.codeland.uhc.event

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

typealias Coloring = (String) -> String

class Chat : Listener {
	companion object {
		fun solid(chatColor: ChatColor): Coloring {
			// don't ask
			return { chatColor.toString() + it.replace("@", "@$chatColor") }
		}

		fun loadFile() {
			val file = File("./nicknames.txt")
			if (!file.exists()) {
				return
			}
			FileReader(file).use { reader ->
				val list = reader.readLines().map { it.split(",")}
				list.forEach { l ->
					nickMap[UUID.fromString(l.first())] = MutableList(l.size - 1) {l.takeLast(l.size - 1)[it]}
				}
			}
		}

		fun saveFile() {
			val file = File("./nicknames.txt")
			file.writeText("")
			FileWriter(file).use { writer ->
				nickMap.entries.forEach { e ->
					writer.append(e.key.toString() + "," + e.value.joinToString(separator = ",") + "\n")
				}
			}
		}

		fun underline(string: String): String {
			var newString = string
			newString = ChatColor.UNDERLINE.toString() + newString
			var index = newString.indexOf(ChatColor.COLOR_CHAR)
			while (index != -1) {
				newString = newString.substring(0, index + 2) + ChatColor.UNDERLINE + newString.substring(index + 2, newString.length)
				index = newString.indexOf(ChatColor.COLOR_CHAR, index + 3)
			}
			return newString
		}

		val nickMap = mutableMapOf<UUID, MutableList<String>>()

		fun addNick(player: Player, nickname: String) {
			getNicks(player).add(nickname)
		}

		fun removeNick(player: Player, nickname: String) {
			getNicks(player).removeIf {
				it.toLowerCase() == nickname.toLowerCase()
			}
		}

		fun getNicks(player: Player): MutableList<String> {
			return nickMap[player.uniqueId] ?: {
				nickMap[player.uniqueId] = mutableListOf()
				nickMap[player.uniqueId]!!
			}()
		}
	}

	class SpecialMention(val name: String, val includes: (Player) -> Boolean, val needsOp: Boolean = false, val coloring: Coloring = solid(ChatColor.GOLD))

	private val mentions = {
		val list = mutableListOf<SpecialMention>()

		list.addAll(mutableListOf(
				SpecialMention("everyone", { true }, needsOp = true),
				SpecialMention("players", { p -> p.gameMode == GameMode.SURVIVAL }),
				SpecialMention("spectators", { p -> p.gameMode == GameMode.SPECTATOR }),
				SpecialMention("admins", { p -> p.isOp})
		))

		for (c in 0 until TeamData.teamColors.size * TeamData.teamColors.size) {
			val colorPair = TeamData.colorPairPermutation(c) ?: continue
			list += SpecialMention(colorPair.getName().replace(" ", "").toLowerCase(), { p -> TeamData.playersTeam(p.uniqueId)?.colorPair == colorPair}, coloring = colorPair::colorString)
		}

		list.addAll(
			Bukkit.getOnlinePlayers().map { p ->
				SpecialMention(name = p.name, coloring = TeamData.playersColor(p.uniqueId), includes = { it == p})
			}
		)

		for (e in nickMap.entries.filter { Bukkit.getPlayer(it.key) != null }) {
			list.addAll(e.value.map { nickname ->
				val player = Bukkit.getPlayer(e.key)!!
				SpecialMention(name = nickname, coloring = TeamData.playersColor(player.uniqueId), includes = { it == player})
			})
		}

		list.sortedByDescending { it.name.length }
	}

	private fun addMentions(event: AsyncPlayerChatEvent) {
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

	@EventHandler
	fun onMessage(event: AsyncPlayerChatEvent) {
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