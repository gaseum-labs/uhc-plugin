package com.codeland.uhc.world.chunkPlacerHolder

import com.codeland.uhc.world.chunkPlacerHolder.type.*
import kotlin.random.Random

enum class ChunkPlacerHolderType(val chunkPlacerHolder: ChunkPlacerHolder) {
	CHRISTMAS(ChristmasWorld()),
	DUNGEON_FIX(DungeonFix()),
	HALLOWEEN_WORLD(HalloweenWorld()),
	MELON_FIX(MelonFix()),
	MUSHROOM_OXEYE_FIX(MushroomOxeyeFix()),
	NETHER_FIX(NetherFix()),
	NETHER_INDICATORS(NetherIndicators()),
	ORE_FIX(OreFix()),
	SUGAR_CANE_FIX(SugarCaneFix());

	companion object {
		fun resetAll(seed: Long) {
			val random = Random(seed)

			var value = random.nextInt(0, 1000)

			values().forEach {
				it.chunkPlacerHolder.list().forEach { chunkPlacer ->
					chunkPlacer.reset(value)
					value += random.nextInt(1, 1000)
				}
			}
		}
	}
}
