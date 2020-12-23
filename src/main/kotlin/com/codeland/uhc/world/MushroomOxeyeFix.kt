package com.codeland.uhc.world

import com.codeland.uhc.world.chunkPlacer.impl.CaveMushroomPlacer
import com.codeland.uhc.world.chunkPlacer.impl.OxeyePlacer
import org.bukkit.Chunk
import org.bukkit.Material

object MushroomOxeyeFix {
	val oxeyePlacer = OxeyePlacer(12, -342922)
	val redMushroomPlacer = CaveMushroomPlacer(12, 92022, Material.RED_MUSHROOM)
	val brownMushroomPlacer = CaveMushroomPlacer(12, 5021, Material.BROWN_MUSHROOM)
}
