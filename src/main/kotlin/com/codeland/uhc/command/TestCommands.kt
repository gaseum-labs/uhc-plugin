package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.command.ubt.PartialUBT
import com.codeland.uhc.command.ubt.UBT
import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.waiting.PvpData
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.CarePackages
import com.codeland.uhc.quirk.quirks.Deathswap
import com.codeland.uhc.quirk.quirks.LowGravity
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uhct")
class TestCommands : BaseCommand() {
	@Subcommand("next")
	@Description("Manually go to the next round")
	fun testNext(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		if (GameRunner.uhc.isPhase(PhaseType.WAITING))
			Commands.errorMessage(sender, "In waiting phase, use /start instead")
		else
			GameRunner.uhc.startNextPhase()
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
		Deathswap.doSwaps()
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
			GameRunner.sendGameMessage(sender, info)
		}
	}

	@Subcommand("elapsed")
	@Description("gets how long this UHC has been going for")
	fun testElapsed(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player

		GameRunner.sendGameMessage(sender, "Elapsed time: ${GameRunner.uhc.elapsedTime}")
	}

	@Subcommand("teams")
	@Description("gives an overview of teams")
	fun testTeams(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val teams = TeamData.teams

		teams.forEach { team ->
			GameRunner.sendGameMessage(sender, team.colorPair.colorString(team.displayName))
			team.members.forEach { uuid ->
				val player = Bukkit.getOfflinePlayer(uuid)
				GameRunner.sendGameMessage(sender, team.colorPair.colorString(player.name ?: "NULL"))
			}
		}

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if ((playerData.participating || playerData.staged) && !TeamData.isOnTeam(uuid)) {
				val player = Bukkit.getOfflinePlayer(uuid)
				GameRunner.sendGameMessage(sender, player.name ?: "NULL")
			}
		}
	}

	@Subcommand("playerData")
	@Description("get this player's playerData")
	fun testPlayerData(sender: CommandSender, player: OfflinePlayer) {
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		GameRunner.sendGameMessage(sender, "PlayerData for ${player.name}:")
		GameRunner.sendGameMessage(sender, "Staged: ${playerData.staged}")
		GameRunner.sendGameMessage(sender, "Participating: ${playerData.participating}")
		GameRunner.sendGameMessage(sender, "Alive: ${playerData.alive}")
		GameRunner.sendGameMessage(sender, "Opting Out: ${playerData.optingOut}")
		GameRunner.sendGameMessage(sender, "In Lobby PVP: ${playerData.lobbyPVP.inPvp}")
	}

	@Subcommand("zombie")
	@Description("creates an afk zombie for a player, even if they are online")
	fun testZombie(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val onlinePlayer = player.player ?: return Commands.errorMessage(sender, "${player.name} is offline!")

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		playerData.createZombie(onlinePlayer)

		GameRunner.sendGameMessage(sender, "Created a zombie for ${player.name}")
	}

	@Subcommand("drop")
	@Description("drops the current care package immediately")
	fun testDrop(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val carePackages = GameRunner.uhc.getQuirk(QuirkType.CARE_PACKAGES) as CarePackages

		if (!carePackages.enabled) return Commands.errorMessage(sender, "Care packages is not going!")

		val result = carePackages.forceDrop()

		if (!result) return Commands.errorMessage(sender, "All care packages have been dropped!")
	}

	@Subcommand("mobcaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		sender as Player

		GameRunner.sendGameMessage(sender, "Monster spawn limit: ${sender.world.monsterSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Animal spawn limit: ${sender.world.animalSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Ambient spawn limit: ${sender.world.ambientSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Water animal spawn limit: ${sender.world.waterAnimalSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Water ambient spawn limit: ${sender.world.waterAmbientSpawnLimit}")
	}

	@Subcommand("mobcap")
	@Description("test a player's individual mobcap")
	fun testMobCap(sender: CommandSender, player: Player) {
		val playerMobs = CustomSpawning.calcPlayerMobs(player)

		GameRunner.sendGameMessage(sender, "${player.name}'s mobcap: ${PlayerData.getPlayerData(player.uniqueId).mobcap} | filled with ${playerMobs.first} representing ${playerMobs.second} of the total")
	}

	@Subcommand("killstreak")
	@Description("test a player's individual mobcap")
	fun testKillstreak(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player
		val pvpData = PlayerData.getLobbyPvp(sender.uniqueId)

		if (pvpData.inPvp) {
			PvpData.onKill(sender)
			GameRunner.sendGameMessage(sender, "Killstreak increased to ${pvpData.killstreak}")
		} else {
			Commands.errorMessage(sender, "You are not in PVP!")
		}
	}

	@Subcommand("gbs")
	fun gbs(sender: CommandSender, location: Location) {
		val originalString = location.world.getBlockAt(location).blockData.getAsString(true)

		val string = UBT.NBTStringToString(originalString.substring(originalString.indexOf('[')))
		Util.log(string)

		val nbtString = UBT.stringToNBTString(string)
		Util.log(nbtString)
	}

	@Subcommand("test sbs")
	fun sbs(sender: CommandSender, location: Location, blockData: String) {
		location.world.getBlockAt(location).setBlockData(Bukkit.createBlockData(blockData), false)
	}

	@Subcommand("ubt corner0")
	fun ubtCorner0(sender: CommandSender, x: Int, y: Int, z: Int) {
		val partialUBT = PartialUBT.getPlayersPartialUBT(sender as Player)
		partialUBT.setCorner0(x, y, z)
	}

	@Subcommand("ubt corner1")
	fun ubtCorner1(sender: CommandSender, x: Int, y: Int, z: Int) {
		val partialUBT = PartialUBT.getPlayersPartialUBT(sender as Player)
		partialUBT.setCorner1(x, y, z)
	}

	@Subcommand("ubt save")
	fun ubtCorner(sender: CommandSender) {
		sender as Player
		val world = sender.world

		val partialUBT = PartialUBT.getPlayersPartialUBT(sender)

		var headerStr = "${partialUBT.width()};${partialUBT.height()};${partialUBT.depth()};"
		var dataStr = ""

		val blockMap = HashMap<String, Short>()
		var numBlocks = 0

		for (x in partialUBT.corner0X..partialUBT.corner1X) {
			for (y in partialUBT.corner0Y..partialUBT.corner1Y) {
				for (z in partialUBT.corner0Z..partialUBT.corner1Z) {
					val block = world.getBlockAt(x, y, z)

					val materialName = block.type.key.key
					var id = blockMap.get(materialName)

					if (id == null) {
						id = numBlocks.toShort()
						blockMap.set(materialName, id)
						++numBlocks
					}

					var blockString = block.blockData.asString.substringAfter(':')
					val bracketIndex = blockString.indexOf('[')

					blockString = if (bracketIndex == -1) {
						id.toString()
					} else {
						id.toString() + blockString.substring(bracketIndex)
					}

					dataStr += "$blockString;"
				}
			}
		}

		headerStr += "$numBlocks;"

		val numberIter = blockMap.values.iterator()
		val materialIter = blockMap.keys.iterator()

		for (i in 0 until numBlocks) {
			headerStr += "${numberIter.next()}-${materialIter.next()};"
		}

		Util.log(headerStr + dataStr)
	}
}
