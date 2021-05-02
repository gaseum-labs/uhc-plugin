package com.codeland.uhc.phase

import com.codeland.uhc.UHCPlugin
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

class WorldBar(var bossBar: BossBar, var world: World, var key: NamespacedKey) {
	companion object {
		var worldBars = ArrayList<WorldBar>()

		private fun keygen(name: String): NamespacedKey {
			return NamespacedKey(UHCPlugin.plugin, "_B$name")
		}

		private fun defaultBar(key: NamespacedKey): BossBar {
			return Bukkit.createBossBar(key, "", BarColor.WHITE, BarStyle.SOLID)
		}

		fun initWorldBars(worlds: List<World>) {
			worldBars = worlds.map { world ->
				val key = keygen(world.name)

				Bukkit.getBossBar(key)?.removeAll()
				Bukkit.removeBossBar(key)

				WorldBar(defaultBar(key), world, key)
			} as ArrayList<WorldBar>

			Bukkit.getOnlinePlayers().forEach { setPlayerBarDimension(it) }
		}

		fun resetWorldBar(oldWorld: World?, newWorld: World) {
			val worldBar = worldBars.find { it.world === oldWorld }

			if (worldBar == null) {
				val key = keygen(newWorld.name)
				worldBars.add(WorldBar(defaultBar(key), newWorld, key))

			} else {
				val key = worldBar.key
				Bukkit.getBossBar(key)?.removeAll()
				Bukkit.removeBossBar(key)

				worldBar.bossBar = defaultBar(key)
				worldBar.world = newWorld
			}
		}

		fun setPlayerBarDimension(player: Player) {
			worldBars.forEach { dimensionBar ->
				if (player.world === dimensionBar.world)
					dimensionBar.bossBar.addPlayer(player)
				else
					dimensionBar.bossBar.removePlayer(player)
			}
		}
	}
}