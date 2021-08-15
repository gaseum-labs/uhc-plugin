package com.codeland.uhc.quirk.quirks.carePackages

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.core.phase.phases.Endgame
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Action
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.block.Block
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

class ChaoticCarePackages(type: QuirkType, game: Game) : Quirk(type, game) {
	val random = Random(game.world.seed)
	private val itemsList = genItemsList(random, CarePackageUtil.genReferenceItems(random))

	val PER_CHEST = 13
	val NUM_DROPS = ceil(itemsList.size / 13.0).toInt()
	/**
	 * time that drops are spread out over
	 * grace + shrink - 10 minutes
	 * subtract any time that has already happened if quirk started late
	 */
	val TIME = (game.config.graceTime.get() + game.config.shrinkTime.get() - 600) * 20 - (UHC.timer)
	val TIME_PER_DROP = TIME / NUM_DROPS

	init {
		if (game.phase.phaseType.ordinal < PhaseType.ENDGAME.ordinal) startDropping()
	}

	override fun customDestroy() {
		stopDropping()
	}

	override fun onPhaseSwitch(phase: Phase) {
		if (phase is Endgame) stopDropping()
	}

	override fun onStartPlayer(uuid: UUID) {
		val playerData = PlayerData.getPlayerData(uuid)
		playerData.setQuirkData(type, genPlayerIndices(itemsList))
	}

	override fun defaultData(): Any {
		return emptyArray<Int>()
	}

	private fun startDropping() {
		timer = 0
		dropNum = 0
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, ::tick, 0, 1)
	}

	private fun stopDropping() {
		Bukkit.getScheduler().cancelTask(taskId)
	}

	var taskId = -1
	var timer = 0
	var dropNum = 0

	fun tick() {
		++timer

		if (timer >= TIME_PER_DROP) {
			if (dropNum >= NUM_DROPS) return startDropping()

			PlayerData.playerDataList.filter { (_, playerData) ->
				playerData.participating
			}.forEach { (uuid, playerData) ->
				val indexList = playerData.getQuirkData(this)

				if (indexList as? Array<Int> != null && indexList.isNotEmpty()) {
					val block = chaoticDropBlock(game.world)

					val inventory = CarePackageUtil.generateChest(game.world, block, ChatColor.values()[random.nextInt(ChatColor.MAGIC.ordinal)])

					chestSlots(PER_CHEST) { index, slot ->
						val itemIndex = index + dropNum * PER_CHEST
						if (itemIndex < indexList.size) inventory.setItem(slot, itemsList[indexList[itemIndex]].create())
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
	fun chaoticDropBlock(world: World): Block {
		val maxRadius = (((world.worldBorder.size - 1.0) / 2.0) - 10.0).roundToInt()
		val minRadius = game.config.endgameRadius.get()

		return when (random.nextInt(4)) {
			0 ->    CarePackageUtil.dropBlock(world, random.nextInt(-maxRadius,  minRadius), random.nextInt(-maxRadius, -minRadius))
			1 ->    CarePackageUtil.dropBlock(world, random.nextInt( minRadius,  maxRadius), random.nextInt(-maxRadius,  minRadius))
			2 ->    CarePackageUtil.dropBlock(world, random.nextInt(-minRadius,  maxRadius), random.nextInt( minRadius,  maxRadius))
			else -> CarePackageUtil.dropBlock(world, random.nextInt(-maxRadius, -minRadius), random.nextInt(-minRadius,  maxRadius))
		}
	}

	fun chestSlots(num: Int, on: (Int, Int) -> Unit) {
		val slots = Array(9 * 3) { it }
		slots.shuffle()
		for (i in 0 until num) on(i, slots[i])
	}

	fun genItemsList(random: Random, reference: Array<CarePackageUtil.ItemReference>): ArrayList<ItemCreator> {
		val generates = ArrayList<ItemCreator>(256)

		reference.forEach { playerItem ->
			while (playerItem.remaining > 0) {
				val amount = random.nextInt(playerItem.min, playerItem.max + 1).coerceAtMost(playerItem.remaining)
				generates.add(playerItem.create.amount(amount))
				playerItem.remaining -= amount
			}
		}

		return generates
	}

	fun genPlayerIndices(itemsList: ArrayList<ItemCreator>): Array<Int> {
		val ret = Array(itemsList.size) { it }
		ret.shuffle()
		return ret
	}
}
