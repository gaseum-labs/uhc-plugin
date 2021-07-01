package com.codeland.uhc.world.gen.layer.pvp

import com.codeland.uhc.world.gen.WorldChunkManagerOverworldPvp
import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1

class LayerPvp : AreaTransformer1 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int): Int {
		return context.a(WorldChunkManagerOverworldPvp.biomeNos.size)
	}
}
