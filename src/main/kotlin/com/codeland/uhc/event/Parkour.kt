package com.codeland.uhc.event

import com.codeland.uhc.core.Lobby
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.ParkourArena
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent

class Parkour : Listener {
	/* checkpoints */
	fun onMove(event: PlayerMoveEvent) {
		val player = event.player
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		if (player.gameMode !== GameMode.ADVENTURE) return

		val newCheckpoint = event.to.block.getRelative(BlockFace.DOWN)

		if (newCheckpoint.type === Material.GOLD_BLOCK) {
			val oldCheckpoint = arena.checkpoints[player.uniqueId]

			if (oldCheckpoint == null || oldCheckpoint.blockKey != newCheckpoint.blockKey) {
				arena.checkpoints[player.uniqueId] = newCheckpoint
				player.sendActionBar(Component.text("New Checkpoint Reached!", NamedTextColor.GOLD))
			}
		}
	}

	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		/* respawn players instead of killing them */
		event.isCancelled = true
		Lobby.resetPlayerStats(player)

		player.gameMode = if (player.uniqueId == arena.owner) {
			GameMode.CREATIVE
		} else {
			GameMode.ADVENTURE
		}

		player.teleport(
			arena.checkpoints[player.uniqueId]?.getRelative(BlockFace.UP)?.location
				?: arena.start.getRelative(BlockFace.UP).location
		)
	}

	fun onBlockPlace(event: BlockPlaceEvent) {
		val player = event.player
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		if (event.blockPlaced.type === Material.LAPIS_BLOCK) {
			arena.start = event.blockPlaced
			player.sendActionBar(Component.text("Parkour start set", NamedTextColor.BLUE))
		}
	}

	fun onBlockDestroy(event: BlockBreakEvent) {
		val player = event.player
		val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

		if (event.block.blockKey == arena.start.blockKey) {
			arena.start = arena.defaultStart()
			player.sendActionBar(Component.text("Parkour start reset", NamedTextColor.RED))
		}
	}
}
