package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.chc.CHCType
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.gui.gui.CreateGameGui
import org.gaseumlabs.uhc.util.DontSet
import org.gaseumlabs.uhc.util.PropertyGroup
import org.gaseumlabs.uhc.util.Set

class GameConfig {
	private val group = PropertyGroup { GuiManager.update(CreateGameGui::class) }

	/* lock is set to true when game is running */
	var lock = false

	private fun <T> locked(set: T) = if (lock) DontSet() else Set(set)

	var naturalRegeneration by group.delegate(false)

	var usingBot by group.delegate(UHC.bot != null, { set ->
		Set(if ( UHC.bot == null) false else set)
	}, { value ->
		if (!value) UHC.bot?.clearTeamVCs()
	})

	var chcType by group.delegate<CHCType?>(null, ::locked)

	/* functions */

	fun reset() = group.reset()
}
