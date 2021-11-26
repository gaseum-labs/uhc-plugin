package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.util.Util
import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1
import kotlin.random.Random

class LayerTemperature(val seed: Long, val radius: Int) : AreaTransformer1 {
	companion object {
		val eightChooseFive = (0b00011111..0xff).filter { i ->
			i.and(1) +
			i.ushr(1).and(1) +
			i.ushr(2).and(1) +
			i.ushr(3).and(1) +
			i.ushr(4).and(1) +
			i.ushr(5).and(1) +
			i.ushr(6).and(1) +
			i.ushr(7) == 5
		}

		val temperateRegions = arrayOf(
			Region.PLAINS,
			Region.SWAMP,
			Region.DARK_FOREST,
			Region.TAIGA,
			Region.JUNGLE,
			Region.FOREST,
			Region.BIRCH_FOREST,
			Region.MOUNTAINS
		)

		val hotRegions = arrayOf(
			Region.SAVANNA,
			Region.DESERT,
			Region.PLAINS
		)

		val coldRegions = arrayOf(
			Region.SNOWY,
			Region.SNOWY_TAIGA
		)

		val badlandsRegions = arrayOf(
			Region.BADLANDS,
			Region.BADLANDS_PLATEAU
		)
	}

	fun dot(x: Int, z: Int) = if (
		Util.mod(x, 2) == 0 &&
		Util.mod(z, 2) == 0 &&
		Util.mod(z * 8 + x, 5) == 0
	) {
		Util.coordPack(x, z, seed)
	} else {
		null
	}

	fun temperature(x: Int, z: Int): Temperature {
		/*  2 X X X | X X
		 *  1 X X X | X X
		 *  0 ------+----
		 * -1 X X X | X X
		 * -2 X X X | X X
		 * -3 X X X | X X
		 *   -3-2-1 0 1 2
		 */
		//if (x !in -radius..radius - 1 || z !in -radius..radius - 1) return Temperature.OCEAN

		val offset = Random(seed).nextInt(100)

		for (i in 0..8) {
			val seed = dot(
				x + (i % 3) - 1 + (offset % 10),
				z + ((i / 3) % 3) - 1 + (offset / 10)
			)

			if (seed != null) {
				val random = Random(seed)
				val temperature = Temperature.randomSpecial(random)

				if (i == 4 || eightChooseFive[random.nextInt(eightChooseFive.size)].ushr(if (i > 4) i - 1 else i)
						.and(1) == 1
				) {
					return temperature
				}
			}
		}

		return Temperature.TEMPERATE
	}

	override fun a(context: WorldGenContext, x: Int, z: Int) = Region.pack(
		when (temperature(x, z)) {
			Temperature.TEMPERATE -> temperateRegions[context.a(temperateRegions.size)]
			Temperature.COLD -> coldRegions[context.a(coldRegions.size)]
			Temperature.HOT -> hotRegions[context.a(hotRegions.size)]
			Temperature.BADLANDS -> badlandsRegions[context.a(badlandsRegions.size)]
			Temperature.MEGA -> Region.GIANT_TAIGA
			Temperature.OCEAN -> Region.OCEAN
		},
		context.a(4) == 0
	)
}
