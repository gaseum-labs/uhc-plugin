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

	const val BORDER_INCREMENT = 96
	const val OCEAN_BUFFER = 16

	fun createAreaGame2(seed: Long, borderRadius: Int): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerTemperature(seed, (borderRadius - OCEAN_BUFFER) / BORDER_INCREMENT).a(noise(0L))  /* 4X */

		baseLayer = GenLayerShiftZZoom(seed, 3).a(noise(1L), baseLayer)   /* 3X */
		baseLayer =     GenLayerShiftX(seed, 3).a(noise(1L), baseLayer)
		baseLayer =          GenLayerCombiner().a(noise(1L), baseLayer)   /* 2X */

		baseLayer = GenLayerBorder().a(noise(7070L), baseLayer)
		baseLayer = GenLayerSplit().a(noise(7071L), baseLayer)

		baseLayer = GenLayerShiftZZoom(seed, 2).a(noise(1L), baseLayer)   /* 2X */
		baseLayer =     GenLayerShiftX(seed, 2).a(noise(1L), baseLayer)
		baseLayer =          GenLayerCombiner().a(noise(1L), baseLayer)   /* 2X */

		/* -------------------------------------------------------------------- */

		var riverLayer = LayerNoise().a(noise(0L))                        /* 4X */

		riverLayer = GenLayerShiftZZoom(seed, 3).a(noise(1L), riverLayer) /* 3X */
		riverLayer =     GenLayerShiftX(seed, 3).a(noise(1L), riverLayer)
		riverLayer =          GenLayerCombiner().a(noise(1L), riverLayer) /* 2X */
		riverLayer = GenLayerShiftZZoom(seed, 2).a(noise(1L), riverLayer) /* 2X */
		riverLayer =     GenLayerShiftX(seed, 2).a(noise(1L), riverLayer)
		riverLayer =          GenLayerCombiner().a(noise(1L), riverLayer) /* 2X */
		riverLayer =          GenLayerCombiner().a(noise(1L), riverLayer) /* 2X (extra) */

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
