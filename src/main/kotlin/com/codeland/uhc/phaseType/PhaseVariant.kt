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

enum class PhaseVariant(var type: PhaseType, var createPhase: () -> Phase, var prettyName: String, var representation: Material, var description: List<String>) {
	WAITING_DEFAULT(PhaseType.WAITING, ::WaitingDefault, "Default", Material.CLOCK, listOf(
		"Everyone waits for the UHC to start"
	)),

	GRACE_DEFAULT(PhaseType.GRACE, ::GraceDefault, "Default", Material.FEATHER, listOf(
		"You may regenerate health and are free from combat to get you started"
	)),

	SHRINK_DEFAULT(PhaseType.SHRINK, ::ShrinkDefault, "Default", Material.SPAWNER, listOf(
		"The border begins to force you to the center"
	)),

	FINAL_DEFAULT(PhaseType.FINAL, ::FinalDefault, "Default", Material.NETHERITE_SCRAP, listOf(
		"All players are now in the final small center radius"
	)),

	GLOWING_DEFAULT(PhaseType.GLOWING, ::GlowingDefault, "Default", Material.GLOWSTONE_DUST, listOf(
		"Glowing is applied to everyone",
		"Now you cannot hide"
	)),

	GLOWING_TOP_TWO(PhaseType.GLOWING, ::GlowingTopTwo, "Top two", Material.SPECTRAL_ARROW, listOf(
		"The two teams with the most health will always be glowing"
	)),

	ENDGAME_NONE(PhaseType.ENDGAME, ::EndgameNone, "None", Material.STONE_SWORD, listOf(
		"Fight to the death without intervention"
	)),

	ENDGAME_CLEAR_BLOCKS(PhaseType.ENDGAME, ::EndgameClearBlocks, "Clear blocks", Material.DIRT, listOf(
		"The ground above and below falls away",
		"Until your only battleground is on y = 60"
	)),

	ENDGAME_DEATHMATCH(PhaseType.ENDGAME, ::EndgameDeathmatch, "Deathmatch", Material.BONE, listOf(
		"Players are teleported to a platform in the sky",
		"There is nothing to do but fight"
	)),

	ENDGAME_POISON(PhaseType.ENDGAME, ::EndgamePoison, "Poision", Material.WITHER_SKELETON_SKULL, listOf(
		"Health will be continuously taken away until everyone dies"
	)),

	POSTGAME_DEFAULT(PhaseType.POSTGAME, ::PostgameDefault, "Default", Material.FILLED_MAP, listOf(
		"Hooray, the match is over!"
	));

	fun start(uhc: UHC, time: Int, onInject: (Phase) -> Unit): Phase {
		val ret = createPhase()

		ret.start(type, uhc, time, onInject)

		return ret
	}
}
