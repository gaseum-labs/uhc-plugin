package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.util.Util
import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1
import kotlin.random.Random

class LayerPerPlayer(val seed: Long) : AreaTransformer1 {
	fun dot(x: Int, z: Int) = if (
		Util.mod(x, 2) == 0 &&
		Util.mod(z, 2) == 0 &&
		Util.mod(z * 8 + x, 5) == 0
	) {
		Util.coordPack(x, z, seed)
	} else {
		null
	}

	fun chunkIndex(chunkX: Int, chunkZ: Int, seed: Long): Int {
		val baseX = Util.floorDiv(chunkX, 3)
		val baseZ = Util.floorDiv(chunkX, 3)

		val random = Random(Util.coordPack(baseX, baseZ, seed))

		/* bits 0..8 indicate 1 if this spot is filled */
		var filled = 0

		/* find all 9 spots if necessary */
		for (i in 0 until 9) {
			var spot = random.nextInt(9)
			while (filled.ushr(spot).and(1) == 1) spot = (spot + 1) % 9
			filled = filled.or(1.shl(spot))

			/* found the index of this subchunk */
			if (spot == Util.mod(chunkX, 3) * 3 + Util.mod(chunkZ, 3)) return spot
		}

		/* impossible, the loop will always succeed */
		return 0
	}

	override fun a(context: WorldGenContext, x: Int, z: Int): Int {
		return 0
	}
}
