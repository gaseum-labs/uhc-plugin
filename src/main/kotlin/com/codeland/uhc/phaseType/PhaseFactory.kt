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
import org.bukkit.Material

enum class PhaseFactory(type: PhaseType, target: Phase, prettyName: String, representation: Material) {
	WAITING_DEFAULT(PhaseType.WAITING, WaitingDefault(), "Default", Material.CLOCK),
	GRACE_DEFAULT(PhaseType.GRACE, GraceDefault(), "Default", Material.FEATHER),

	SHRINK_DEFAULT(PhaseType.SHRINK, ShrinkDefault(), "Default", Material.SPAWNER),

	FINAL_DEFAULT(PhaseType.FINAL, FinalDefault(), "Default", Material.NETHERITE_SCRAP),

	GLOWING_DEFAULT(PhaseType.GLOWING, GlowingDefault(), "Default", Material.GLOWSTONE_DUST),
	GLOWING_TOP_TWO(PhaseType.GLOWING, GlowingTopTwo(), "Top two", Material.SPECTRAL_ARROW),

	ENDGAME_CLEAR_BLOCKS(PhaseType.ENDGAME, EndgameClearBlocks(), "Clear blocks", Material.DIRT),
	ENDGAME_DEATHMATCH(PhaseType.ENDGAME, EndgameDeathmatch(), "Deathmatch", Material.BONE),
	ENDGAME_NONE(PhaseType.ENDGAME, EndgameNone(), "None", Material.STONE_SWORD),
	ENDGAME_POISON(PhaseType.ENDGAME, EndgamePoison(), "Poision", Material.WITHER_SKELETON_SKULL),

	POSTGAME_DEFAULT(PhaseType.POSTGAME, PostgameDefault(), "Default", Material.FILLED_MAP);

	var type = type
	var target = target
	var time = 0L
	var prettyName = prettyName
	var representation = representation

	init {
		target.phaseType = type

		Factories.list[type.ordinal].add(this)
	}

	fun start(uhc: UHC): Phase {
		target.start(uhc, time)

		return target
	}
}
