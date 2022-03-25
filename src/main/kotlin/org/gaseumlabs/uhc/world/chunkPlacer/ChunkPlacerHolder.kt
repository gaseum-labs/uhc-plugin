package org.gaseumlabs.uhc.world.chunkPlacer

import org.gaseumlabs.uhc.world.chunkPlacer.impl.*
import org.gaseumlabs.uhc.world.chunkPlacer.impl.christmas.SnowPlacer
import org.gaseumlabs.uhc.world.chunkPlacer.impl.halloween.*
import org.gaseumlabs.uhc.world.chunkPlacer.impl.nether.*
import org.gaseumlabs.uhc.world.chunkPlacer.impl.structure.TowerPlacer
import org.bukkit.Chunk
import org.bukkit.Material

enum class ChunkPlacerHolder(val chunkPlacer: ChunkPlacer) {
	/* nether */
	WART(WartPlacer()),
	BLACKSTONE(BlackstonePlacer()),
	MAGMA(MagmaPlacer()),
	LAVA_STREAM(LavaStreamPlacer()),
	BASALT(BasaltPlacer()),
	DEBRIS(OrePlacer(5, 5, 6, 30, 2, Material.ANCIENT_DEBRIS, Material.ANCIENT_DEBRIS)),

	/* orefix */
	AMETHYST(AmethystPlacer()),

	/** CHCS */

	/* structures */
	TOWER(TowerPlacer()),

	/* halloween */
	DEAD_BUSH(DeadBushPlacer()),
	PUMPKIN(PumpkinPlacer()),
	LANTERN(LanternPlacer()),
	COBWEB(CobwebPlacer()),
	BRICKS(BricksPlacer()),
	BANNER(BannerPlacer()),

	/* christmas */
	SNOW(SnowPlacer()),

	/* amplified */
	REVERSE_COAL(OrePlacer(1, 1, 63, 240, 6, Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE)),
	REVERSE_IRON(OrePlacer(1, 1, 63, 240, 6, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE)),
	REVERSE_REDSTONE(OrePlacer(2, 2, 100, 240, 5, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE)),
	REVERSE_COPPER(OrePlacer(2, 2, 100, 240, 4, Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE)),
	REVERSE_GOLD(OrePlacer(2, 2, 150, 240, 4, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE)),
	REVERSE_LAPIS(OrePlacer(3, 3, 150, 240, 3, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE)),
	REVERSE_DIAMOND(OrePlacer(4, 4, 200, 240, 1, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)),

	;

	fun addToList(chunk: Chunk, list: ArrayList<ChunkPlacer>) {
		if (chunkPlacer.shouldGenerate(chunk.x, chunk.z, ordinal.toLong(), chunk.world.seed)) {
			list.add(chunkPlacer)
		}
	}

	operator fun component1(): ChunkPlacer {
		return chunkPlacer
	}
}
