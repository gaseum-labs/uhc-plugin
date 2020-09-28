package com.codeland.uhc.event

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.PhaseType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.lang.StringBuilder

typealias Coloring = (String) -> String

class Chat : Listener {

	companion object {
		fun solid(chatColor: ChatColor): Coloring {
			return { chatColor.toString() + it }
		}
	}

	class SpecialMention(val name: String, val includes: (Player) -> Boolean, val needsOp: Boolean = false, val coloring: Coloring = solid(ChatColor.GOLD))

	private val specialMentions = {
		val list = arrayListOf(
			SpecialMention("everyone", { true }, needsOp = true),
			SpecialMention("players", { p -> p.gameMode == GameMode.SURVIVAL }),
			SpecialMention("spectators", { p -> p.gameMode == GameMode.SPECTATOR }),
			SpecialMention("admins", { p -> p.isOp})
		)

		for (c in 0 until TeamData.teamColors.size * TeamData.teamColors.size) {
			val colorPair = TeamData.colorPairPermutation(c) ?: continue
			list += SpecialMention(colorPair.getName().replace(" ", "").toLowerCase(), { p -> TeamData.playersTeam(p)?.colorPair == colorPair}, coloring = colorPair::colorString)
		}

		list
	}()

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

		fun formatted(player: Player, message: String): String {
			return "<" + player.name + "> " + message
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

		event.isCancelled = true
		for (p in Bukkit.getOnlinePlayers()) {
			var message = event.message
			var mentioned = false
			for (player in Bukkit.getOnlinePlayers()) {
				val mention = "@" + player.name
				val mentions = findAll(message, mention)
				if (mentions.size > 0) {
					val colorPair = TeamData.playersTeam(player)?.colorPair
					var coloring = if (colorPair == null) solid(ChatColor.BLUE) else colorPair!!::colorString
					var c: Coloring
					c = if (p == player) {
						mentioned = true
						{ underline(coloring(it)) }
					} else coloring
					message = colorAll(message, c, mention)
				}
			}
			for (special in specialMentions) {
				val mention = "@" + special.name
				val mentions = findAll(message, mention)
				if (mentions.size > 0 && (!special.needsOp || event.player.isOp)) {
					var coloring = special.coloring
					var c: Coloring
					c = if (special.includes(p)) {
						mentioned = true
						{ underline(coloring(it)) }
					} else coloring
					message = colorAll(message, c, mention)
				}
			}
			if (mentioned) p.playSound(p.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5f, 5f)
			p.sendMessage(formatted(event.player, message))
		}
	}

	@EventHandler
	fun onMessage(event: AsyncPlayerChatEvent) {
		/* only modify chat when game is running */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING) || GameRunner.uhc.isPhase(PhaseType.POSTGAME)) {
			addMentions(event)
			return
		}

		/* only modify chat behavior with players on teams */
		val team = TeamData.playersTeam(event.player) ?: return

		fun firstIsMention(message: String): Boolean {
			for (player in Bukkit.getOnlinePlayers()) {
				if (message.length >= player.name.length + 1 && message.substring(0, player.name.length + 1) == "@" + player.name) return true
			}
			for (special in specialMentions) {
				if (message.length >= special.name.length + 1 && message.substring(0, special.name.length + 1) == "@" + special.name) return true
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

			val component = "${team.colorPair.color0}<${event.player.displayName}> ${org.bukkit.ChatColor.RESET}${event.message}"

			team.members.forEach { member ->
				member.player?.sendMessage(component)
			}
		}
	}
}