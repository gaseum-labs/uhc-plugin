package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class HalfZatoichi(type: QuirkType) : Quirk(type) {
	override fun onEnable() {
		/* give players half zatoichi */
		giveZatoichi()

		currentRunnable = getRunnable()
		currentRunnable?.runTaskTimer(GameRunner.plugin, 0, 2)
	}

	override fun onDisable() {
		removeZatoichi()

		currentRunnable?.cancel()
		currentRunnable = null
	}

	var currentRunnable = null as BukkitRunnable?

	fun getRunnable(): BukkitRunnable {
		return object : BukkitRunnable() {
			val players = ArrayList<Senshi>()
			override fun run() {
				for (player in Bukkit.getServer().onlinePlayers) {
					var visited = false
					for (senshi in players) {
						if (senshi.updatePlayer(player)) {
							visited = true
							break
						}
					}
					if (!visited) {
						players.add(Senshi(player.displayName))
					}
				}
			}
		}
	}

	private class Senshi(name: String) {
		private val name = name
		private var holdingZatoichi = false

		fun updatePlayer(player: Player): Boolean {
			if (name == player.displayName) {
				if (holdingZatoichi) {
					if (!isHalfZatoichi(player.inventory.itemInMainHand)) {
						holdingZatoichi = false

						for (i in (0..35)) {
							if (player.inventory.getItem(i)?.type == Material.IRON_SWORD) {
								if (player.inventory.getItem(i)?.itemMeta?.displayName == "Half Zatoichi (bloody)") {
									val meta = player.inventory.getItem(i)!!.itemMeta.clone()
									meta.setDisplayName("Half Zatoichi")
									player.inventory.getItem(i)!!.itemMeta = meta
									return true
								}
							}
						}

						player.damage(5.0)
					}
				} else {
					holdingZatoichi = isHalfZatoichi(player.inventory.itemInMainHand)
				}
				return true
			}
			return false
		}
	}

	companion object {
		fun giveZatoichi() {
			Bukkit.getServer().onlinePlayers.forEach { player ->
				if (player.gameMode == GameMode.SURVIVAL) {
					val zatoichi = ItemStack(Material.IRON_SWORD)

					val meta = zatoichi.itemMeta
					meta.setDisplayName("Half Zatoichi")
					meta.isUnbreakable = true
					zatoichi.itemMeta = meta

					player.inventory.setItem(9, zatoichi)
				}
			}
		}

		fun removeZatoichi() {
			Bukkit.getServer().onlinePlayers.forEach { player ->
				var zatoichiIndex = player.inventory.contents.indexOfFirst { itemStack ->
					if (itemStack != null)
						isHalfZatoichi(itemStack)
					else
						false
				}

				if (zatoichiIndex != -1) {
					player.inventory.clear(zatoichiIndex)
				}
			}
		}

		fun isHalfZatoichi(item: ItemStack?): Boolean {
			if (item?.type == Material.IRON_SWORD) {
				if (item.itemMeta.displayName.startsWith("Half Zatoichi")) {
					return true
				}
			}
			return false
		}
	}
}