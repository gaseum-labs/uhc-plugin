package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import java.util.*

class Pests(type: QuirkType, game: Game) : Quirk(type, game) {
    override fun customDestroy() {
        /* remove all pests from the game */
        PlayerData.playerDataList.filter { (_, playerData) -> playerData.undead() }.forEach { (uuid, _) ->
            GameRunner.playerAction(uuid) { player ->
	            player.gameMode = GameMode.SPECTATOR
            }
        }
    }

    override fun onStartPlayer(uuid: UUID) {
        GameRunner.playerAction(uuid) { player ->
            if (PlayerData.isUndead(player.uniqueId)) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 4.0
            }
        }
    }

    companion object {
        fun onBecomePest(uuid: UUID) {
	        TeamData.removeFromTeam(arrayListOf(uuid), true, true, true)
        }

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

	    init { banList.sort() }
    }
}