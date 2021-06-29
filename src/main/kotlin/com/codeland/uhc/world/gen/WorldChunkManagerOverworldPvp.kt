package com.codeland.uhc.world.gen

import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.util.Util
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import kotlin.math.floor
import kotlin.random.Random

class WorldChunkManagerOverworldPvp(
	val seed: Long,
	private val biomeRegistry: IRegistry<BiomeBase>,
) : WorldChunkManagerOverworld(seed, false, false, biomeRegistry) {
	val biomes = arrayOf(
		BiomeNo.PLAINS,
		BiomeNo.DESERT,
		BiomeNo.MOUNTAINS,
		BiomeNo.FOREST,
		BiomeNo.TAIGA,
		BiomeNo.SWAMP,
		BiomeNo.SNOWY_TUNDRA,
		BiomeNo.MUSHROOM_FIELDS,
		BiomeNo.STONE_SHORE,
		BiomeNo.BIRCH_FOREST,
		BiomeNo.DARK_FOREST,
		BiomeNo.SAVANNA_PLATEAU,
		BiomeNo.BADLANDS,
		BiomeNo.WOODED_BADLANDS_PLATEAU,
		BiomeNo.FLOWER_FOREST,
		BiomeNo.ICE_SPIKES,
		BiomeNo.MODIFIED_GRAVELLY_MOUNTAINS,
		BiomeNo.ERODED_BADLANDS,
		BiomeNo.CRIMSON_FOREST
	).map { biomeRegistry.d(BiomeRegistry.a(it)) }

	val inBetween = biomeRegistry.d(BiomeRegistry.a(BiomeNo.BEACH))

	val stride = PvpGameManager.ARENA_STRIDE

	fun inRange(sx: Int, sz: Int, size: Int): Boolean {
		val border = (stride - size) / 2
		return sx > border && sx < stride - border && sz > border && sz < stride - border
	}

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    val cx = floor(x.toFloat() / (stride / 4)).toInt() + Short.MAX_VALUE / 2
	    val cz = floor(z.toFloat() / (stride / 4)).toInt() + Short.MAX_VALUE / 2

	    val sx = Util.mod(x, stride / 4) * 4
	    val sz = Util.mod(z, stride / 4) * 4

	    return when {
	    	inRange(sx, sz, PvpGameManager.BORDER) -> biomes[Random(cx.toLong().shl(32).or(cz.toLong()).xor(seed)).nextInt(0, biomes.size)]
		    else -> inBetween
	    }
    }
}
