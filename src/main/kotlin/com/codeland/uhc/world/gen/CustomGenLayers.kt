package com.codeland.uhc.world.gen

import com.codeland.uhc.world.gen.layer.*
import com.codeland.uhc.world.gen.layer.game.*
import com.codeland.uhc.world.gen.layer.game.GenLayerSpecial
import com.codeland.uhc.world.gen.layer.lobby.GenLayerOceanDeepener
import com.codeland.uhc.world.gen.layer.lobby.GenLayerOceanRiser
import com.codeland.uhc.world.gen.layer.lobby.GenLayerShatteredIsland
import com.codeland.uhc.world.gen.layer.lobby.LayerOceanNoise
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.area.AreaFactory
import net.minecraft.world.level.newbiome.area.AreaLazy
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.context.WorldGenContextArea
import net.minecraft.world.level.newbiome.layer.GenLayer
import net.minecraft.world.level.newbiome.layer.GenLayerZoom
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

	private fun <T : Area, C : AreaContextTransformed<T>> createAreaFactoryGame(
		seed: LongFunction<C>,
	): AreaFactory<T> {
		var baseLayer = LayerTemperature().a(seed.apply(12L))

		baseLayer = GenLayerZoom.a.a(seed.apply(1001L), baseLayer)

		baseLayer = GenLayerMerge().a(seed.apply(49L), baseLayer)
		baseLayer = GenLayerIdBiome().a(seed.apply(3333L), baseLayer)

		baseLayer = GenLayerZoom.a.a(seed.apply(1002L), baseLayer)

		baseLayer = GenLayerSpecial().a(seed.apply(192L), baseLayer)

		baseLayer = GenLayerZoom.a.a(seed.apply(1003L), baseLayer)

		var hillLayer = LayerNoise().a(seed.apply(88L))
		hillLayer = GenLayerZoom.a.a(seed.apply(90L), hillLayer)
		hillLayer = GenLayerZoom.a.a(seed.apply(90L), hillLayer)
		hillLayer = GenLayerEdge(1).a(seed.apply(142L), hillLayer)
		hillLayer = GenLayerZoom.a.a(seed.apply(2920L), hillLayer)

		baseLayer = GenLayerHillApply().a(seed.apply(23L), baseLayer, hillLayer)
		baseLayer = GenLayerBiomeEdge().a(seed.apply(292L), baseLayer)

		baseLayer = GenLayerZoom.a.a(seed.apply(1004L), baseLayer)
		baseLayer = GenLayerZoom.a.a(seed.apply(1005L), baseLayer)

		var riverLayer = LayerNoise().a(seed.apply(213L))
		riverLayer = GenLayerZoom.a.a(seed.apply(1001L), riverLayer)
		riverLayer = GenLayerZoom.a.a(seed.apply(1002L), riverLayer)
		riverLayer = GenLayerZoom.a.a(seed.apply(1003L), riverLayer)
		riverLayer = GenLayerZoom.a.a(seed.apply(1004L), riverLayer)
		riverLayer = GenLayerEdge(BiomeNo.RIVER).a(seed.apply(122L), riverLayer)
		riverLayer = GenLayerZoom.a.a(seed.apply(1005L), riverLayer)

		baseLayer = GenLayerRiverApply().a(seed.apply(2220L), baseLayer, riverLayer)

		return baseLayer
	}

	fun createGenLayerNether(seed: Long): GenLayer {
		val var6 = createAreaFactoryNether(LongFunction { var2x: Long -> WorldGenContextArea(25, seed, var2x) })
		return GenLayer(var6)
	}

	fun createGenLayerLobby(seed: Long): GenLayer {
		val var6 = createAreaFactoryLobby(LongFunction { var2x: Long -> WorldGenContextArea(25, seed, var2x) })
		return GenLayer(var6)
	}

	fun createGenLayerGame(seed: Long): AreaLazy {
		return createAreaFactoryGame(
			LongFunction { var2x: Long -> WorldGenContextArea(25, seed, var2x) }
		).make()
	}
}
