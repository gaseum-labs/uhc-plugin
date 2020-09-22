package com.codeland.uhc.event

import com.codeland.uhc.team.TeamData
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.PhaseType
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.lang.StringBuilder

class Chat : Listener {
	// making it into a function so it'll work in the waiting area as well

	class SpecialMention(val name: String, val includes: (Player) -> Boolean, val needsOp: Boolean = false, val color: org.bukkit.ChatColor = org.bukkit.ChatColor.GOLD)

	private val specialMentions = {
		val l = mutableListOf(
			SpecialMention("everyone", { true }, needsOp = true),
			SpecialMention("players", { p -> p.gameMode == GameMode.SURVIVAL }),
			SpecialMention("spectators", { p -> p.gameMode == GameMode.SPECTATOR }),
			SpecialMention("admins", { p -> p.isOp})
		)
		for (c in TeamData.teamColors) {
			l += SpecialMention(TeamData.prettyTeamName(c).replace(" ", "").toLowerCase().substring(4), { p -> GameRunner.playersTeam(p.name)?.color == c}, color = c)
		}
		l
	}()

	private fun addMentions(event: AsyncPlayerChatEvent) {

		fun findAll(message: String, name: String): MutableList<Int> {
			val list = mutableListOf<Int>()
			var index = message.indexOf(name)
			while (index != -1) {
				list.add(index)
				index = message.indexOf(name, index + 1)
			}
			return list
		}

		fun prefixAll(message: String, prefix: String, target: String): String {
			val mentions = findAll(message, target)
			val builder = StringBuilder(message)
			var offset = 0
			for (m in mentions) {
				builder.insert(m + offset, prefix)
				offset += prefix.length
			}
			return builder.toString()
		}

		fun postfixAll(message: String, prefix: String, target: String): String {
			val mentions = findAll(message, target)
			val builder = StringBuilder(message)
			var offset = 0
			for (m in mentions) {
				builder.insert(m + target.length + offset, prefix)
				offset += prefix.length
			}
			return builder.toString()
		}

		fun formatted(player: Player, message: String): String {
			return "<" + player.name + "> " + message
		}

		event.isCancelled = true

		val mentioned = mutableListOf<Player>()
		val specialMentioned = mutableListOf<SpecialMention>()

		for (player in Bukkit.getOnlinePlayers()) {
			val mention = "@" + player.name
			if (event.message.contains(mention)) {
				var color = GameRunner.playersTeam(player.name)?.color
				if (color == null) color = org.bukkit.ChatColor.BLUE
				event.message = prefixAll(event.message, color.toString(), mention)
				event.message = postfixAll(event.message, org.bukkit.ChatColor.WHITE.toString(), mention)
				mentioned += player
			}
		}
		for (special in specialMentions) {
			val mention = "@" + special.name
			if (event.message.contains(mention) && (!special.needsOp || event.player.isOp)) {
				event.message = prefixAll(event.message, special.color.toString(), mention)
				event.message = postfixAll(event.message, org.bukkit.ChatColor.WHITE.toString(), mention)
				specialMentioned += special
			}
		}

		for (player in Bukkit.getOnlinePlayers()) {
			var forPlayer = event.message
			var wasMentioned = false
			if (player in mentioned) {
				wasMentioned = true
				forPlayer = prefixAll(forPlayer, org.bukkit.ChatColor.UNDERLINE.toString(), "@" + player.name)
			}
			for (special in specialMentioned) {
				if (special.includes(player)) {
					wasMentioned = true
					forPlayer = prefixAll(forPlayer, org.bukkit.ChatColor.UNDERLINE.toString(), "@" + special.name)
				}
			}
			if (wasMentioned) player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 2f)
			player.sendMessage(formatted(event.player, forPlayer))//
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
		val team = GameRunner.playersTeam(event.player.name) ?: return

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

			val component = "${team.color}<${event.player.displayName}> ${org.bukkit.ChatColor.RESET}${event.message}"

			team.entries.forEach { entry ->
				Bukkit.getPlayer(entry)?.sendMessage(component)
			}
		}
	}
}