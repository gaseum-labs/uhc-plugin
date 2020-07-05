package com.codeland.uhc.phases.grace

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.logging.Level

class GraceZatoichi : GraceDefault() {

	override fun start(uhc : UHC, length : Long) {
		super.start(uhc, length)
		object : BukkitRunnable() {
			override fun run() {
				for (player in Bukkit.getServer().onlinePlayers) {
					val zatoichi = ItemStack(Material.IRON_SWORD)
					zatoichi.itemMeta.setDisplayName("Half Zatoichi")
					player.inventory.setItem(10, zatoichi)
				}
			}
		}.runTaskLater(GameRunner.plugin!!, 1)
	}
}