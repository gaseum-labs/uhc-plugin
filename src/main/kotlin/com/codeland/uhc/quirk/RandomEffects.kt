package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class RandomEffects(type: QuirkType) : Quirk(type) {
	override fun onEnable() {
		timer = time

		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameRunner.plugin, {
			if (!GameRunner.uhc.isGameGoing()) return@scheduleSyncRepeatingTask

			if (--timer == 0) {
				timer = time
				giveEffects()
			}
		}, 0, 20)

		if (GameRunner.uhc.isGameGoing())
			giveEffects()
	}

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) {
			Bukkit.getOnlinePlayers().forEach { player ->
				resetEffects(player)
			}

			giveEffects()
		}
	}

	override fun onDisable() {
		Bukkit.getOnlinePlayers().forEach { player ->
			for (activePotionEffect in player.activePotionEffects)
				player.removePotionEffect(activePotionEffect.type)
		}

		Bukkit.getScheduler().cancelTask(taskID)
	}

	var taskID = 0

	companion object {
		var time = 180
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
			PotionEffectType.ABSORPTION,
			PotionEffectType.SATURATION,
			PotionEffectType.CONDUIT_POWER,
			PotionEffectType.SLOW_FALLING
		)

		fun giveEffects() {
			Bukkit.getOnlinePlayers().forEach { player ->
				if (player.gameMode != GameMode.SURVIVAL) return

				val effectType = getNextEffect(player)

				/* give effect with a slight time overlap */
				val effect = PotionEffect(effectType, time * 20, 1, false, true)
				player.addPotionEffect(effect)
			}
		}

		/* meta tags for potioned players */

		private const val LIST_TAG = "UHC_Q_POTION_LIST"
		private const val INDEX_TAG = "UHC_Q_POTION_INDEX"

		private fun createEffectsList(): Array<PotionEffectType> {
			val ret = effects.copyOf()

			/* shuffle array */
			for (index in ret.indices) {
				val swapIndex = (Math.random() * effects.size).toInt()

				val temp = ret[index]
				ret[index] = ret[swapIndex]
				ret[swapIndex] = temp
			}

			return ret
		}

		private fun increaseMetaIndex(player: Player, effectList: Array<PotionEffectType>): PotionEffectType {
			val indexMeta = player.getMetadata(INDEX_TAG)

			return if (indexMeta.size === 0) {
				player.setMetadata(INDEX_TAG, FixedMetadataValue(GameRunner.plugin, 1))

				effectList[0]

			} else {
				val index = indexMeta[0].asInt()

				if (index == effectList.size) {
					val newEffectList = createEffectsList()
					player.setMetadata(LIST_TAG, FixedMetadataValue(GameRunner.plugin, newEffectList))
					player.setMetadata(INDEX_TAG, FixedMetadataValue(GameRunner.plugin, 1))

					newEffectList[0]

				} else {
					player.setMetadata(INDEX_TAG, FixedMetadataValue(GameRunner.plugin, index + 1))

					effectList[index]
				}
			}
		}

		fun getNextEffect(player: Player): PotionEffectType {
			val listMeta = player.getMetadata(LIST_TAG)

			/* first time getting effect, need to create meta */
			if (listMeta.size === 0) {
				val effectList = createEffectsList()

				player.setMetadata(LIST_TAG, FixedMetadataValue(GameRunner.plugin, effectList))
				player.setMetadata(INDEX_TAG, FixedMetadataValue(GameRunner.plugin, 1))

				return effectList[0]
			}

			val effectList = listMeta[0].value() as Array<PotionEffectType>
			val indexMeta = player.getMetadata(INDEX_TAG)

			return increaseMetaIndex(player, effectList)
		}

		fun resetEffects(player: Player) {
			player.setMetadata(LIST_TAG, FixedMetadataValue(GameRunner.plugin, createEffectsList()))
			player.setMetadata(INDEX_TAG, FixedMetadataValue(GameRunner.plugin, 0))
		}
	}
}