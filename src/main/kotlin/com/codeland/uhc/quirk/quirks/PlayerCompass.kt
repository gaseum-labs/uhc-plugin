package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList

class PlayerCompass(type: QuirkType) : Quirk(type) {
	var taskID = 0

	override fun onEnable() {
		if (UHC.isGameGoing()) {
			taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, ::compassTick, 0, 10)
		}
	}

	override fun onDisable() {
		Bukkit.getScheduler().cancelTask(taskID)
	}

	override val representation: ItemStack
		get() = ItemStack(Material.COMPASS)

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) {
			taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, ::compassTick, 0, 10)
		} else if (phase.type == PhaseType.POSTGAME || phase.type == PhaseType.WAITING) {
			Bukkit.getScheduler().cancelTask(taskID)
		}
	}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> player.inventory.addItem(createCompass()) }
	}

	override fun onEnd(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> revokeCompass(player) }
	}

	companion object {
		fun isCompass(itemStack: ItemStack?): Boolean {
			return itemStack != null &&
				itemStack.type == Material.COMPASS &&
				itemStack.itemMeta.hasLore() &&
				itemStack.itemMeta.hasDisplayName()
		}

		fun createCompass(): ItemStack {
			val compass = ItemStack(Material.COMPASS)
			val meta = compass.itemMeta
			meta.setDisplayName("${ChatColor.RESET}${ChatColor.GOLD}Player Compass")
			meta.lore = listOf("From Player Compasses CHC", "${ChatColor.BLACK}${UUID.randomUUID()}")
			meta.addEnchant(ItemUtil.fakeEnchantment, 0, true)
			compass.itemMeta = meta

			return compass
		}

		fun revokeCompass(player: Player) {
			player.inventory.contents.forEach { itemStack ->
				if (isCompass(itemStack)) itemStack.amount = 0
			}
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

				val team = TeamData.playersTeam(player.uniqueId)

				players.forEachIndexed { j, otherPlayer ->
					if (j != i && otherPlayer.world == player.world && (team == null || team != TeamData.playersTeam(otherPlayer.uniqueId))) {
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
	}
}
