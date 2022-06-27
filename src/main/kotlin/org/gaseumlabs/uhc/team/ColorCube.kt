package org.gaseumlabs.uhc.team

import org.bukkit.DyeColor
import kotlin.random.Random

class ColorCube {
	companion object {
		const val NUM_COLORS = 16

		fun otherSlot(slot: Int): Int {
			return if (slot == 0) 1 else 0
		}
	}

	private val takenSlot0 = Array(NUM_COLORS) { false }
	private val takenSlot1 = Array(NUM_COLORS) { false }
	private val slots = arrayOf(takenSlot0, takenSlot1)

	private fun findEmptyIndex(random: Random, slotTaken: Array<Boolean>): Int? {
		val offset = random.nextInt(0, NUM_COLORS)

		for (i in 0 until NUM_COLORS) {
			val index = (i + offset) % NUM_COLORS
			if (!slotTaken[index]) return index
		}

		return null
	}

	/* team interface */

	/**
	 * picks two free colors for a new teams and modifies
	 * internal cube so that they are marked as taken
	 *
	 * @return the two team colors, or null if no team could be created
	 */
	fun pickTeam(): Pair<DyeColor, DyeColor>? {
		val random = Random((Math.random() * Int.MAX_VALUE).toInt())

		val index0 = findEmptyIndex(random, takenSlot0) ?: return null
		val index1 = findEmptyIndex(random, takenSlot1) ?: return null

		takenSlot0[index0] = true
		takenSlot1[index1] = true

		return DyeColor.values()[index0] to DyeColor.values()[index1]
	}

	fun removeTeam(colors: Array<DyeColor>) {
		takenSlot0[colors[0].ordinal] = false
		takenSlot1[colors[1].ordinal] = false
	}

	fun switchColor(colorFrom: DyeColor, colorTo: DyeColor, slot: Int) {
		slots[slot][colorFrom.ordinal] = false
		slots[slot][colorTo.ordinal] = true
	}

	fun taken(color: DyeColor, slot: Int): Boolean {
		return slots[slot][color.ordinal]
	}
}
