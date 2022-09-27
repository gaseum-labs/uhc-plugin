package org.gaseumlabs.uhc.event

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.block.Container
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.core.phase.PhaseType.*
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.team.Teams
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.extensions.ArrayListExtensions.removeRef
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.GlobalResources
import org.gaseumlabs.uhc.world.regenresource.RegenResource
import org.gaseumlabs.uhc.world.regenresource.ResourceId
import org.gaseumlabs.uhc.world.regenresource.Vein

class Fishing : Listener {
	@EventHandler
	fun onFish(event: PlayerFishEvent) {
		val game = UHC.game ?: return
		val team = game.teams.playersTeam(event.player.uniqueId) ?: return
		if (!PlayerData.get(event.player).participating) return
		if (event.player.world !== WorldManager.gameWorld) return

		event.hook.minWaitTime = 0
		event.hook.maxWaitTime = 0

		when (event.state) {
			PlayerFishEvent.State.CAUGHT_FISH -> {
				findVein(game.globalResources, event.hook.location)?.let { (regenResource, vein) ->
					spawnItems(event.caught as Item, listOf(ItemStack(COAL, 1))) //calculateDeficiencies(game.teams, team)
					remove(game.globalResources, regenResource, vein)
				}
			}
			else -> {}
		}
	}

	companion object {
		private fun findVein(globalResources: GlobalResources, location: Location) =
			findVeinIn(globalResources, location, ResourceId.upperFish)
				?: findVeinIn(globalResources, location, ResourceId.lowerFish)

		private fun findVeinIn(globalResources: GlobalResources, location: Location, regenResource: RegenResource<*>) =
			globalResources.getVeinList(regenResource).find {
				it.centerLocation().distance(location) <= 3.0
			}?.let { regenResource to it }

		fun remove(globalResources: GlobalResources, regenResource: RegenResource<*>, vein: Vein) {
			val veinList = globalResources.getVeinList(regenResource)
			veinList.removeRef(vein)
			vein.erase()
		}

		fun spawnItems(original: Item, stacks: List<ItemStack>) {
			val location = original.location
			val velocity = original.velocity

			original.remove()

			stacks.forEach { stack ->
				val item = location.world.dropItem(location, stack)
				item.velocity = velocity
				item.pickupDelay = 0
			}
		}


	}
}