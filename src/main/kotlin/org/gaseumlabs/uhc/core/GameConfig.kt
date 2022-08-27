package org.gaseumlabs.uhc.core

import org.bukkit.World
import org.gaseumlabs.uhc.chc.CHCType
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.gui.gui.CreateGameGui
import org.gaseumlabs.uhc.util.*
import org.gaseumlabs.uhc.util.Set
import org.gaseumlabs.uhc.world.WorldManager

class GameConfig {
	private val group = PropertyGroup { GuiManager.update(CreateGameGui::class) }

	/* lock is set to true when game is running */
	var lock = false

	private fun <T> locked(set: T) = if (lock) DontSet() else Set(set)

	var naturalRegeneration by group.delegate(false)
	var killReward by group.delegate(KillReward.APPLE)
	var usingBot by group.delegate(UHC.bot != null, { set ->
		Set(if ( UHC.bot == null) false else set)
	}, { value ->
		if (!value) UHC.bot?.clearTeamVCs()
	})

	var defaultWorldEnvironment by group.delegate(World.Environment.NORMAL, ::locked)
	var chcType by group.delegate<CHCType?>(null, ::locked)

	/* border settings */
	var scale by group.delegate(1.0f, ::locked)
	var battlegroundRadius by group.delegate(72, ::locked)
	var graceTime by group.delegate(1200, ::locked)
	var shrinkTime by group.delegate(1200, ::locked)
	var battlegroundTime by group.delegate(1200, ::locked)
	var collapseTime by group.delegate(300, ::locked)

	/* functions */

	fun reset() = group.reset()

	/* getters */

	fun getWorlds(): Pair<World?, World?> {
		return if (defaultWorldEnvironment === World.Environment.NORMAL) {
			Pair(WorldManager.gameWorld, WorldManager.netherWorld)
		} else {
			Pair(WorldManager.netherWorld, WorldManager.gameWorld)
		}
	}
}
