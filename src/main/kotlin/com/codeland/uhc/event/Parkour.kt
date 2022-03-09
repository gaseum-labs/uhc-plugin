package com.codeland.uhc.event

import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent

class Parkour : Listener {
	/* checkpoints */
	@EventHandler
	fun onMove(event: PlayerMoveEvent) {
		val player = event.player
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		val under = event.to.block.getRelative(BlockFace.DOWN)

		val checkpointType = when {
			under.type === Material.GOLD_BLOCK -> 0
			under.blockKey == arena.start.blockKey -> 1
			else -> -1
		}

		val parkourData = arena.getParkourData(player.uniqueId)
		parkourData.timerGoing = true

		if (checkpointType != -1) {
			val isBuilding = player.gameMode === GameMode.CREATIVE

			if (parkourData.checkpoint.blockKey != under.blockKey) {
				parkourData.checkpoint = under
				player.sendActionBar(Component.text(
					if (isBuilding) {
						"${ChatColor.AQUA}Set Testing Start"
					} else {
						arrayOf(
							"${ChatColor.GOLD}New Checkpoint Reached!",
							"${ChatColor.BLUE}Reset to Start!"
						)[checkpointType]
					}
				))
			}
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		/* respawn players instead of killing them */
		event.isCancelled = true

		arena.enterPlayer(player, false, true)
	}

	@EventHandler
	fun onBlockPlace(event: BlockPlaceEvent) {
		val player = event.player
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		if (event.blockPlaced.type === Material.LAPIS_BLOCK) {
			arena.start = event.blockPlaced
			player.sendActionBar(Component.text("Parkour start set", NamedTextColor.BLUE))
		}
	}

	@EventHandler
	fun onBlockDestroy(event: BlockBreakEvent) {
		val player = event.player
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		if (event.block.blockKey == arena.start.blockKey) {
			arena.start = arena.defaultStart()
			player.sendActionBar(Component.text("Parkour start reset", NamedTextColor.RED))
		}
	}
}
