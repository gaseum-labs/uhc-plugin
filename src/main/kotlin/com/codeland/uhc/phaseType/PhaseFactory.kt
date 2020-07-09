package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.endgame.EndgameClearBlocks
import com.codeland.uhc.phases.endgame.EndgameDeathmatch
import com.codeland.uhc.phases.endgame.EndgameNone
import com.codeland.uhc.phases.endgame.EndgamePoison
import com.codeland.uhc.phases.final.FinalDefault
import com.codeland.uhc.phases.glowing.GlowingDefault
import com.codeland.uhc.phases.glowing.GlowingTopTwo
import com.codeland.uhc.phases.grace.GraceDefault
import com.codeland.uhc.phases.postgame.PostgameDefault
import com.codeland.uhc.phases.shrink.ShrinkDefault
import com.codeland.uhc.phases.waiting.WaitingDefault

enum class PhaseFactory(type: PhaseType, target: Phase) {
	WAITING_DEFAULT(PhaseType.WAITING, WaitingDefault()),
	GRACE_DEFAULT(PhaseType.GRACE, GraceDefault()),

	SHRINK_DEFAULT(PhaseType.SHRINK, ShrinkDefault()),

	FINAL_DEFAULT(PhaseType.FINAL, FinalDefault()),

	GLOWING_DEFAULT(PhaseType.GLOWING, GlowingDefault()),
	GLOWING_TOP_TWO(PhaseType.GLOWING, GlowingTopTwo()),

	ENDGAME_CLEAR_BLOCKS(PhaseType.ENDGAME, EndgameClearBlocks()),
	ENDGAME_DEATHMATCH(PhaseType.ENDGAME, EndgameDeathmatch()),
	ENDGAME_NONE(PhaseType.ENDGAME, EndgameNone()),
	ENDGAME_POISON(PhaseType.ENDGAME, EndgamePoison()),

	POSTGAME_DEFAULT(PhaseType.POSTGAME, PostgameDefault())
	;

	var type = type
	var target = target
	var time = 0L

	init {
		target.phaseType = type
	}

	fun start(uhc: UHC): Phase {
		target.start(uhc, time)

		return target
	}
}
