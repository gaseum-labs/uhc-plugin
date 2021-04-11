package com.codeland.uhc.world.gen

import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random

class WorldChunkManagerOverworldPvp(
	var0: Long,
	var2: Boolean,
	var3: Boolean,
	private val var4: IRegistry<BiomeBase>,
	val biomes: Array<ResourceKey<BiomeBase>>,
	size : Int
) : WorldChunkManagerOverworld(var0, var2, var3, var4) {
	val chunkSize = size / 4

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    val cx = floor(x.toFloat() / chunkSize).toInt() + Short.MAX_VALUE / 2
	    val cz = floor(z.toFloat() / chunkSize).toInt() + Short.MAX_VALUE / 2

	    val random = Random(cx.shl(16).or(cz))

	    val checker = Util.mod(cx, 2) == Util.mod(cz, 2)

	    return var4.d(biomes[
		    if (checker)
		    	random.nextInt(0, biomes.size / 2)
	        else
			    random.nextInt(biomes.size / 2, biomes.size)
	    ])
    }
}
