package com.codeland.uhc.world.gen

import net.minecraft.server.v1_16_R3.*
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap
import kotlin.math.floor
import kotlin.random.Random

class WorldChunkManagerOverworldChunkBiomes(
	val seed: Long,
	var2: Boolean,
	var3: Boolean,
	private val var4: IRegistry<BiomeBase>
) : WorldChunkManagerOverworld(seed, var2, var3, var4) {
	val biomesHashmapField = BiomeRegistry::class.java.getDeclaredField("c")

	init { biomesHashmapField.isAccessible = true }

	val biomeList = (biomesHashmapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>)
		.map { (_, biome) -> biome } as ArrayList<ResourceKey<BiomeBase>>

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
        val chunkX = floor(x / 4.0).toInt()
	    val chunkZ = floor(z / 4.0).toInt()

	    val random = Random(chunkX.toLong().shl(32).or(chunkZ.toLong().and(0x0000FFFF)).xor(seed))

	    return var4.d(biomeList[random.nextInt(0, biomeList.size)])
    }
}
