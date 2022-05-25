package org.gaseumlabs.uhc.lobbyPvp

import org.bukkit.World
import org.gaseumlabs.uhc.lobbyPvp.arena.*

enum class ArenaType(val load: (data: String, world: World) -> Arena?) {
	PVP(PvpArena::load),
	PARKOUR(ParkourArena::load),
	GAP_SLAP(GapSlapArena::load),
}
