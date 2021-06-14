package com.codeland.uhc.world.gen

import com.codeland.uhc.world.gen.layer.*
import net.minecraft.server.v1_16_R3.*
import net.minecraft.server.v1_16_R3.GenLayerSpecial.*
import java.util.function.LongFunction

object CustomGenLayers {
	val aFunction = GenLayers::class.java.getDeclaredMethod("a", Long::class.java, AreaTransformer2::class.java, AreaFactory::class.java, Int::class.java, LongFunction::class.java)

	init {
		aFunction.isAccessible = true
	}

	private fun <T : Area, C : AreaContextTransformed<T>> createAreaFactory(var0: Boolean, var1: Int, var2: Int, var3: LongFunction<C>): AreaFactory<T> {
		var var4 = LayerIsland.INSTANCE.a(var3.apply(1L))
		var4 = GenLayerZoom.FUZZY.a(var3.apply(2000L), var4)
		var4 = GenLayerIsland.INSTANCE.a(var3.apply(1L), var4)
		var4 = GenLayerZoom.NORMAL.a(var3.apply(2001L), var4)
		var4 = GenLayerIsland.INSTANCE.a(var3.apply(2L), var4)
		var4 = GenLayerIsland.INSTANCE.a(var3.apply(50L), var4)
		var4 = GenLayerIsland.INSTANCE.a(var3.apply(70L), var4)
		var4 = GenLayerIcePlains.INSTANCE.a(var3.apply(2L), var4)

		var var5 = GenLayerOceanEdge.INSTANCE.a(var3.apply(2L))
		var5 = aFunction.invoke(null, 2001L, GenLayerZoom.NORMAL, var5, 6, var3) as AreaFactory<T>
		var4 = GenLayerTopSoil.INSTANCE.a(var3.apply(2L), var4)
		var4 = GenLayerIsland.INSTANCE.a(var3.apply(3L), var4)
		var4 = Special1.INSTANCE.a(var3.apply(2L), var4)
		var4 = Special2.INSTANCE.a(var3.apply(2L), var4)
		var4 = Special3.INSTANCE.a(var3.apply(3L), var4)
		var4 = GenLayerZoom.NORMAL.a(var3.apply(2002L), var4)
		var4 = GenLayerZoom.NORMAL.a(var3.apply(2003L), var4)
		var4 = GenLayerIsland.INSTANCE.a(var3.apply(4L), var4)
		var4 = GenLayerMushroomIsland.INSTANCE.a(var3.apply(5L), var4)
		var4 = GenLayerDeepOcean.INSTANCE.a(var3.apply(4L), var4)
		var4 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var4, 0, var3) as AreaFactory<T>

