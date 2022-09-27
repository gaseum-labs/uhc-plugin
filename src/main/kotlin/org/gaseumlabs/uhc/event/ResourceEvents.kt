package org.gaseumlabs.uhc.event

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.metadata.Metadatable
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.util.extensions.BlockExtensions.samePlace
import org.gaseumlabs.uhc.world.regenresource.*

class ResourceEvents : Listener {
	@EventHandler
	fun onDamageEntity(event: EntityDamageByEntityEvent) {
		val game = UHC.game ?: return
		val player = event.damager as? Player ?: return

		val regenResource =  getTypeFrom(event.entity) ?: return
		val vein = findAndRemoveVein(regenResource, game) { vein ->
			vein is VeinEntity && vein.entity.entityId == event.entity.entityId
		} ?: return

		markCollected(game, player, regenResource, vein.value)
	}

	@EventHandler
	fun onDamageBlock(event: BlockDamageEvent) {
		val game = UHC.game ?: return

		val brokenBlock = event.block
		val player = event.player

		val regenResource = getTypeFrom(brokenBlock) ?: return
		val vein = findAndRemoveVein(regenResource, game) { vein ->
			vein is VeinBlock && vein.blocks.any { it.samePlace(brokenBlock) }
		} ?: return

		markCollected(game, player, regenResource, vein.value)
	}

	private fun markCollected(game: Game, player: Player, regenResource: RegenResource<*>, value: Int) {
		val team = game.teams.playersTeam(player.uniqueId)
		if (team != null) {
			val collected = game.globalResources.getTeamVeinData(team, regenResource).collected
			collected[game.phase.phaseType] = collected.getOrPut(game.phase.phaseType) { 0 } + value
		}
	}

	private fun getTypeFrom(holder: Metadatable) =
		holder.getMetadata(GlobalResources.RESOURCE_KEY).firstOrNull()?.asInt()?.let {
			GlobalResources.resourcesList[it]
		}

	/**
	 * also automatically removes the vein from the current list
	 */
	private inline fun findAndRemoveVein(
		regenResource: RegenResource<*>,
		game: Game,
		compare: (vein: Vein) -> Boolean
	): Vein? {
		val veinList = game.globalResources.getVeinList(regenResource)
		val veinIndex = veinList.indexOfFirst { vein -> compare(vein) }
		return if (veinIndex != -1) veinList.removeAt(veinIndex) else null
	}
}
