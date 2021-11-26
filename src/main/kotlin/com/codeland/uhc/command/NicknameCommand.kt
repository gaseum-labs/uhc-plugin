package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.codeland.uhc.core.UHC
import com.codeland.uhc.database.DataManager
import com.codeland.uhc.database.file.NicknamesFile
import com.codeland.uhc.util.Action
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor.GOLD
import org.bukkit.ChatColor.GRAY
import org.bukkit.command.CommandSender

@CommandAlias("nick")
class NicknameCommand : BaseCommand() {
	private fun validNickname(nickname: String): String? {
		val allowedCharacters = ('a'..'z').toList() + ('0'..'9').toList() + '_'
		val filtered = nickname.lowercase().filter { it in allowedCharacters }
		return if (filtered.length < 2) null else nickname
	}

	@Subcommand("add")
	@CommandCompletion("@uhcplayer")
	@Description("add a nickname that can be used in mentions")
	fun add(sender: CommandSender, targetName: String, nickname: String) {
		val validated = validNickname(nickname)
			?: return Commands.errorMessage(sender,
				"Usernames must be at least two characters long and alphanumeric (including underscores)")

		val target = Bukkit.getOfflinePlayerIfCached(targetName)
			?: return Commands.errorMessage(sender,
				"Cannot find player by the name ${Commands.coloredInError(targetName, GRAY)}")

		if (UHC.dataManager.nicknames.addNick(target.uniqueId, validated)) {
			UHC.dataManager.push(DataManager.nicknamesFile, NicknamesFile.NicknameEntry(target.uniqueId, validated))
			Action.sendGameMessage(sender,
				"${Util.coloredInGameMessage(targetName, GRAY)} can now be called ${
					Util.coloredInGameMessage(validated,
						GRAY)
				}")

		} else {
			Commands.errorMessage(sender, "That nickname already exists for ${target.name}")
		}
	}

	@Subcommand("remove")
	@CommandCompletion("@uhcplayer")
	@Description("remove a previously added nickname")
	fun remove(sender: CommandSender, targetName: String, nickname: String) {
		val target = Bukkit.getOfflinePlayerIfCached(targetName)
			?: return Commands.errorMessage(sender,
				"Cannot find player by the name ${Commands.coloredInError(targetName, GRAY)}")

		if (UHC.dataManager.nicknames.removeNick(target.uniqueId, nickname)) {
			UHC.dataManager.remove(DataManager.nicknamesFile, NicknamesFile.NicknameEntry(target.uniqueId, nickname))
			Action.sendGameMessage(
				sender,
				"${Util.coloredInGameMessage(targetName, GRAY)} can no longer be called ${
					Util.coloredInGameMessage(nickname, GRAY)
				}"
			)
		} else {
			Commands.errorMessage(
				sender,
				"${Commands.coloredInError(nickname, GRAY)} is not one of ${
					Commands.coloredInError(targetName, GRAY)
				}'s nicknames"
			)
		}
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("list")
	@Description("list the nicknames of a player")
	fun list(sender: CommandSender, targetName: String) {
		val target = Bukkit.getOfflinePlayerIfCached(targetName)
			?: return Commands.errorMessage(sender,
				"Cannot find player by the name ${Commands.coloredInError(targetName, GRAY)}")

		val nickList = UHC.dataManager.nicknames.getNicks(target.uniqueId)

		if (nickList.isEmpty()) {
			Action.sendGameMessage(sender, "This player has no nicknames.")
		} else {
			Action.sendGameMessage(sender, "Nicknames for ${Util.coloredInGameMessage(targetName, GRAY)}:")
			nickList.forEach { nickname -> sender.sendMessage("$GOLD- $nickname") }
		}
	}
}