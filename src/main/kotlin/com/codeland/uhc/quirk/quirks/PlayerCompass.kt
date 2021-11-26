package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Action
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class PlayerCompass(type: QuirkType, game: Game) : Quirk(type, game) {
	var taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, ::compassTick, 0, 10)

	override fun customDestroy() {
		Bukkit.getScheduler().cancelTask(taskID)
	}

	override fun onStartPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player -> player.inventory.addItem(createCompass()) }
	}

	override fun onEndPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player -> revokeCompass(player) }
	}

	fun filterDrops(drops: MutableList<ItemStack>) {
		drops.removeAll { itemStack -> isCompass(itemStack) }
	}

	fun compassTick() {
		val players = ArrayList<Player>()

		/* grab all players that will have their compass updated */
		PlayerData.playerDataList.forEach { (uuid, data) ->
			val player = Bukkit.getPlayer(uuid)

			/* only care about players that are currently online and playing */
			if (player != null && data.alive && data.participating) players.add(player)
		}

		players.forEachIndexed { i, player ->
			var leastDistance = Double.MAX_VALUE
			var leastPlayer = null as Player?

			val team = game.teams.playersTeam(player.uniqueId)

			players.forEachIndexed { j, otherPlayer ->
				if (j != i && otherPlayer.world == player.world && (team == null || team != game.teams.playersTeam(
						otherPlayer.uniqueId))
				) {
					val distance = player.location.distance(otherPlayer.location)

					if (distance < leastDistance) {
						leastDistance = distance
						leastPlayer = otherPlayer
					}
				}
			}

			val targetPlayer = leastPlayer
			if (targetPlayer != null) player.compassTarget = targetPlayer.location
		}
	}

	companion object {
		fun isCompass(itemStack: ItemStack?): Boolean {
			return itemStack != null &&
			itemStack.type == Material.COMPASS &&
			itemStack.itemMeta.hasLore() &&
			itemStack.itemMeta.hasDisplayName()
		}

		fun createCompass(): ItemStack {
			return ItemCreator.fromType(Material.COMPASS, false)
				.name("${ChatColor.RESET}${ChatColor.GOLD}Player Compass")
				.lore("From Player Compasses CHC", "${ChatColor.BLACK}${UUID.randomUUID()}")
				.enchant()
				.create()
		}

		fun revokeCompass(player: Player) {
			player.inventory.contents.forEach { itemStack ->
				if (isCompass(itemStack)) itemStack.amount = 0
			}
		}
	}
}
