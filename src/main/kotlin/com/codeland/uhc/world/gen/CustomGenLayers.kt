package com.codeland.uhc.world.gen

import com.codeland.uhc.world.gen.layer.*
import com.codeland.uhc.world.gen.layer.game.*
import com.codeland.uhc.world.gen.layer.game.GenLayerSpecial
import com.codeland.uhc.world.gen.layer.game2.*
import com.codeland.uhc.world.gen.layer.lobby.GenLayerOceanDeepener
import com.codeland.uhc.world.gen.layer.lobby.GenLayerOceanRiser
import com.codeland.uhc.world.gen.layer.lobby.GenLayerShatteredIsland
import com.codeland.uhc.world.gen.layer.lobby.LayerOceanNoise
import com.codeland.uhc.world.gen.layer.pvp.LayerPvp
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.area.AreaFactory
import net.minecraft.world.level.newbiome.area.AreaLazy
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.context.WorldGenContextArea
import net.minecraft.world.level.newbiome.layer.GenLayer
import net.minecraft.world.level.newbiome.layer.GenLayerSmooth
import net.minecraft.world.level.newbiome.layer.GenLayerZoom
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
import java.util.function.LongFunction

object CustomGenLayers {
	private fun <T : Area, C : AreaContextTransformed<T>> createAreaFactoryNether(
		seed: LongFunction<C>,
	): AreaFactory<T> {
		var baseLayer = LayerNetherBase().a(seed.apply(1932L))

		baseLayer = GenLayerZoom.ar.a(seed.apply(3246L), baseLayer)
		baseLayer = GenLayerZoom.a.a(seed.apply(128290L), baseLayer)
		baseLayer = GenLayerZoom.a.a(seed.apply(2623L), baseLayer)

		baseLayer = GenLayerExpandNether().a(seed.apply(73232L), baseLayer)
		baseLayer = GenLayerZoom.a.a(seed.apply(9L), baseLayer)

		return baseLayer
	}

	private fun <T : Area, C : AreaContextTransformed<T>> createAreaFactoryLobby(
		seed: LongFunction<C>,
	): AreaFactory<T> {
		var baseLayer = LayerOceanNoise().a(seed.apply(12L))
		baseLayer = GenLayerOceanDeepener().a(seed.apply(32401L), baseLayer)

		baseLayer = GenLayerZoom.ar.a(seed.apply(24L), baseLayer)
		baseLayer = GenLayerZoom.a.a(seed.apply(48L), baseLayer)

		baseLayer = GenLayerShatteredIsland().a(seed.apply(96L), baseLayer)

		baseLayer = GenLayerZoom.ar.a(seed.apply(192L), baseLayer)

		baseLayer = GenLayerOceanRiser().a(seed.apply(3242L), baseLayer)

		baseLayer = GenLayerZoom.a.a(seed.apply(222L), baseLayer)
		baseLayer = GenLayerZoom.a.a(seed.apply(333L), baseLayer)

		return baseLayer
	}

	fun createAreaGame(seed: Long): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerTemperature().a(noise(1000L))

		baseLayer = GenLayerZoom.a.a(noise(1001L), baseLayer)

		baseLayer = GenLayerMerge().a(noise(1L), baseLayer)
		baseLayer = GenLayerIdBiome().a(noise(2L), baseLayer)
		baseLayer = GenLayerMergePlains().a(noise(3L), baseLayer)

		baseLayer = GenLayerZoom.a.a(noise(1002L), baseLayer)

		baseLayer = GenLayerSpecial().a(noise(4L), baseLayer)

		baseLayer = GenLayerZoom.a.a(noise(1003L), baseLayer)

		var hillLayer = LayerNoise().a(noise(5L))
		hillLayer = GenLayerZoom.a.a(noise(6L), hillLayer)
		hillLayer = GenLayerZoom.a.a(noise(7L), hillLayer)
		hillLayer = GenLayerEdge(1).a(noise(8L), hillLayer)
		hillLayer = GenLayerZoom.a.a(noise(9L), hillLayer)

		baseLayer = GenLayerHillApply().a(noise(10L), baseLayer, hillLayer)
		baseLayer = GenLayerBiomeEdge().a(noise(11L), baseLayer)

		baseLayer = GenLayerZoom.a.a(noise(1004L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1005L), baseLayer)

		var riverLayer = LayerNoise().a(noise(1000L))
		riverLayer = GenLayerZoom.a.a(noise(1001L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1002L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1003L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1004L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1005L), riverLayer)
		riverLayer = GenLayerEdge(BiomeNo.RIVER).a(noise(122L), riverLayer)

		baseLayer = GenLayerRiverApply().a(noise(2220L), baseLayer, riverLayer)

		return baseLayer.make() as Area
	}

	fun createAreaGame2(seed: Long, border: Int): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerUnique().a(noise(1000L))

		baseLayer = GenLayerZoom.a.a(noise(1001L), baseLayer)

		baseLayer = GenLayerHole().a(noise(1002L), baseLayer)
		baseLayer = GenLayerCohere().a(noise(1003L), baseLayer)
		baseLayer = GenLayerSeparate().a(noise(1004L), baseLayer)
		baseLayer = GenLayerRegion().a(noise(1005), baseLayer)
		baseLayer = GenLayerOcean(border, 4).a(noise(234L), baseLayer)

		baseLayer = GenLayerZoom.a.a(noise(1006L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1007L), baseLayer)

		baseLayer = GenLayerBorder().a(noise(289L), baseLayer)
		baseLayer = GenLayerSplit().a(noise(1008L), baseLayer)

		baseLayer = GenLayerZoom.a.a(noise(1009L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1010L), baseLayer)

		var riverLayer = LayerNoise().a(noise(1000L))
		riverLayer = GenLayerZoom.a.a(noise(1001L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1006L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1007L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1009L), riverLayer)
		riverLayer = GenLayerZoom.a.a(noise(1010L), riverLayer)
		riverLayer = GenLayerEdge(BiomeNo.RIVER).a(noise(122L), riverLayer)

		baseLayer = GenLayerRiverApply().a(noise(2220L), baseLayer, riverLayer)

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

	fun createGenLayerNether(seed: Long): GenLayer {
		return GenLayer(createAreaFactoryNether { WorldGenContextArea(25, seed, it) })
	}

	fun createGenLayerLobby(seed: Long): GenLayer {
		return GenLayer(createAreaFactoryLobby { WorldGenContextArea(25, seed, it) })
	}
}
