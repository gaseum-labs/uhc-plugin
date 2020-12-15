package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.waiting.LobbyPvp
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerInteractEvent

class PvpListener : Listener {
	@EventHandler
	fun onBowShoot(event: EntityShootBowEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			val projectile = event.projectile as? AbstractArrow

			if (projectile != null) {
				val player = projectile.shooter as? Player

				if (player != null) LobbyPvp.getPvpData(player).stillTime = 0
			}
		}
	}

	@EventHandler
	fun onStartMining(event: BlockDamageEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			LobbyPvp.getPvpData(event.player).stillTime = 0
		}
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			LobbyPvp.getPvpData(event.player).stillTime = 0
		}
	}

	@EventHandler
	fun onInventory(event: InventoryInteractEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			LobbyPvp.getPvpData(event.whoClicked as Player).stillTime = 0
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			LobbyPvp.getPvpData(event.player).stillTime = 0
		}
	}
}