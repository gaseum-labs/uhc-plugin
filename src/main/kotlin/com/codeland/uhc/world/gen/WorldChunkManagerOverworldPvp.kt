package com.codeland.uhc.world.gen

import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random

class WorldChunkManagerOverworldPvp(
	val seed: Long,
	var2: Boolean,
	var3: Boolean,
	private val var4: IRegistry<BiomeBase>,
	val biomes: Array<ResourceKey<BiomeBase>>,
) : WorldChunkManagerOverworld(seed, var2, var3, var4) {
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

	    return var4.d(when {
	    	inRange(sx, sz, PvpGameManager.LARGE_BORDER) -> biomes[Random(cx.toLong().shl(16).or(cz.toLong()).xor(seed)).nextInt(0, biomes.size)]
		    inRange(sx, sz, PvpGameManager.BEACH) -> Biomes.BEACH
		    else -> Biomes.OCEAN
	    })
    }
}
