package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.util.Action
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import java.util.*

class Pests(type: QuirkType, game: Game) : Quirk(type, game) {
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
			if (PlayerData.isUndead(player.uniqueId)) {
				player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 4.0
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