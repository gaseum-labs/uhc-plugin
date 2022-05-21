package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.lobbyPvp.arena.ParkourArena
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.bukkit.World

enum class ArenaType(val load: (data: String, world: World) -> Arena?) {
	PVP(PvpArena::load),
	PARKOUR(ParkourArena::load);
}
