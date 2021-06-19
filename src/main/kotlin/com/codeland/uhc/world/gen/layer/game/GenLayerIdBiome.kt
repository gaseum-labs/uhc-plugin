package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.server.v1_16_R3.AreaTransformer4
import net.minecraft.server.v1_16_R3.WorldGenContext

class GenLayerIdBiome: AreaTransformer4 {
	companion object {
		fun spec(p: Int): Boolean {
			return p.ushr(31) == 1
		}

		fun temp(p: Int): Int {
			return p.ushr(29).and(3)
		}

		fun id(p: Int): Int {
			return p.and(0x0000ffff)
		}

		fun create(special: Boolean, temp: Int, biome: Int): Int {
			return (if (special) 1 else 0).shl(31).or(temp.shl(29)).or(biome)
		}
	}

	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		val biome = when (p5) {
			0 -> when (context.a(2)) {
				0 -> BiomeNo.SNOWY_TUNDRA
				else -> BiomeNo.SNOWY_TAIGA
			}
			1 -> when (context.a(8)) {
				0 -> BiomeNo.SWAMP
				1 -> BiomeNo.DARK_FOREST
				2 -> BiomeNo.BIRCH_FOREST
				3 -> BiomeNo.TAIGA
				4 -> BiomeNo.FOREST
				5 -> BiomeNo.FOREST
				6 -> BiomeNo.PLAINS
				else -> BiomeNo.PLAINS
			}
			else -> when (context.a(3)) {
				0 -> BiomeNo.BADLANDS
				1 -> BiomeNo.DESERT
				else -> BiomeNo.SAVANNA
			}
		}

		return create(context.a(2) == 1, p5, biome)
	}
}