		var var6 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var4, 0, var3) as AreaFactory<T>
		var6 = GenLayerCleaner.INSTANCE.a(var3.apply(100L), var6)

		var var7 = GenLayerBiome(var0).a(var3.apply(200L), var4)
		var7 = GenLayerJungle.INSTANCE.a(var3.apply(1001L), var7)
		var7 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var7, 2, var3) as AreaFactory<T>
		var7 = GenLayerDesert.INSTANCE.a(var3.apply(1000L), var7)

		val var8 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var6, 2, var3) as AreaFactory<T>
		var7 = GenLayerRegionHills.INSTANCE.a(var3.apply(1000L), var7, var8)
		var6 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var6, 2, var3) as AreaFactory<T>
		var6 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var6, var2, var3) as AreaFactory<T>
		var6 = GenLayerRiver.INSTANCE.a(var3.apply(1L), var6)
		var6 = GenLayerSmooth.INSTANCE.a(var3.apply(1000L), var6)
		var7 = GenLayerPlains.INSTANCE.a(var3.apply(1001L), var7)

		for (var9 in 0 until var1) {
			var7 = GenLayerZoom.NORMAL.a(var3.apply((1000 + var9).toLong()), var7)
			if (var9 == 0) {
				var7 = GenLayerIsland.INSTANCE.a(var3.apply(3L), var7)
			}
			if (var9 == 1 || var1 == 1) {
				var7 = GenLayerMushroomShore.INSTANCE.a(var3.apply(1000L), var7)
			}
		}

		var7 = GenLayerSmooth.INSTANCE.a(var3.apply(1000L), var7)
		var7 = GenLayerRiverMix.INSTANCE.a(var3.apply(100L), var7, var6)
		var7 = GenLayerOcean.INSTANCE.a(var3.apply(100L), var7, var5)
		return var7
	}

	/* biome numbers can be found in BiomeRegistry */

	private fun <T : Area, C : AreaContextTransformed<T>> createAreaFactoryNoOceans(
		var0: Boolean,
		biomeSize: Int,
		var2: Int,
		seed: LongFunction<C>,
		allowJungles: Boolean
	): AreaFactory<T> {
		//var var4 = LayerIsland.INSTANCE.a(var3.apply(1L))
		//var4 = GenLayerZoom.FUZZY.a(var3.apply(2000L), var4)
		//var4 = GenLayerIsland.INSTANCE.a(var3.apply(1L), var4)
		//var4 = GenLayerZoom.NORMAL.a(var3.apply(2001L), var4)
		//var4 = GenLayerIsland.INSTANCE.a(var3.apply(2L), var4)
		//var4 = GenLayerIsland.INSTANCE.a(var3.apply(50L), var4)
		//var4 = GenLayerIsland.INSTANCE.a(var3.apply(70L), var4)
		//var4 = GenLayerIcePlains.INSTANCE.a(var3.apply(2L), var4)
		var var4 = LayerNoOcean().a(seed.apply(2L))

		var var5 = GenLayerOceanEdge.INSTANCE.a(seed.apply(2L))
		var5 = aFunction.invoke(null, 2001L, GenLayerZoom.NORMAL, var5, 6, seed) as AreaFactory<T>
		var4 = GenLayerTopSoil.INSTANCE.a(seed.apply(2L), var4)
		var4 = GenLayerIsland.INSTANCE.a(seed.apply(3L), var4)
		var4 = Special1.INSTANCE.a(seed.apply(2L), var4)
		var4 = Special2.INSTANCE.a(seed.apply(2L), var4)
		var4 = Special3.INSTANCE.a(seed.apply(3L), var4)
		//var4 = GenLayerZoom.NORMAL.a(seed.apply(2002L), var4)
		//var4 = GenLayerZoom.NORMAL.a(seed.apply(2003L), var4)
		var4 = GenLayerIsland.INSTANCE.a(seed.apply(4L), var4)
		//var4 = GenLayerMushroomIsland.INSTANCE.a(var3.apply(5L), var4)
		//var4 = GenLayerDeepOcean.INSTANCE.a(var3.apply(4L), var4) //not needed because no oceans
		var4 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var4, 0, seed) as AreaFactory<T>

		var var6 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var4, 0, seed) as AreaFactory<T>
		var6 = GenLayerCleaner.INSTANCE.a(seed.apply(100L), var6)

		var var7 = if (allowJungles) {
			GenLayerJungle.INSTANCE.a(seed.apply(1001L), GenLayerBiome(var0).a(seed.apply(200L), var4))
		} else {
			GenLayerBiomeNoJungle().a(seed.apply(200L), var4)
		}

		var7 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var7, 2, seed) as AreaFactory<T>
		var7 = GenLayerDesert.INSTANCE.a(seed.apply(1000L), var7)

		val var8 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var6, 1, seed) as AreaFactory<T>
		var7 = GenLayerRegionHills.INSTANCE.a(seed.apply(1000L), var7, var8)
		var6 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var6, 2, seed) as AreaFactory<T>
		var6 = aFunction.invoke(null, 1000L, GenLayerZoom.NORMAL, var6, var2, seed) as AreaFactory<T>
		var6 = GenLayerRiver.INSTANCE.a(seed.apply(1L), var6)
		var6 = GenLayerSmooth.INSTANCE.a(seed.apply(1000L), var6)
		var7 = GenLayerPlains.INSTANCE.a(seed.apply(1001L), var7)

		for (i in 0 until biomeSize) {
			var7 = GenLayerZoom.NORMAL.a(seed.apply((1000 + i).toLong()), var7)
			//if (i == 0) var7 = GenLayerIsland.INSTANCE.a(var3.apply(3L), var7)
			//if (i == 1 || biomeSize == 1) var7 = GenLayerMushroomShore.INSTANCE.a(var3.apply(1000L), var7)
		}

		var7 = GenLayerSmooth.INSTANCE.a(seed.apply(1000L), var7)
		var7 = GenLayerRiverMix.INSTANCE.a(seed.apply(100L), var7, var6)
		var7 = GenLayerOcean.INSTANCE.a(seed.apply(100L), var7, var5)
		return var7
	}

	private fun <T : Area, C : AreaContextTransformed<T>> createAreaFactoryNether(
		seed: LongFunction<C>,
	): AreaFactory<T> {
		var baseLayer = LayerNetherBase().a(seed.apply(1932L))

		baseLayer = GenLayerZoom.FUZZY.a(seed.apply(3246L), baseLayer)
		baseLayer = GenLayerZoom.NORMAL.a(seed.apply(128290L), baseLayer)
		baseLayer = GenLayerZoom.NORMAL.a(seed.apply(2623L), baseLayer)

		baseLayer = GenLayerExpandNether().a(seed.apply(73232L), baseLayer)
		baseLayer = GenLayerZoom.NORMAL.a(seed.apply(9L), baseLayer)

		return baseLayer
	}

	private fun <T : Area, C : AreaContextTransformed<T>> createAreaFactoryLobby(
		seed: LongFunction<C>,
	): AreaFactory<T> {
		var baseLayer = LayerOceanNoise().a(seed.apply(12L))

		baseLayer = GenLayerZoom.FUZZY.a(seed.apply(24L), baseLayer)
		baseLayer = GenLayerZoom.NORMAL.a(seed.apply(48L), baseLayer)

		baseLayer = GenLayerShatteredIsland().a(seed.apply(96L), baseLayer)

		baseLayer = GenLayerZoom.FUZZY.a(seed.apply(192L), baseLayer)

		baseLayer = GenLayerOceanRiser().a(seed.apply(3242L), baseLayer)

		baseLayer = GenLayerZoom.NORMAL.a(seed.apply(222L), baseLayer)
		baseLayer = GenLayerZoom.NORMAL.a(seed.apply(333L), baseLayer)

		return baseLayer
	}

	fun createGenLayer(var0: Long, var2: Boolean, biomeSize: Int, var4: Int, allowJungles: Boolean): GenLayer {
		val var6 = createAreaFactoryNoOceans(var2, biomeSize, var4, LongFunction { var2x: Long -> WorldGenContextArea(25, var0, var2x) }, allowJungles)
		return GenLayer(var6)
	}

	fun createGenLayerNether(seed: Long): GenLayer {
		val var6 = createAreaFactoryNether(LongFunction { var2x: Long -> WorldGenContextArea(25, seed, var2x) })
		return GenLayer(var6)
	}

	fun createGenLayerLobby(seed: Long): GenLayer {
		val var6 = createAreaFactoryLobby(LongFunction { var2x: Long -> WorldGenContextArea(25, seed, var2x) })
		return GenLayer(var6)
	}
}
