package com.codeland.uhc.world

import com.codeland.uhc.world.chunkPlacer.impl.BricksPlacer
import com.codeland.uhc.world.chunkPlacer.impl.CobwebPlacer
import com.codeland.uhc.world.chunkPlacer.impl.LanternPlacer
import com.codeland.uhc.world.chunkPlacer.impl.PumpkinPlacer

object HalloweenWorld {
	val pumpkinPlacer = PumpkinPlacer(3, 2341342)
	val lanternPlacer = LanternPlacer(4, -932493)
	val cobwebPlacer = CobwebPlacer(5, 30823383)
	val bricksPlacer = BricksPlacer(6, -72940)
}
