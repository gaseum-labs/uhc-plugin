package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.endgame.EndgameClearBlocks
import com.codeland.uhc.phases.endgame.EndgameDeathmatch
import com.codeland.uhc.phases.endgame.EndgameNone
import com.codeland.uhc.phases.endgame.EndgamePoison

enum class EndgameType {
	CLEARBLOCK,
	DEATHMATCH,
	POISON,
	NONE;

	fun startPhase(uhc : UHC) : Phase {
		val ret =
		if (this == CLEARBLOCK) {
			EndgameClearBlocks()
		} else if (this == DEATHMATCH) {
			EndgameDeathmatch()
		} else if (this == POISON) {
			EndgamePoison()
		} else {
			EndgameNone()
		}
		ret.start(uhc, 0)
		return ret
	}
}