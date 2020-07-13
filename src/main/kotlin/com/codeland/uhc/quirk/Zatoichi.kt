package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

object Zatoichi {
	fun start(uhc : UHC, length : Long) {
		for (player in Bukkit.getServer().onlinePlayers) {
			val zatoichi = ItemStack(Material.IRON_SWORD)
			val meta = zatoichi.itemMeta.clone()
			meta.setDisplayName("Half Zatoichi")
			meta.isUnbreakable = true
			zatoichi.itemMeta = meta
			player.inventory.setItem(9, zatoichi)
		}
		object : BukkitRunnable() {
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
		}.runTaskTimer(GameRunner.plugin!!, 0, 1)
	}

	private class Senshi(name : String) {
		private val name = name
		private var holdingZatoichi = false

		fun updatePlayer(player : Player) : Boolean {
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

						//if (player.absorptionAmount < 5.0) {
						//	if (player.health > 5.0 - player.absorptionAmount) {
						//		player.health -= 5.0 - player.absorptionAmount
						//	} else {
						//		player.health = 0.0
						//	}
						//	player.absorptionAmount = 0.0
						//} else {
						//	player.absorptionAmount -= 5.0
						//}
					}
				} else {
					holdingZatoichi = isHalfZatoichi(player.inventory.itemInMainHand)
				}
				return true
			}
			return false
		}

	}

	public fun isHalfZatoichi(item : ItemStack?) : Boolean {
		if (item?.type == Material.IRON_SWORD) {
			if (item.itemMeta.displayName.startsWith("Half Zatoichi")) {
				return true
			}
		}
		return false
	}

}