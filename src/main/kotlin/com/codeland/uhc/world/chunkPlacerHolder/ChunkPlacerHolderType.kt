package com.codeland.uhc.world.chunkPlacerHolder

import com.codeland.uhc.world.chunkPlacerHolder.type.*
import kotlin.random.Random

enum class ChunkPlacerHolderType(val chunkPlacerHolder: ChunkPlacerHolder) {
	CHRISTMAS(ChristmasWorld()),
	HALLOWEEN_WORLD(HalloweenWorld()),
	MUSHROOM_OXEYE_FIX(MushroomOxeyeFix()),
	NETHER_FIX(NetherFix()),
	NETHER_INDICATORS(NetherIndicators()),
	ORE_FIX(OreFix()),
	SUGAR_CANE_FIX(SugarCaneFix()),
	STRUCTURES(StructuresWorld());

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
