package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.util.Util
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import kotlin.random.Random

class WorldChunkManagerOverworldChunkBiomes(
	val seed: Long,
	private val var4: IRegistry<BiomeBase>,
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	val biomesHashmapField = BiomeRegistry::class.java.getDeclaredField("c")

	init {
		biomesHashmapField.isAccessible = true
	}

	val biomeList = (biomesHashmapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>)
		.map { (_, biome) -> biome } as ArrayList<ResourceKey<BiomeBase>>

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		val chunkX = Util.floorDiv(x, 4)
		val chunkZ = Util.floorDiv(z, 4)

		val random = Random(chunkX.toLong().shl(32).or(chunkZ.toLong().and(0x0000FFFF)).xor(seed))

		return var4.d(biomeList[random.nextInt(biomeList.size)])
	}
}
