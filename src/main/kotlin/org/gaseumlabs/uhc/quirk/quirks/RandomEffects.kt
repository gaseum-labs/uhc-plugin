package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.util.Action
import org.bukkit.Bukkit
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class RandomEffects(type: QuirkType, game: Game) : Quirk(type, game) {
	var time = 180
	var timer = 0

	var taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(org.gaseumlabs.uhc.UHCPlugin.plugin, {
		if (--timer <= 0) {
			timer = time
			giveEffects()
		}
	}, 0, 20)

	override fun customDestroy() {
		Bukkit.getScheduler().cancelTask(taskID)
	}

	companion object {
		var effects = arrayOf(
			PotionEffectType.SPEED,
			PotionEffectType.FAST_DIGGING,
			PotionEffectType.INCREASE_DAMAGE,
			PotionEffectType.JUMP,
			PotionEffectType.DAMAGE_RESISTANCE,
			PotionEffectType.FIRE_RESISTANCE,
			PotionEffectType.INVISIBILITY,
			PotionEffectType.NIGHT_VISION,
			PotionEffectType.ABSORPTION,
			PotionEffectType.SATURATION,
			PotionEffectType.CONDUIT_POWER,
			PotionEffectType.SLOW_FALLING
		)

		var usingEffects = emptyArray<PotionEffectType>()
		var effectIndex = -1

		fun giveEffects() {
			if (effectIndex == -1) {
				generateUsingEffects()
				effectIndex = usingEffects.lastIndex
			}

			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if (playerData.participating) Action.playerAction(uuid) { player ->
					player.addPotionEffect(PotionEffect(usingEffects[effectIndex], 3600, 0, false, true, true))
				}
			}

			--effectIndex
		}

		private fun generateUsingEffects() {
			effectIndex = 0

			usingEffects = effects.copyOf()

			/* shuffle array */
			for (index in usingEffects.indices) {
				val swapIndex = (Math.random() * effects.size).toInt()

				val temp = usingEffects[index]
				usingEffects[index] = usingEffects[swapIndex]
				usingEffects[swapIndex] = temp
			}
		}
	}
}