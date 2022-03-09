package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import org.bukkit.World

enum class ArenaType(val load: (data: String, world: World) -> Arena?) {
	PVP(PvpArena::load),
	PARKOUR(ParkourArena::load);
}
