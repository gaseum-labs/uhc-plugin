package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.gaseumlabs.uhc.blockfix.BlockFixType
import org.gaseumlabs.uhc.command.Commands.errorMessage
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.customSpawning.CustomSpawning
import org.gaseumlabs.uhc.customSpawning.CustomSpawningType
import org.gaseumlabs.uhc.event.Portal
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.gaseumlabs.uhc.chc.chcs.carePackages.CarePackages
import org.gaseumlabs.uhc.util.Action
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.core.OfflineZombie
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.type.VeinFish
import java.util.*

@CommandAlias("uhct")
class TestCommands : BaseCommand() {
	@Subcommand("next")
	@Description("Manually go to the next round")
	fun testNext(sender: CommandSender) {
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
			sender.inventory.addItem(ItemStack(Material.values()[random.nextInt(Material.values().size)],
				random.nextInt(64) + 1))
		}
	}

	@Subcommand("deathswap warning")
	@Description("change the length of pre-swap warnings")
	fun testDsWarnings(sender: CommandSender, warning: Int) {

	}

	@Subcommand("deathswap immunity")
	@Description("change the length of the post-swap immunity period")
	fun testDsImmunity(sender: CommandSender, immunity: Int) {

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
			sender.sendMessage(info)
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
	@CommandCompletion("@uhcplayer")
	@Description("get this player's playerData")
	fun testPlayerData(sender: CommandSender, player: OfflinePlayer) {
		val playerData = PlayerData.get(player.uniqueId)
		val team = UHC.getTeams().playersTeam(player.uniqueId)

		Action.sendGameMessage(sender, "PlayerData for ${player.name}:")
		Action.sendGameMessage(sender, "Participating: ${playerData.participating}")
		Action.sendGameMessage(sender, "Alive: ${playerData.alive}")
		Action.sendGameMessage(sender, "Opting Out: ${playerData.optingOut}")
		Action.sendGameMessage(sender, "Last Played: ${playerData.lastPlayed}")
		Action.sendGameMessage(sender, "Arena: ${ArenaManager.playersArena(player.uniqueId)}")
		sender.sendMessage(Component.text("Team: ", NamedTextColor.GOLD, TextDecoration.BOLD).append(
			team?.apply(team.grabName()) ?: Component.text("[Not on a team]", NamedTextColor.RED)
		))
	}

	@Subcommand("zombie")
	@Description("creates an afk zombie for a player, even if they are online")
	fun testZombie(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val onlinePlayer = player.player ?: return errorMessage(sender, "${player.name} is offline!")

		OfflineZombie.createZombie(onlinePlayer)

		Action.sendGameMessage(sender, "Created a zombie for ${player.name}")
	}

	@Subcommand("drop")
	@Description("drops the current care package immediately")
	fun testDrop(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val carePackages = UHC.game?.chc as? CarePackages
			?: return errorMessage(sender, "Care packages is not going!")

		val result = carePackages.forceDrop()

		if (!result) return errorMessage(sender, "All care packages have been dropped!")
	}

	@Subcommand("mobcap")
	@CommandCompletion("@uhcplayer")
	@Description("test a player's individual mobcap")
	fun testMobCap(sender: CommandSender, offlinePlayer: OfflinePlayer, type: CustomSpawningType) {
		val testPlayer = Bukkit.getPlayer(offlinePlayer.uniqueId)
			?: return errorMessage(sender, "${offlinePlayer.name} is not online")

		val number = CustomSpawning.calcPlayerMobs(type, testPlayer)

		Action.sendGameMessage(sender, "${testPlayer.name}'s ${type.name.lowercase()} mobcap: $number out of ${
			PlayerData.get(testPlayer.uniqueId).spawningData[type.ordinal].intCap()
		}")
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("pvpmatch")
	fun pvpMatch(sender: CommandSender, offlinePlayer1: OfflinePlayer, offlinePlayer2: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val player1 = Bukkit.getPlayer(offlinePlayer1.uniqueId) ?: return errorMessage(sender,
			"The Player named ${offlinePlayer1.name} could not be found")
		val player2 = Bukkit.getPlayer(offlinePlayer2.uniqueId) ?: return errorMessage(sender,
			"The Player named ${offlinePlayer2.name} could not be found")

		if (player1 === player2) return errorMessage(sender, "Must select two different players")

		if (ArenaManager.playersArena(player1.uniqueId) != null) return errorMessage(sender,
			"${player1.name} is already in a game")
		if (ArenaManager.playersArena(player2.uniqueId) != null) return errorMessage(sender,
			"${player2.name} is already in a game")

		PlayerData.get(player1.uniqueId).inLobbyPvpQueue = 0
		PlayerData.get(player2.uniqueId).inLobbyPvpQueue = 0

		ArenaManager.addNewArena(
			PvpArena(
				arrayListOf(arrayListOf(player1.uniqueId),
					arrayListOf(player2.uniqueId)
				),
				ArenaManager.nextCoords(),
				PvpQueue.TYPE_1V1)
		)

		Action.sendGameMessage(sender, "Started a match between ${player1.name} and ${player2.name}")
	}

	companion object {
		var flag = false
	}

	@Subcommand("flag")
	@Description("set the global debug flag")
	fun testFlag(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		flag = !flag
		Action.sendGameMessage(sender, "Set flag to $flag")
	}

	@Subcommand("killreward")
	fun testKillReward(sender: CommandSender) {
		if (Commands.opGuard(sender)) return
		sender as Player

		val game = UHC.game ?: return errorMessage(sender, "Game is not going")
		val killReward = game.config.killReward

		killReward.apply(sender.uniqueId,
			game.teams.playersTeam(sender.uniqueId)?.members ?: arrayListOf(),
			sender.location)
	}

	@Subcommand("trader")
	fun testTrader(sender: CommandSender) {
		if (Commands.opGuard(sender)) return
		if (sender !is Player) return
		val game = UHC.game ?: return

		val trader = game.trader.currentTrader
			?: return errorMessage(sender, "Trader not spawned")

		sender.teleport(trader.location)
	}

	@Subcommand("portal")
	fun testPortal(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val player = sender as? Player ?: return

		val game = UHC.game ?: return errorMessage(player, "Game is not going")
		Portal.onPlayerPortal(player)
	}

	@Subcommand("seeVeins")
	@CommandCompletion("@uhcregenresource")
	fun testSeeVeins(sender: CommandSender, resourceKey: String) {
		if (Commands.opGuard(sender)) return

		val player = sender as? Player ?: return
		val game = UHC.game ?: return

		val resource = ResourceId.byKeyName(resourceKey) ?: return
		val veins = game.globalResources.getVeinList(resource)

		veins.forEach { vein ->
			when (vein) {
				is VeinBlock -> {
					vein.blocks.forEach { block ->
						Action.sendGameMessage(player, "Vein at ${block.x}, ${block.y}, ${block.z} veins")
						val fallingBlock = block.world.spawnFallingBlock(block.location.add(0.5, 0.0, 0.5),
							Material.COPPER_BLOCK.createBlockData())
						fallingBlock.dropItem = false
						fallingBlock.isGlowing = true
						fallingBlock.setGravity(false)
					}
				}
				is VeinEntity -> {
					vein.entity.isGlowing = true
				}
				is VeinFish -> {
					vein.fish.forEach { it.isGlowing = true }
				}
				else -> {}
			}
		}

		Action.sendGameMessage(player, "Found ${veins.size} veins")
	}

	@Subcommand("clearVeins")
	@CommandCompletion("@uhcregenresource")
	fun testClearVeins(sender: CommandSender, resourceKey: String) {
		if (Commands.opGuard(sender)) return

		val player = sender as? Player ?: return
		val game = UHC.game ?: return
		val resource = ResourceId.byKeyName(resourceKey) ?: return

		val veins = game.globalResources.getVeinList(resource)
		val size = veins.size
		veins.removeIf { vein -> vein.erase(); true }

		Action.sendGameMessage(player, "Deleted $size veins")
	}

	@Subcommand("veinData")
	@CommandCompletion("@uhcregenresource")
	fun testVeinData(sender: CommandSender, resourceKey: String) {
		if (Commands.opGuard(sender)) return

		val player = sender as? Player ?: return
		val game = UHC.game ?: return
		val team = game.teams.playersTeam(player.uniqueId) ?: return

		val resource = ResourceId.byKeyName(resourceKey) ?: return
		val veinData = game.globalResources.getTeamVeinData(team, resource)

		Action.sendGameMessage(player, "Veindata for ${resource.prettyName}:")
		Action.sendGameMessage(player, "Num Released: ${game.globalResources.releasedCurrently(game, resource)}")
		Action.sendGameMessage(player, "Collected: ${veinData.collected.getOrDefault(game.phase.phaseType, 0)}")
	}

	@Subcommand("setVeinCollected")
	@CommandCompletion("@uhcregenresource")
	fun testSetVeinCollected(sender: CommandSender, resourceKey: String, amount: Int) {
		if (Commands.opGuard(sender)) return

		val player = sender as? Player ?: return
		val game = UHC.game ?: return
		val team = game.teams.playersTeam(player.uniqueId) ?: return
		val resource = ResourceId.byKeyName(resourceKey) ?: return

		val veinData = game.globalResources.getTeamVeinData(team, resource)
		veinData.collected[game.phase.phaseType] = amount

		Action.sendGameMessage(player, "Set collected for ${resource.prettyName} to $amount")
	}

	@Subcommand("refreshLinks")
	fun refreshLinksCommand(sender: CommandSender) {
		UHC.dataManager.linkData.massPlayersLink()
	}

	@Subcommand("surfacescan")
	fun surfaceScanCommand(sender: CommandSender) {
		val player = sender as? Player ?: return

		val chunk = player.chunk
		val genBounds = RegenUtil.GenBounds.fromChunk(chunk)

		RegenUtil.superSurfaceSpreader(genBounds) { true }.forEach { block ->
			block.setType(Material.GOLD_BLOCK, false)
		}
	}
}