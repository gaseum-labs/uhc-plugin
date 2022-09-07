package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.util.*
import java.util.UUID

data class ArenaMarker(
	val coords: Coords,
	val start: BlockPos,
	val owner: UUID,
	val premiere: Boolean,
) {
	companion object {
		val key = KeyGen.genKey("arena_markers")
		val dataType = createSuperArrayDataType<ArenaMarker>()
	}
}
