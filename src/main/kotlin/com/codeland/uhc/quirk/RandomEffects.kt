package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.logging.Logger

class RandomEffects(type: QuirkType) : Quirk(type) {
	override fun onEnable() {
		timer = time

		currentRunnable = getRunnable()
		currentRunnable?.runTaskTimer(GameRunner.plugin, 0, 20)

		if (!(GameRunner.uhc.isPhase(PhaseType.WAITING) || GameRunner.uhc.isPhase(PhaseType.POSTGAME)))
			giveEffects()
	}

	override fun onPhaseSwitch(phase: PhaseVariant) {
		GameRunner.log("phase switching")

		if (phase.type == PhaseType.GRACE) {
			GameRunner.log("is grace")
			giveEffects()
		}
	}

	override fun onDisable() {
		Bukkit.getOnlinePlayers().forEach { player ->
			for (activePotionEffect in player.activePotionEffects)
				player.removePotionEffect(activePotionEffect.type)
		}

		currentRunnable?.cancel()
		currentRunnable = null
	}

	var currentRunnable = null as BukkitRunnable?

	fun getRunnable(): BukkitRunnable {
		return object : BukkitRunnable() {
			override fun run() {
				if (GameRunner.uhc.isPhase(PhaseType.WAITING) || GameRunner.uhc.isPhase(PhaseType.POSTGAME)) return

				--timer
				if (timer == 0) {
					timer = time

					giveEffects()
				}
			}
		}
	}

	companion object {
		var time = 300
		var timer = 0

		var effects = arrayOf(
			PotionEffectType.SPEED,
			PotionEffectType.FAST_DIGGING,
			PotionEffectType.INCREASE_DAMAGE,
			PotionEffectType.JUMP,
			PotionEffectType.DAMAGE_RESISTANCE,
			PotionEffectType.FIRE_RESISTANCE,
			PotionEffectType.INVISIBILITY,
			PotionEffectType.NIGHT_VISION,
			PotionEffectType.HEALTH_BOOST,
			PotionEffectType.ABSORPTION,
			PotionEffectType.SATURATION,
			PotionEffectType.CONDUIT_POWER,
			PotionEffectType.GLOWING,
			PotionEffectType.HUNGER,
			PotionEffectType.CONFUSION
		)

		fun giveEffects() {
			Bukkit.getOnlinePlayers().forEach { player ->
				GameRunner.log("giving effect to player ${player.name}")

				if (player.gameMode != GameMode.SURVIVAL) return

				/* make sure to not give same effect twice */
				var effectIndex = GameRunner.randRange(0, effects.lastIndex)

				while (player.hasPotionEffect(effects[effectIndex])) {
					++effectIndex
					effectIndex %= effects.size
				}

				GameRunner.log("gave effect ${effects[effectIndex].name}")

				/* give effect with a slight time overlap */
				val effect = PotionEffect(effects[effectIndex], (time + 2) * 20, 1, false, true)
				player.addPotionEffect(effect)
			}
		}
	}
}