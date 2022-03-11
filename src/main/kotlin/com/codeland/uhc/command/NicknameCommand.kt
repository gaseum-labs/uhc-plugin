package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.codeland.uhc.core.UHC
import com.codeland.uhc.database.DataManager
import com.codeland.uhc.database.file.NicknamesFile
import com.codeland.uhc.util.Action
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Bukkit
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
				"Cannot find player by the name $targetName")

		if (UHC.dataManager.nicknames.addNick(target.uniqueId, validated)) {
			UHC.dataManager.push(DataManager.nicknamesFile, NicknamesFile.NicknameEntry(target.uniqueId, validated))
			sender.sendMessage(
				Component.text(targetName, GRAY)
					.append(Component.text(" can now be called ", GOLD))
					.append(Component.text(validated, GRAY))
			)

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
				"Cannot find player by the name $targetName"
			)

		if (UHC.dataManager.nicknames.removeNick(target.uniqueId, nickname)) {
			UHC.dataManager.remove(DataManager.nicknamesFile, NicknamesFile.NicknameEntry(target.uniqueId, nickname))
			sender.sendMessage(
				Component.text(targetName, GRAY)
					.append(Component.text(" can no longer be called ", GOLD))
					.append(Component.text(nickname, GRAY))
			)
		} else {
			Commands.errorMessage(
				sender, "$nickname is not one of ${targetName}'s nicknames"
			)
		}
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("list")
	@Description("list the nicknames of a player")
	fun list(sender: CommandSender, targetName: String) {
		val target = Bukkit.getOfflinePlayerIfCached(targetName)
			?: return Commands.errorMessage(sender,
				"Cannot find player by the name $targetName")

		val nickList = UHC.dataManager.nicknames.getNicks(target.uniqueId)

		if (nickList.isEmpty()) {
			Action.sendGameMessage(sender, "This player has no nicknames.")
		} else {
			sender.sendMessage(
				Component.text("Nicknames for", GOLD)
					.append(Component.text(targetName, GRAY))
					.append(Component.text(":", GRAY))
			)
			nickList.forEach { nickname -> sender.sendMessage(Component.text("- $nickname", GOLD)) }
		}
	}
}