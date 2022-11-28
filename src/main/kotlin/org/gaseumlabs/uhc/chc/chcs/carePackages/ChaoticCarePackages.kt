package org.gaseumlabs.uhc.chc.chcs.carePackages

import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.gaseumlabs.uhc.chc.NoDataCHC
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.phases.Endgame
import org.gaseumlabs.uhc.core.phase.phases.Grace
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.Util
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class ChaoticCarePackages : NoDataCHC() {
	private var itemsList = ArrayList<ItemCreator>()

	private val PER_CHEST = 13

	/**
	 * time that drops are spread out over: grace + shrink
	 */
	private var TIME = 0
	private var TIME_PER_DROP = 0
	private var NUM_DROPS = 0

	var taskId = -1
	var timer = 0
	var dropNum = 0

	private val chestTypes = arrayOf(
		Material.CHEST,
		Material.TRAPPED_CHEST,
		Material.BARREL,
	)

	override fun gamePreset() = GamePreset(
		KillReward.NONE,
		World.Environment.NORMAL,
		1.0f,
		72,
		600,
		600,
		600,
		150,
	)

	override fun customDestroy(game: Game) {
		stopDropping()
	}

	override fun onPhaseSwitch(game: Game, phase: Phase) {
		if (phase is Grace) {
			/* prep */
			val numPlayers = game.teams.teams().sumOf { it.members.size }
			itemsList = genItemsList(numPlayers)

			NUM_DROPS = Util.ceilDiv(itemsList.size, PER_CHEST)
			TIME = (game.preset.graceTime + game.preset.shrinkTime) * 20
			TIME_PER_DROP = TIME / NUM_DROPS

			startDropping(game)
		}
		if (phase is Endgame) stopDropping()
	}

	override fun onStartPlayer(game: Game, uuid: UUID) {}

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

		if (timer == TIME_PER_DROP) {
			if (dropNum == NUM_DROPS) return stopDropping()

			val block = chaoticDropBlock(game, game.world)

			val inventory = CarePackageUtil.generateChest(
				block,
				TextColor.color(Random.nextInt()),
				chestTypes.random()
			)

			chestSlots(PER_CHEST) { index, slot ->
				val itemIndex = index + dropNum * PER_CHEST
				if (itemIndex < itemsList.size) {
					inventory.setItem(slot, itemsList[itemIndex].create())
				}
			}

			game.world.players.forEach { player ->
				Action.sendGameMessage(player, "Care package dropped at ${block.x} ${block.y} ${block.z}")
			}

			++dropNum
			timer = 0
		}
	}

	/**
	 * @return the block that a chest will drop at
	 * picks the space randomly within the worldborder of the given world
	 * avoids the center radius
	 */
	private fun chaoticDropBlock(game: Game, world: World): Block {
		val maxRadius = (((world.worldBorder.size - 1.0) / 2.0) - 10.0).roundToInt()
		val minRadius = game.preset.battlegroundRadius / 2

		return when (Random.nextInt(4)) {
			0 -> CarePackageUtil.dropBlock(
				world,
				Random.nextInt(-maxRadius, minRadius),
				Random.nextInt(-maxRadius, -minRadius)
			)
			1 -> CarePackageUtil.dropBlock(
				world,
				Random.nextInt(minRadius, maxRadius),
				Random.nextInt(-maxRadius, minRadius)
			)
			2 -> CarePackageUtil.dropBlock(
				world,
				Random.nextInt(-minRadius, maxRadius),
				Random.nextInt(minRadius, maxRadius)
			)
			else -> CarePackageUtil.dropBlock(
				world,
				Random.nextInt(-maxRadius, -minRadius),
				Random.nextInt(-minRadius, maxRadius)
			)
		}
	}

	private inline fun chestSlots(num: Int, on: (Int, Int) -> Unit) {
		val slots = Array(9 * 3) { it }
		slots.shuffle()
		for (i in 0 until num) on(i, slots[i])
	}

	private fun genItemsList(numPlayers: Int): ArrayList<ItemCreator> {
		val generates = ArrayList<ItemCreator>(256)

		for (i in 0 until numPlayers) ChaoticUtil.genReferenceItems().forEach { playerItem ->
			while (playerItem.remaining > 0) {
				val amount = Random.nextInt(playerItem.min, playerItem.max + 1).coerceAtMost(playerItem.remaining)
				generates.add(playerItem.creator().amount(amount))
				playerItem.remaining -= amount
			}
		}

		generates.shuffle()
		return generates
	}
}
