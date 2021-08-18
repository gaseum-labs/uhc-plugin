package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.util.Action
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.WanderingTrader
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import java.util.*

object Trader {
	const val MAX_TRIES = 24

	fun spawnTraderForPlayer(uuid: UUID) {
		var tries = 0
		var taskId = 0

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			if (spawnTrader(uuid) || ++tries >= MAX_TRIES) {
				Bukkit.getScheduler().cancelTask(taskId)
			}
		}, 0, 200)
	}

	fun findTraderLocation(center: Block): Block? {
		for (i in -1..1) for (j in -1..1) if (i != 0 && j != 0) {
			for (x in i * 5 - 2..i * 5 + 2) {
				for (z in j * 5 - 2..j * 5 + 2) {
					for (y in 2..2) {
						val spawnBlock = center.getRelative(x, y, z)
						if (
							spawnBlock.type.isAir &&
							spawnBlock.getRelative(BlockFace.UP).type.isAir &&
							!spawnBlock.getRelative(BlockFace.DOWN).isPassable
						) return spawnBlock
					}
				}
			}
		}

		return null
	}

	fun createTrade(cost: Int, result: ItemStack, max: Int): MerchantRecipe {
		val trade = MerchantRecipe(result, 0, max, true, cost * 100, 1.0f)
		trade.addIngredient(ItemStack(Material.EMERALD, cost))
		return trade
	}

	fun createTrader(block: Block) {
		val trader = block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), EntityType.WANDERING_TRADER) as WanderingTrader

		trader.setRecipe(0, createTrade(1, ItemStack(      Material.OXEYE_DAISY), 4))
		trader.setRecipe(1, createTrade(1, ItemStack(          Material.SAND, 9), 4))
		trader.setRecipe(2, createTrade(1, ItemStack(Material.GLOWSTONE_DUST, 8), 4))
		trader.setRecipe(3, createTrade(1, ItemStack(    Material.SUGAR_CANE, 3), 4))
		trader.setRecipe(4, createTrade(2, ItemStack(            Material.APPLE), 3))
		trader.setRecipe(5, createTrade(3, ItemStack(      Material.MELON_SLICE), 2))
	}

	fun spawnTrader(uuid: UUID): Boolean {
		val playerLocation = Action.getPlayerLocation(uuid)?.block
		if (playerLocation != null) {
			val traderLocation = findTraderLocation(playerLocation)
			if (traderLocation != null) {
				createTrader(traderLocation)

				Bukkit.getPlayer(uuid)?.player?.playSound(
					Sound.sound(
						Sound.Type { Key.key("entity.wandering_trader.ambient") },
						Sound.Source.MASTER,
						1.0f,
						1.0f
					),
					Sound.Emitter.self()
				)

				return true
			}
		}

		return false
	}
}
