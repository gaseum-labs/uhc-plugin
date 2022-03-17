package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.core.stats.GameType
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.gui.gui.CreateGameGui
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.util.UHCProperty
import org.gaseumlabs.uhc.world.WorldGenOption
import org.gaseumlabs.uhc.world.WorldManager
import org.bukkit.World
import org.bukkit.block.Biome

class GameConfig {
	/* lock is set to true when game is running */
	var lock = false

	private fun <T> lockedProperty(set: T): T? {
		return if (lock) null else set
	}

	val gameType = UHCProperty(GameType.UHC)
	val naturalRegeneration = UHCProperty(false)
	val killReward = UHCProperty(KillReward.APPLE)
	val usingBot = UHCProperty(UHC.bot != null) { set ->
		val bot = UHC.bot

		if (bot == null) {
			false

		} else {
			if (!set) bot.clearTeamVCs()
			set
		}
	}

	val defaultWorldEnvironment = UHCProperty(World.Environment.NORMAL, ::lockedProperty)

	/* border settings */
	val scale = UHCProperty(1.0f, ::lockedProperty)
	val endgameRadius = UHCProperty(30, ::lockedProperty)
	val graceTime = UHCProperty(1200, ::lockedProperty)
	val shrinkTime = UHCProperty(3000, ::lockedProperty)
	val collapseTime = UHCProperty(300, ::lockedProperty)

	/* quirks settings */
	val quirksEnabled = ArrayList<UHCProperty<Boolean>>(QuirkType.values().size)

	init {
		QuirkType.values().forEach { quirkType ->
			quirksEnabled.add(
				UHCProperty(false) { set ->
					if (set) {
						quirkType.incompatibilities.forEach { otherType ->
							if (quirksEnabled[otherType.ordinal].get()) {
								quirksEnabled[otherType.ordinal].set(false)
							}
						}

						true

					} else {
						false
					}
				}
			)
		}
	}

	/* world gen */
	val centerBiome = UHCProperty<Biome?>(null)
	val worldGenEnabled = WorldGenOption.values().map { UHCProperty(it.defaultEnabled) }

	/* functions */

	fun reset() {
		naturalRegeneration.reset()
		killReward.reset()
		defaultWorldEnvironment.reset()
		usingBot.reset()
		scale.reset()
		endgameRadius.reset()
		graceTime.reset()
		shrinkTime.reset()
		collapseTime.reset()
		quirksEnabled.forEach { it.reset() }
		centerBiome.reset()
		worldGenEnabled.forEach { it.reset() }
	}

	/* getters */

	fun getWorlds(): Pair<World?, World?> {
		return if (defaultWorldEnvironment.get() === World.Environment.NORMAL) {
			Pair(WorldManager.gameWorld, WorldManager.netherWorld)
		} else {
			Pair(WorldManager.netherWorld, WorldManager.gameWorld)
		}
	}

	fun worldGenEnabled(type: WorldGenOption): Boolean {
		return worldGenEnabled[type.ordinal].get()
	}

	//fun phaseTime(phaseType: PhaseType): Int {
	//	return when (phaseType) {
	//		PhaseType.GRACE -> graceTime.get()
	//		PhaseType.SHRINK -> shrinkTime.get()
	//		PhaseType.ENDGAME -> collapseTime.get()
	//		PhaseType.POSTGAME -> 0
	//	}
	//}

	var gui = GuiManager.register(CreateGameGui(this))
}
