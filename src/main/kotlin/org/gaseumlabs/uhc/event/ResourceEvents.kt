package org.gaseumlabs.uhc.event

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.util.extensions.BlockExtensions.samePlace
import org.gaseumlabs.uhc.world.regenresource.*

class ResourceEvents : Listener {
	@EventHandler
	fun onDamageEntity(event: EntityDamageByEntityEvent) {
		val game = UHC.game ?: return
		val player = event.damager as? Player ?: return

		val regenResource = regenResourceTypeEntity(game, event.entity)?.regenResource ?: return

		if (findVein(regenResource, game) { vein ->
				vein is VeinEntity && vein.entity.entityId == event.entity.entityId
			}) markCollected(game, player, regenResource)
	}

	@EventHandler
	fun onBreakBlock(event: BlockDamageEvent) {
		val game = UHC.game ?: return

		val brokenBlock = event.block
		val player = event.player

		val regenResource = regenResourceTypeBlock(game, brokenBlock)?.regenResource ?: return

		/* is this block part of a vein? */
		if (findVein(regenResource, game) { vein ->
				vein is VeinBlock && vein.blocks.any { it.samePlace(brokenBlock) }
			})
		/* mark the player's team as collecting this vein */
			markCollected(game, player, regenResource)

	}

	fun markCollected(game: Game, player: Player, regenResource: RegenResource) {
		val team = game.teams.playersTeam(player.uniqueId)
		if (team != null) {
			val collected = game.resourceScheduler.getVeinData(team, regenResource).collected
			collected[game.phase.phaseType] = collected.getOrPut(game.phase.phaseType) { 0 } + 1
		}
	}

	fun regenResourceTypeBlock(game: Game, brokenBlock: Block): ResourceDescriptionBlock? {
		return game.resourceScheduler.resourceDescriptions.find { resourceDescription ->
			resourceDescription is ResourceDescriptionBlock && resourceDescription.isBlock(brokenBlock)
		} as ResourceDescriptionBlock?
	}

	fun regenResourceTypeEntity(game: Game, entity: Entity): ResourceDescriptionEntity? {
		return game.resourceScheduler.resourceDescriptions.find { resourceDescription ->
			resourceDescription is ResourceDescriptionEntity && resourceDescription.isEntity(entity)
		} as ResourceDescriptionEntity?
	}

	/**
	 * also automatically removes the vein from the current list
	 */
	fun findVein(type: RegenResource, game: Game, compare: (vein: Vein) -> Boolean): Boolean {
		for ((_, teamVeins) in game.resourceScheduler.veinDataList) {
			val veinData = teamVeins[type.ordinal]

			for (i in veinData.current.indices) {
				val vein = veinData.current[i]

				if (compare(vein)) {
					veinData.current.removeAt(i)
					return true
				}
			}
		}

		return false
	}
}
