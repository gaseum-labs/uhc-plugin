package org.gaseumlabs.uhc.world.gen.UHCArea.layer.game

import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCLayer
import kotlin.random.Random

class LayerPerPlayer(seed: Long) : UHCLayer(seed) {
	private val baseRegions = arrayOf(
		Region.FLAT,
		Region.FLAT,
		Region.FLAT,
		Region.FORESTED,
		Region.FORESTED,
		Region.FORESTED,
		Region.AQUATIC,
		Region.JUNGLEY,
		null
	)

	private val specialRegions = arrayOf(
		Region.SPRUCEY,
		Region.MOUNTAINOUS,
		Region.ARID,
		Region.ACACIA,
		Region.SNOWING,
	)

	private fun chunkIndex(x: Int, z: Int, random: Random): Int {
		/* bits 0..8 indicate 1 if this spot is filled */
		var filled = 0

		/* find all 9 spots if necessary */
		for (i in 0 until 9) {
			var spot = random.nextInt(9)
			while (filled.ushr(spot).and(1) == 1) spot = (spot + 1) % 9
			filled = filled.or(1.shl(spot))

			/* found the index of this subchunk */
			if (spot == Util.mod(x, 3) * 3 + Util.mod(z, 3)) return spot
		}

		/* impossible, the loop will always succeed */
		throw Exception("chunk could not find an index")
	}

	override fun sample(x: Int, z: Int): Int {
		val baseX = Util.floorDiv(x, 3)
		val baseZ = Util.floorDiv(z, 3)

		val random = Random(Util.coordPack(baseX, baseZ, seed))

		val specialRegion = specialRegions[random.nextInt(specialRegions.size)]

		return (baseRegions[chunkIndex(x, z, random)] ?: specialRegion).getBiome(random)
	}
}
