package com.codeland.uhc.world.chunkPlacer

import com.codeland.uhc.world.chunkPlacer.impl.*
import com.codeland.uhc.world.chunkPlacer.impl.christmas.SnowPlacer
import com.codeland.uhc.world.chunkPlacer.impl.halloween.*
import com.codeland.uhc.world.chunkPlacer.impl.nether.*
import com.codeland.uhc.world.chunkPlacer.impl.structure.TowerPlacer
import org.bukkit.Chunk
import org.bukkit.Material

enum class ChunkPlacerHolder(val chunkPlacer: AbstractChunkPlacer) {
	/* nether */
	WART(WartPlacer(3)),
	BLACKSTONE(BlackstonePlacer()),
	MAGMA(MagmaPlacer()),
	LAVA_STREAM(LavaStreamPlacer(2)),
	BASALT(BasaltPlacer(2)),
	DEBRIS(OrePlacer(5, 6, 30, 2, Material.ANCIENT_DEBRIS, Material.ANCIENT_DEBRIS)),

	/* stews */
	OXEYE(OxeyePlacer(10)),
	RED_MUSHROOM(CaveMushroomPlacer(16, Material.RED_MUSHROOM)),
	BROWN_MUSHROOM(CaveMushroomPlacer(16, Material.BROWN_MUSHROOM)),

	/* orefix */
	AMETHYST(AmethystPlacer()),
	GOLD(OrePlacer(3, 6, 32, 5, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE)),
	LAPIS(OrePlacer(4, 6, 32, 4, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE)),
	DIAMOND(OrePlacer(5, 6, 16, 3, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)),
	EMERALD(OrePlacer(6, 6, 32, 1, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE)),

	/** CHCS */

	/* structures */
	TOWER(TowerPlacer(24)),

	/* halloween */
	DEAD_BUSH(DeadBushPlacer(1)),
	PUMPKIN(PumpkinPlacer(3)),
	LANTERN(LanternPlacer(4)),
	COBWEB(CobwebPlacer(5)),
	BRICKS(BricksPlacer(6)),
	BANNER(BannerPlacer(13)),

	/* christmas */
	SNOW(SnowPlacer(1)),

	/* amplified */
	REVERSE_COAL(OrePlacer(1, 63, 240, 6, Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE)),
	REVERSE_IRON(OrePlacer(1, 63, 240, 6, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE)),
	REVERSE_REDSTONE(OrePlacer(2, 100, 240, 5, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE)),
	REVERSE_COPPER(OrePlacer(2, 100, 240, 4, Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE)),
	REVERSE_GOLD(OrePlacer(2, 150, 240, 4, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE)),
	REVERSE_LAPIS(OrePlacer(3, 150, 240, 3, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE)),
	REVERSE_DIAMOND(OrePlacer(4, 200, 240, 1, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)),

	/** DEPRECATED */

	/* sugar cane */
	DEEP_SUGAR_CANE(SugarCanePlacer(2, 58, 62)),
	LOW_SUGAR_CANE(SugarCanePlacer(6, 63, 63)),
	HIGH_SUGAR_CANE(SugarCanePlacer(4, 64, 82)),
	;

	fun onGenerate(chunk: Chunk, seed: Long) {
		chunkPlacer.onGenerate(chunk, this.ordinal.toLong(), seed)
	}

	operator fun component1(): AbstractChunkPlacer {
		return chunkPlacer
	}
}
