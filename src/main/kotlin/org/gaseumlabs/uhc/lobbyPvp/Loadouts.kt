package org.gaseumlabs.uhc.lobbyPvp

import java.util.*

class Loadouts(
	val loadouts: HashMap<UUID, Array<Loadout>>,
) {
	companion object {
		const val NUM_SLOTS = 3
		val MAX_COST = 32

		/* it's fine to reorder these */
		/* determines the order they display by default in the gui */
		val loadoutItems = arrayOf(
			LoadoutItems.IRON_HELMET,
			LoadoutItems.IRON_CHESTPLATE,
			LoadoutItems.IRON_LEGGINGS,
			LoadoutItems.IRON_BOOTS,
			LoadoutItems.DIAMOND_HELMET,
			LoadoutItems.DIAMOND_CHESTPLATE,
			LoadoutItems.DIAMOND_LEGGINGS,
			LoadoutItems.DIAMOND_BOOTS,
			LoadoutItems.IRON_SWORD,
			LoadoutItems.DIAMOND_SWORD,
			LoadoutItems.IRON_AXE,
			LoadoutItems.DIAMOND_AXE,
			LoadoutItems.BOW,
			LoadoutItems.CROSSBOW,
			LoadoutItems.CROSSBOW_2,
			LoadoutItems.SHIELD,
			LoadoutItems.PICKAXE,
			LoadoutItems.ARROWS,
			LoadoutItems.SPECTRAL_ARROWS,
			LoadoutItems.WATER_BUCKET,
			LoadoutItems.LAVA_BUCKET,
			LoadoutItems.BLOCKS,
			LoadoutItems.BLOCKS_2,
			LoadoutItems.ENDER_PEARLS,
			LoadoutItems.GOLDEN_APPLES,
			LoadoutItems.SPEED_POTION,
			LoadoutItems.SPEED_POTION_2,
			LoadoutItems.HEALTH_POTION,
			LoadoutItems.HEALTH_POTION_2,
			LoadoutItems.DAMAGE_POTION,
			LoadoutItems.DAMAGE_POTION_2,
		)

		fun defaultSlots() = Array(NUM_SLOTS) { Loadout.genDefault() }
	}

	fun getPlayersLoadouts(uuid: UUID): Array<Loadout> {
		return loadouts.getOrPut(uuid) { defaultSlots() }
	}
}
