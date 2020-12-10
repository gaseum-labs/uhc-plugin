package com.codeland.uhc.phase

import com.codeland.uhc.UHCPlugin
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

class DimensionBar(var bossBar: BossBar, var world: World) {
	companion object {
		var dimensionBars = emptyArray<DimensionBar>()

		fun createBossBars(worlds: List<World>) {
			dimensionBars = Array(worlds.size) { i ->
				val key = NamespacedKey(UHCPlugin.plugin, "B$i")

				DimensionBar(Bukkit.getBossBar(key)
					?: Bukkit.createBossBar(key, "", BarColor.WHITE, BarStyle.SOLID), worlds[i])
			}
		}

		fun setPlayerBarDimension(player: Player) {
			var world = player.world

			dimensionBars.forEach { dimensionBar ->
				if (world === dimensionBar.world) dimensionBar.bossBar.addPlayer(player)
				else dimensionBar.bossBar.removePlayer(player)
			}
		}

		fun dimensionOne(player: Player) {
			dimensionBars.forEachIndexed { i, dimensionBar ->
				if (i == 0) dimensionBar.bossBar.addPlayer(player)
				else dimensionBar.bossBar.removePlayer(player)
			}
		}
	}
}