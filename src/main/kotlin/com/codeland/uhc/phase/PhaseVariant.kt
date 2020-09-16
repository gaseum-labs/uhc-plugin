package com.codeland.uhc.phase

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.phases.endgame.*
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.phase.phases.postgame.PostgameDefault
import com.codeland.uhc.phase.phases.shrink.ShrinkDefault
import com.codeland.uhc.phase.phases.waiting.WaitingDefault
import org.bukkit.Material

enum class PhaseVariant(var type: PhaseType, var createPhase: () -> Phase, var prettyName: String, var representation: Material, var description: List<String>) {
	WAITING_DEFAULT(PhaseType.WAITING, ::WaitingDefault, "Default", Material.CLOCK, listOf(
		"Everyone waits for the UHC to start"
	)),

	GRACE_FORGIVING(PhaseType.GRACE, ::GraceDefault, "Forgiving", Material.OXEYE_DAISY, listOf(
		"You may regenerate health and are free from combat to get you started",
		"If you happen to die you get a second chance"
	)),

	GRACE_UNFORGIVING(PhaseType.GRACE, ::GraceDefault, "Unforgiving", Material.WITHER_ROSE, listOf(
		"You may regenerate health and are free from combat to get you started",
		"If you die all your items go away"
	)),

	GRACE_HARSH(PhaseType.GRACE, ::GraceDefault, "Harsh", Material.FEATHER, listOf(
		"You may regenerate health and are free from combat to get you started",
		"But make sure you don't die because you don't have a second chance"
	)),

	SHRINK_DEFAULT(PhaseType.SHRINK, ::ShrinkDefault, "Default", Material.SPAWNER, listOf(
		"The border begins to force you to the center"
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

	ENDGAME_GLOWING_ALL(PhaseType.ENDGAME, ::EndgameGlowingAll, "Glowing all", Material.GLOWSTONE_DUST, listOf(
		"Glowing is applied to everyone",
		"Now you cannot hide"
	)),

	ENDGAME_GLOWING_TOP_TWO(PhaseType.ENDGAME, ::EndgameGlowingTopTwo, "Glowing Top Wwo", Material.SPECTRAL_ARROW, listOf(
		"The two teams with the most health will always be glowing"
	)),

	POSTGAME_DEFAULT(PhaseType.POSTGAME, ::PostgameDefault, "Default", Material.FILLED_MAP, listOf(
		"Hooray, the match is over!"
	));

	fun start(uhc: UHC, time: Int, onInject: (Phase) -> Unit): Phase {
		val ret = createPhase()

		ret.start(type, this, uhc, time, onInject)

		return ret
	}
}
