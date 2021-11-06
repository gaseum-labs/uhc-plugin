package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Action
import com.codeland.uhc.discord.DataManager
import com.codeland.uhc.event.Chat
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import java.util.*

@CommandAlias("nick")
class NicknameCommand : BaseCommand() {
	private fun validNickname(nickname: String): String? {
		val allowedCharacters = ('a'..'z').toList() + ('0'..'9').toList() + '_'
		val filtered = nickname.lowercase().filter { it in allowedCharacters }
		return if (filtered.length < 2) null else nickname
	}

	private fun nicknameTaken(nickname: String): Boolean {
		return UHC.dataManager.nicknames.map.any { (_, list) ->
			list.any { nick -> nick == nickname }
		}
	}

	@Subcommand("add")
	@Description("add a nickname that can be used in mentions")
	fun add(sender: CommandSender, targetName: String, nickname: String) {
		val validated = validNickname(nickname)
			?: return Commands.errorMessage(sender, "Usernames must be at least two characters long and alphanumeric (including underscores)")

		if (nicknameTaken(validated))
			return Commands.errorMessage(sender, "That nickname is already taken by another player")

		val target = Bukkit.getOfflinePlayerIfCached(targetName)
			?: return Commands.errorMessage(sender, "Cannot find player by the name ${Commands.coloredInError(targetName, GRAY)}")

		Chat.addNick(target.uniqueId, validated)

		Action.sendGameMessage(sender, "${Util.coloredInGameMessage(targetName, GRAY)} can now be called ${Util.coloredInGameMessage(validated, GRAY)}")
	}

	@Subcommand("remove")
	@Description("remove a previously added nickname")
	fun remove(sender: CommandSender, targetName: String, nickname: String) {
		val target = Bukkit.getOfflinePlayerIfCached(targetName)
			?: return Commands.errorMessage(sender, "Cannot find player by the name ${Commands.coloredInError(targetName, GRAY)}")

		if (Chat.removeNick(target.uniqueId, nickname))
			Action.sendGameMessage(sender, "${Util.coloredInGameMessage(targetName, GRAY)} can no longer be called ${Util.coloredInGameMessage(nickname, GRAY)}")
		else
			Commands.errorMessage(sender, "${Commands.coloredInError(nickname, GRAY)} is not one of ${Commands.coloredInError(targetName, GRAY)}'s nicknames")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("list")
	@Description("list the nicknames of a player")
	fun list(sender: CommandSender, targetName: String) {
		val target = Bukkit.getOfflinePlayerIfCached(targetName)
			?: return Commands.errorMessage(sender, "Cannot find player by the name ${Commands.coloredInError(targetName, GRAY)}")

		val nickList = Chat.getNicks(target.uniqueId)

		if (nickList.isEmpty()) {
			Action.sendGameMessage(sender, "This player has no nicknames.")
		} else {
			Action.sendGameMessage(sender, "Nicknames for ${Util.coloredInGameMessage(targetName, GRAY)}:")
			nickList.forEach { nickname -> sender.sendMessage("$GOLD- $nickname") }
		}
	}
}