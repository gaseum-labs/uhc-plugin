package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.command.Commands.errorMessage
import com.codeland.uhc.core.*
import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.customSpawning.CustomSpawningType
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.carePackages.CarePackages
import com.codeland.uhc.quirk.quirks.Deathswap
import com.codeland.uhc.quirk.quirks.LowGravity
import com.codeland.uhc.util.Action
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@CommandAlias("uhct")
class TestCommands : BaseCommand() {
	@Subcommand("next")
	@Description("Manually go to the next round")
	fun testNext(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		val game = UHC.game

		if (game == null)
			errorMessage(sender, "Game has not started")
		else
			game.nextPhase()
	}

	@Subcommand("fill")
	@Description("fill your inventory with random items")
	fun testFill(sender: CommandSender) {
		if (Commands.opGuard(sender)) return
		sender as Player

		val random = Random()

		for (i in 0 until 500) {
			sender.inventory.addItem(ItemStack(Material.values()[random.nextInt(Material.values().size)], random.nextInt(64) + 1))
		}
	}

	@Subcommand("gravity")
	@Description("change the gravity constant")
	fun testGravity(sender: CommandSender, gravity: Double) {
		LowGravity.gravity = gravity
	}

	@Subcommand("deathswap warning")
	@Description("change the length of pre-swap warnings")
	fun testDsWarnings(sender: CommandSender, warning: Int) {

	}

	@Subcommand("deathswap immunity")
	@Description("change the length of the post-swap immunity period")
	fun testDsImmunity(sender: CommandSender, immunity: Int) {

	}

	@Subcommand("deathswap swap")
	@Description("swap all players")
	fun testDsSwap(sender: CommandSender) {
		val game = UHC.game ?: return errorMessage(sender, "Game has not started")
		Deathswap.doSwaps(game)
	}

	@Subcommand("insomnia")
	@Description("get the insomnia of the sender")
	fun testExhaustion(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player
		sender.sendMessage("${sender.name}'s insomnia: ${sender.getStatistic(Statistic.TIME_SINCE_REST)}")
	}

	@Subcommand("blockFix")
	@Description("gets when the next apple will drop for you")
	fun testBlockFix(sender: CommandSender, blockFixType: BlockFixType) {
		if (Commands.opGuard(sender)) return
		sender as Player

		blockFixType.blockFix.getInfoString(sender) { info ->
			Action.sendGameMessage(sender, info)
		}
	}

	@Subcommand("elapsed")
	@Description("gets how long this UHC has been going for")
	fun testElapsed(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player

		Action.sendGameMessage(sender, "Elapsed time: ${UHC.timer}")
	}

	@Subcommand("playerData")
	@Description("get this player's playerData")
	fun testPlayerData(sender: CommandSender, player: OfflinePlayer) {
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		Action.sendGameMessage(sender, "PlayerData for ${player.name}:")
		Action.sendGameMessage(sender, "Staged: ${playerData.staged}")
		Action.sendGameMessage(sender, "Participating: ${playerData.participating}")
		Action.sendGameMessage(sender, "Alive: ${playerData.alive}")
		Action.sendGameMessage(sender, "Opting Out: ${playerData.optingOut}")
		Action.sendGameMessage(sender, "Last Played: ${playerData.lastPlayed}")
	}

	@Subcommand("zombie")
	@Description("creates an afk zombie for a player, even if they are online")
	fun testZombie(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val onlinePlayer = player.player ?: return errorMessage(sender, "${player.name} is offline!")

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		playerData.createZombie(onlinePlayer)

		Action.sendGameMessage(sender, "Created a zombie for ${player.name}")
	}

	@Subcommand("drop")
	@Description("drops the current care package immediately")
	fun testDrop(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val carePackages = UHC.game?.getQuirk(QuirkType.CARE_PACKAGES) as? CarePackages
			?: return errorMessage(sender, "Care packages is not going!")

		val result = carePackages.forceDrop()

		if (!result) return errorMessage(sender, "All care packages have been dropped!")
	}

	@Subcommand("mobcaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		sender as Player

		Action.sendGameMessage(sender, "Monster spawn limit: ${sender.world.monsterSpawnLimit}")
		Action.sendGameMessage(sender, "Animal spawn limit: ${sender.world.animalSpawnLimit}")
		Action.sendGameMessage(sender, "Ambient spawn limit: ${sender.world.ambientSpawnLimit}")
		Action.sendGameMessage(sender, "Water animal spawn limit: ${sender.world.waterAnimalSpawnLimit}")
		Action.sendGameMessage(sender, "Water ambient spawn limit: ${sender.world.waterAmbientSpawnLimit}")
	}

	@Subcommand("mobcap")
	@Description("test a player's individual mobcap")
	fun testMobCap(sender: CommandSender, player: Player, type: CustomSpawningType) {
		val playerMobs = CustomSpawning.calcPlayerMobs(type, player)

		Action.sendGameMessage(sender, "${player.name}'s mobcap: ${PlayerData.getPlayerData(player.uniqueId).spawningData[type.ordinal].mobcap} | filled with ${playerMobs.first} representing ${playerMobs.second} of the total")
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("pvpmatch")
	fun pvpMatch(sender: CommandSender, offlinePlayer1: OfflinePlayer, offlinePlayer2: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val player1 = Bukkit.getPlayer(offlinePlayer1.uniqueId) ?: return errorMessage(sender, "The Player named ${offlinePlayer1.name} could not be found")
		val player2 = Bukkit.getPlayer(offlinePlayer2.uniqueId) ?: return errorMessage(sender, "The Player named ${offlinePlayer2.name} could not be found")

		if (player1 === player2) return errorMessage(sender, "Must select two different players")

		if (ArenaManager.playersArena(player1.uniqueId) != null) return errorMessage(sender, "${player1.name} is already in a game")
		if (ArenaManager.playersArena(player2.uniqueId) != null) return errorMessage(sender, "${player2.name} is already in a game")

		PlayerData.getPlayerData(player1.uniqueId).inLobbyPvpQueue.set(0)
		PlayerData.getPlayerData(player2.uniqueId).inLobbyPvpQueue.set(0)

		ArenaManager.addArena(
			PvpArena(
				arrayListOf(arrayListOf(player1.uniqueId),
				arrayListOf(player2.uniqueId)
			), PvpArena.TYPE_1V1)
		)

		Action.sendGameMessage(sender, "Started a match between ${player1.name} and ${player2.name}")
	}

	companion object {
		var flag = false
	}

	@Subcommand("flag")
	@Description("test a player's individual mobcap")
	fun testFlag(sender: CommandSender) {
		flag = !flag
		Action.sendGameMessage(sender, "Set flag to $flag")
	}
}
