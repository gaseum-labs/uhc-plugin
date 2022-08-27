package org.gaseumlabs.uhc.chc.chcs

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.chc.CHCType
import org.gaseumlabs.uhc.util.Action
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.gaseumlabs.uhc.util.Util
import java.util.*

class Pests(type: CHCType, game: Game) : CHC<Nothing?>(type, game) {
	override fun defaultData() = null

	override fun customDestroy() {
		/* remove all pests from the game */
		PlayerData.playerDataList.filter { (_, playerData) -> playerData.undead() }.forEach { (uuid, _) ->
			Action.playerAction(uuid) { player ->
				player.gameMode = GameMode.SPECTATOR
			}
		}
	}

	override fun onStartPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player ->
			if (PlayerData.get(player).undead()) {
				player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 4.0
			}
		}
	}

	override fun eventListener(): Listener {
		return object : Listener {
			@EventHandler
			fun onCraft(event: CraftItemEvent) {
				if (Util.binarySearch(event.recipe.result.type, banList)) event.isCancelled = true
			}

			@EventHandler
			fun onMobAnger(event: EntityTargetLivingEntityEvent) {
				val target = event.target as? Player ?: return
				if (PlayerData.get(target).undead()) event.isCancelled = true
			}
		}
	}

	companion object {
		val banList = arrayOf(
			Material.BOW,
			Material.DIAMOND_PICKAXE,
			Material.DIAMOND_AXE,
			Material.DIAMOND_HOE,
			Material.DIAMOND_SHOVEL,
			Material.DIAMOND_SWORD,
			Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.DIAMOND_BOOTS
		)

		init {
			banList.sort()
		}
	}
}