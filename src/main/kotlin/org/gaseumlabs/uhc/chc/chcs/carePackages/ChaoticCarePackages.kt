package org.gaseumlabs.uhc.chc.chcs.carePackages

import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.phases.Endgame
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.util.Action
import org.bukkit.*
import org.bukkit.block.Block
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.phase.phases.Grace
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

class ChaoticCarePackages : CHC<Array<Int>>() {
	private val itemsList = genItemsList(CarePackageUtil.genReferenceItems())

	private val PER_CHEST = 13
	private val NUM_DROPS = ceil(itemsList.size / 13.0).toInt()

	/**
	 * time that drops are spread out over
	 * grace + shrink - 10 minutes
	 * subtract any time that has already happened if quirk started late
	 */
	private var TIME = 0
	private var TIME_PER_DROP = 0

	var taskId = -1
	var timer = 0
	var dropNum = 0

	override fun customDestroy(game: Game) {
		stopDropping()
	}

	override fun onPhaseSwitch(game: Game, phase: Phase) {
		if (phase is Grace) {
			TIME = (game.config.graceTime + game.config.shrinkTime - 600) * 20
			TIME_PER_DROP = TIME / NUM_DROPS
			startDropping(game)
		}
		if (phase is Endgame) stopDropping()
	}

	override fun onStartPlayer(game: Game, uuid: UUID) {
		val playerData = PlayerData.get(uuid)
		playerData.setQuirkData(this, genPlayerIndices(itemsList))
	}

	override fun defaultData() = emptyArray<Int>()

	private fun startDropping(game: Game) {
		timer = 0
		dropNum = 0
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(org.gaseumlabs.uhc.UHCPlugin.plugin, { tick(game) }, 0, 1)
	}

	private fun stopDropping() {
		Bukkit.getScheduler().cancelTask(taskId)
	}

	private fun tick(game: Game) {
		++timer

		if (timer >= TIME_PER_DROP) {
			if (dropNum >= NUM_DROPS) return stopDropping()

			PlayerData.playerDataList.filter { (_, playerData) ->
				playerData.participating
			}.forEach { (uuid, playerData) ->
				val indexList = playerData.getQuirkData(this)

				if (indexList.isNotEmpty()) {
					val block = chaoticDropBlock(game, game.world)

					val inventory = CarePackageUtil.generateChest(game.world,
						block,
						ChatColor.values()[Random.nextInt(ChatColor.MAGIC.ordinal)])

					chestSlots(PER_CHEST) { index, slot ->
						val itemIndex = index + dropNum * PER_CHEST
						if (itemIndex < indexList.size) inventory.setItem(slot,
							itemsList[indexList[itemIndex]].create())
					}

					val player = Bukkit.getPlayer(uuid)
					if (player != null) {
						Action.sendGameMessage(player, "Care package dropped at ${block.x} ${block.y} ${block.z}")
					}
				}
			}

			++dropNum
			timer = 0
		}
	}

	/**
	 * @return the block that a chest or spire will generate at
	 *
	 * picks the space randomly within the worldborder of the given world
	 *
	 * avoids the center radius
	 */
	private fun chaoticDropBlock(game: Game, world: World): Block {
		val maxRadius = (((world.worldBorder.size - 1.0) / 2.0) - 10.0).roundToInt()
		val minRadius = game.config.battlegroundRadius / 2

		return when (Random.nextInt(4)) {
			0 -> CarePackageUtil.dropBlock(world,
				Random.nextInt(-maxRadius, minRadius),
				Random.nextInt(-maxRadius, -minRadius))
			1 -> CarePackageUtil.dropBlock(world,
				Random.nextInt(minRadius, maxRadius),
				Random.nextInt(-maxRadius, minRadius))
			2 -> CarePackageUtil.dropBlock(world,
				Random.nextInt(-minRadius, maxRadius),
				Random.nextInt(minRadius, maxRadius))
			else -> CarePackageUtil.dropBlock(world,
				Random.nextInt(-maxRadius, -minRadius),
				Random.nextInt(-minRadius, maxRadius))
		}
	}

	private inline fun chestSlots(num: Int, on: (Int, Int) -> Unit) {
		val slots = Array(9 * 3) { it }
		slots.shuffle()
		for (i in 0 until num) on(i, slots[i])
	}

	private fun genItemsList(reference: Array<CarePackageUtil.ItemReference>): ArrayList<ItemCreator> {
		val generates = ArrayList<ItemCreator>(256)

		reference.forEach { playerItem ->
			while (playerItem.remaining > 0) {
				val amount = Random.nextInt(playerItem.min, playerItem.max + 1).coerceAtMost(playerItem.remaining)
				generates.add(playerItem.create.amount(amount))
				playerItem.remaining -= amount
			}
		}

		return generates
	}

	private fun genPlayerIndices(itemsList: ArrayList<ItemCreator>): Array<Int> {
		val ret = Array(itemsList.size) { it }
		ret.shuffle()
		return ret
	}
}
