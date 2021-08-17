package com.codeland.uhc.world.gen

import com.codeland.uhc.world.gen.layer.GenLayerExpandNether
import com.codeland.uhc.world.gen.layer.LayerNetherBase
import com.codeland.uhc.world.gen.layer.game2.*
import com.codeland.uhc.world.gen.layer.pvp.LayerPvp
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.WorldGenContextArea
import net.minecraft.world.level.newbiome.layer.GenLayerZoom

object CustomGenLayers {
	fun createAreaNether(seed: Long): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerNetherBase().a(noise(101L))

		baseLayer = GenLayerZoom.ar.a(noise(102L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(103L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(104L), baseLayer)

		baseLayer = GenLayerExpandNether().a(noise(105L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(106L), baseLayer)

		return baseLayer.make()
	}

	fun createAreaGame2(seed: Long, border: Int): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerUnique().a(noise(1000L))

		baseLayer = GenLayerZoom.a.a(noise(1001L), baseLayer)

		baseLayer = GenLayerHole().a(noise(8080L), baseLayer)
		baseLayer = GenLayerCohere().a(noise(8081L), baseLayer)
		baseLayer = GenLayerSeparate().a(noise(8082L), baseLayer)
		baseLayer = GenLayerRegion().a(noise(8083L), baseLayer)
		baseLayer = GenLayerOcean(border - 16, 4).a(noise(8084L), baseLayer)

		baseLayer = GenLayerZoom.a.a(noise(1002L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1003L), baseLayer)

		baseLayer = GenLayerBorder().a(noise(7070L), baseLayer)
		baseLayer = GenLayerSplit().a(noise(7071L), baseLayer)

		baseLayer = GenLayerZoom.a.a(noise(1004L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1005L), baseLayer)

		var riverLayer = LayerNoise().a(noise(1000L))
		riverLayer = GenLayerZoom.a.a(noise(1001L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1002L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1003L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1004L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1005L), riverLayer)
		riverLayer = GenLayerEdge(BiomeNo.RIVER).a(noise(9090L), riverLayer)

		baseLayer = GenLayerRiverApply().a(noise(9091L), baseLayer, riverLayer)

		return baseLayer.make() as Area
	}

	fun createAreaPvp(seed: Long): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerPvp().a(noise(1000L))
		baseLayer = GenLayerZoom.a.a(noise(1001L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1002L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1003L), baseLayer)
		baseLayer = GenLayerZoom.ar.a(noise(1004L), baseLayer)

		return baseLayer.make() as Area
	}
}
