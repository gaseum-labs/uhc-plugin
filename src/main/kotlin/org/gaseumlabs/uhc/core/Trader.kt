package org.gaseumlabs.uhc.core

import org.bukkit.Bukkit
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.WanderingTrader
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import kotlin.random.Random
import kotlin.random.nextInt

class Trader {
	var currentTrader: WanderingTrader? = null

	private fun createTrade(cost: Int, result: ItemStack, max: Int): MerchantRecipe {
		val trade = MerchantRecipe(result, 0, max, true, cost * 100, 1.0f)
		trade.addIngredient(ItemStack(EMERALD, cost))
		return trade
	}

	private fun createTrader(surface: Block): WanderingTrader {
		val trader = surface.world.spawnEntity(
			surface.location.add(0.5, 1.0, 0.5),
			EntityType.WANDERING_TRADER
		) as WanderingTrader

		trader.recipes = listOf(
			createTrade(1, ItemStack(OXEYE_DAISY), 6),
			createTrade(1, ItemStack(SAND, 9), 6),
			createTrade(1, ItemStack(GLOWSTONE_DUST, 8), 6),
			createTrade(1, ItemStack(SUGAR_CANE, 3), 6),
			createTrade(1, ItemStack(LEATHER), 6),
			createTrade(1, ItemStack(PUFFERFISH_BUCKET), 6),
			createTrade(1, ItemStack(FEATHER), 6),
			createTrade(1, ItemStack(EXPERIENCE_BOTTLE), 6),
			createTrade(2, ItemStack(APPLE), 6),
			createTrade(2, ItemStack(GOLD_INGOT, 2), 6),
			createTrade(2, ItemStack(ENDER_PEARL), 6),
			createTrade(3, ItemStack(MELON_SLICE), 6),
			createTrade(4, ItemStack(DIAMOND), 6),
		)

		return trader
	}

	fun traderTick(currentTick: Int) {
		val trader = currentTrader
		val world = WorldManager.gameWorld!!

		if (trader == null) {
			if (currentTick % 27 != 0) return

			val spawnRadius = ((world.worldBorder.size / 2.0).toInt() - 16).coerceAtMost(250)
			val checkRange = -spawnRadius..spawnRadius
			if (checkRange.isEmpty()) return

			var spawnSurface: Block? = null
			for (tries in 0 until 10) {
				spawnSurface = RegenUtil.surfaceSpreaderOverworld(
					world,
					Random.nextInt(checkRange),
					Random.nextInt(checkRange),
					8
				) { block ->
					!block.isPassable &&
					block.getRelative(0, 1, 0).isPassable &&
					block.getRelative(0, 2, 0).isPassable
				}
				if (spawnSurface != null) {
					break
				}
			}

			if (spawnSurface == null) return

			currentTrader = createTrader(spawnSurface)

		} else {
			if (!trader.isValid) {
				currentTrader = null
				return
			}

			if (!RegenUtil.insideWorldBorder(world, trader.location.blockX, trader.location.blockZ)) {
				trader.remove()
				currentTrader = null
				return
			}

			/* trader stuck/trapped check */
			if (currentTick % 507 == 0) {
				val players = PlayerData.playerDataList.mapNotNull { (uuid, playerData) ->
					if (!playerData.alive) return@mapNotNull null
					Bukkit.getPlayer(uuid)
				}

				if (players.none { player -> trader.hasLineOfSight(player) }) {
					trader.remove()
					currentTrader = null
					return
				}
			}
		}
	}
}
