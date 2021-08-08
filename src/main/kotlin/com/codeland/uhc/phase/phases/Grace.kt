package com.codeland.uhc.phase.phases

import com.codeland.uhc.core.*
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import org.bukkit.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class Grace(game: Game, time: Int) : Phase(PhaseType.GRACE, time, game) {
	override fun customStart() {}

	override fun updateBarLength(remainingTicks: Int): Float {
		return barLengthRemaining(remainingTicks)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarTitle(world: World, remainingSeconds: Int): String {
		return "${ChatColor.RESET}Grace period ends in ${phaseType.chatColor}${ChatColor.BOLD}${Util.timeString(remainingSeconds)}"	}

	override fun endPhrase(): String {
		return "Grace Period Ending"
	}
}
