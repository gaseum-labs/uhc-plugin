package com.codeland.uhc.phase

import com.codeland.uhc.UHCPlugin
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

class DimensionBar(var bossBar: BossBar, var world: World, var key: NamespacedKey) {
	companion object {
		var dimensionBars = emptyArray<DimensionBar>()

		fun createBossBars(worlds: List<World>) {
			dimensionBars.forEach { dimensionBar ->
				dimensionBar.bossBar.players.forEach { dimensionBar.bossBar.removePlayer(it) }
				Bukkit.removeBossBar(dimensionBar.key)
			}

			dimensionBars = Array(worlds.size) { i ->
				val key = NamespacedKey(UHCPlugin.plugin, "B$i")

				DimensionBar(
					Bukkit.getBossBar(key) ?: Bukkit.createBossBar(key, "", BarColor.WHITE, BarStyle.SOLID),
					worlds[i],
					key
				)
			}
		}

		fun setPlayerBarDimension(player: Player) {
			dimensionBars.forEach { dimensionBar ->
				if (player.world === dimensionBar.world) dimensionBar.bossBar.addPlayer(player)
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