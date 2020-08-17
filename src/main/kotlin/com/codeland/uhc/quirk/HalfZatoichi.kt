package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable

class HalfZatoichi(type: QuirkType) : Quirk(type) {
	override fun onEnable() {
		if (GameRunner.uhc.currentPhase?.phaseType != PhaseType.WAITING) {
			giveAllZatoichi()
		}

		currentRunnable = getRunnable()
		currentRunnable?.runTaskTimer(GameRunner.plugin, 0, 5)
	}

	override fun onDisable() {
		removeAllZatoichi()

		currentRunnable?.cancel()
		currentRunnable = null
	}

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) {
			giveAllZatoichi()
		} else if (phase.type == PhaseType.WAITING) {
			removeAllZatoichi()
		}
	}

	var currentRunnable = null as BukkitRunnable?

	private fun getRunnable(): BukkitRunnable {
		return object : BukkitRunnable() {
			/* punishment loop for sheathing without killing */
			override fun run() {
				Bukkit.getServer().onlinePlayers.forEach { player ->
					val lastHolding = getZatoichiMeta(player, HOLDING_META_TAG)
					val currentlyHolding = isHalfZatoichi(player.inventory.itemInMainHand)

					if (lastHolding) {
						if (!currentlyHolding) {
							setZatoichiMeta(player, HOLDING_META_TAG, false)

							if (!getZatoichiMeta(player, BLOODY_META_TAG))
								player.damage(5.0)
							else
								setZatoichiMeta(player, BLOODY_META_TAG, false)
						}
					} else if (currentlyHolding) {
						setZatoichiMeta(player, HOLDING_META_TAG, true)
					}
				}

			}
		}
	}

	companion object {
		const val HOLDING_META_TAG = "uhc_zatoichi_holding"
		const val BLOODY_META_TAG = "uhc_zatoichi_bloody"

		val ZATOICHI_NAME = "${ChatColor.RESET}Half Zatoichi"
		val ZATOICHI_BLOODY_NAME = "${ChatColor.RESET}Half Zatoichi ${ChatColor.RED}${ChatColor.BOLD}(Bloody)"

		fun getZatoichiMeta(player: Player, tag: String): Boolean {
			val meta = player.getMetadata(tag)

			return if (meta.size == 0) {
				player.setMetadata(tag, FixedMetadataValue(GameRunner.plugin, false))
				false
			} else {
				meta[0].asBoolean()
			}
		}

		fun setZatoichiMeta(player: Player, tag: String, holding: Boolean) {
			player.setMetadata(tag, FixedMetadataValue(GameRunner.plugin, holding))
		}

		fun setBloody(player: Player) {
			val stack = player.inventory.itemInMainHand

			if (isHalfZatoichi(stack)) {
				val meta = player.inventory.itemInMainHand.itemMeta
				meta.setDisplayName(ZATOICHI_BLOODY_NAME)
			}

			setZatoichiMeta(player, BLOODY_META_TAG, true)
		}

		fun unsetBloody(player: Player) {
			findZatoichi(player) { zatoichi, index ->
				val meta = zatoichi.itemMeta
				meta.setDisplayName(ZATOICHI_NAME)
				zatoichi.itemMeta = meta
			}

			setZatoichiMeta(player, BLOODY_META_TAG, false)
		}

		fun onKill(killer: Player) {
			if (isHalfZatoichi(killer.inventory.itemInMainHand)) {
				setBloody(killer)

				val maxHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
				val addedTotal = maxHealth / 2.0

				if (killer.health == maxHealth) {
					killer.absorptionAmount += addedTotal

				} else {
					val addedAbsorbtion = killer.health + killer.absorptionAmount + addedTotal - maxHealth
					val addedHealth = addedTotal - addedAbsorbtion

					if (addedAbsorbtion > 0) killer.absorptionAmount = addedAbsorbtion
					if (addedHealth > 0) killer.health += addedHealth
				}
			}
		}

		fun createZatoichi(): ItemStack {
			val zatoichi = ItemStack(Material.IRON_SWORD)
			val meta = zatoichi.itemMeta

			meta.setDisplayName(ZATOICHI_NAME)
			meta.lore = listOf(
				"Soldiers and Demos",
				"Can duel with katanas",
				"For a one-hit kill"
			)
			meta.isUnbreakable = true

			zatoichi.itemMeta = meta
			return zatoichi
		}

		fun giveAllZatoichi() {
			Bukkit.getServer().onlinePlayers.forEach { player ->
				if (player.gameMode == GameMode.SURVIVAL)
					player.inventory.setItem(9, createZatoichi())
			}
		}

		fun findZatoichi(player: Player, onFound: (ItemStack, Int) -> Unit) {
			val array = player.inventory.contents

			for (i in 0..array.lastIndex) {
				if (isHalfZatoichi(array[i])) {
					onFound(array[i], i)
					break
				}
			}
		}

		fun removeAllZatoichi() {
			Bukkit.getServer().onlinePlayers.forEach { player ->
				findZatoichi(player) { zatoichi, index ->
					player.inventory.clear(index)
				}
			}
		}

		fun isHalfZatoichi(item: ItemStack?): Boolean {
			return (item?.type == Material.IRON_SWORD && item.itemMeta.hasLore() && item.itemMeta.isUnbreakable)
		}
	}
}