package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.server.v1_16_R3.AreaTransformer4
import net.minecraft.server.v1_16_R3.WorldGenContext
import javax.management.BadBinaryOpValueExpException

class GenLayerVis : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return when (GenLayerIdBiome.temp(p5)) {
			0 -> BiomeNo.SNOWY_TUNDRA
			1 -> BiomeNo.TAIGA
			2 -> BiomeNo.PLAINS
			else -> BiomeNo.BADLANDS
		}
	}
}
