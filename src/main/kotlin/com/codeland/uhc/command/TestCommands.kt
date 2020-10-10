package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.command.ubt.PartialUBT
import com.codeland.uhc.command.ubt.UBT
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.quirk.quirks.LowGravity
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uhca test")
class TestCommands : BaseCommand() {
	@CommandAlias("test next")
	@Description("Manually go to the next round")
	fun testNext(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		if (GameRunner.uhc.isPhase(PhaseType.WAITING))
			Commands.errorMessage(sender, "In waiting phase, use /start instead")
		else
			GameRunner.uhc.startNextPhase()
	}

	@CommandAlias("test gravity")
	@Description("change the gravity constant")
	fun testGravity(sender: CommandSender, gravity: Double) {
		LowGravity.gravity = gravity
	}

	@CommandAlias("test insomnia")
	@Description("get the insomnia of the sender")
	fun testExhaustion(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player
		sender.sendMessage("${sender.name}'s insomnia: ${sender.getStatistic(Statistic.TIME_SINCE_REST)}")
	}

	@CommandAlias("test blockFix")
	@Description("gets when the next apple will drop for you")
	fun testBlockFix(sender: CommandSender, blockFixType: BlockFixType) {
		if (Commands.opGuard(sender)) return
		sender as Player

		blockFixType.blockFix.getInfoString(sender) { info ->
			GameRunner.sendGameMessage(sender, info)
		}
	}

	@CommandAlias("test elapsed")
	@Description("gets how long this UHC has been going for")
	fun testElapsed(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player

		GameRunner.sendGameMessage(sender, "Elapsed time: ${GameRunner.uhc.elapsedTime}")
	}

	@CommandAlias("test teams")
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
	}

	@CommandAlias("test alive")
	fun testAlive(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		GameRunner.sendGameMessage(sender, "${player.name} is alive: ${GameRunner.uhc.isAlive(player.uniqueId)}")
	}

	@CommandAlias("test participating")
	fun testParticipating(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		GameRunner.sendGameMessage(sender, "${player.name} is participating: ${GameRunner.uhc.isParticipating(player.uniqueId)}")
	}

	@CommandAlias("test gbs")
	fun gbs(sender: CommandSender, location: Location) {
		val originalString = location.world.getBlockAt(location).blockData.getAsString(true)

		val string = UBT.NBTStringToString(originalString.substring(originalString.indexOf('[')))
		Util.log(string)

		val nbtString = UBT.stringToNBTString(string)
		Util.log(nbtString)
	}

	@CommandAlias("test sbs")
	fun sbs(sender: CommandSender, location: Location, blockData: String) {
		location.world.getBlockAt(location).setBlockData(Bukkit.createBlockData(blockData), false)
	}

	@CommandAlias("test ubt corner0")
	fun ubtCorner0(sender: CommandSender, x: Int, y: Int, z: Int) {
		val partialUBT = PartialUBT.getPlayersPartialUBT(sender as Player)
		partialUBT.setCorner0(x, y, z)
	}

	@CommandAlias("test ubt corner1")
	fun ubtCorner1(sender: CommandSender, x: Int, y: Int, z: Int) {
		val partialUBT = PartialUBT.getPlayersPartialUBT(sender as Player)
		partialUBT.setCorner1(x, y, z)
	}

	@CommandAlias("test ubt save")
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
