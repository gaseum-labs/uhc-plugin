package org.gaseumlabs.uhc.event

import org.bukkit.*
import org.bukkit.Material.SHIELD
import org.bukkit.block.Banner
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType.*
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.event.inventory.InventoryType.LOOM
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.command.ParticipantTeamCommands
import org.gaseumlabs.uhc.core.UHC

class TeamShield : Listener {
	@EventHandler
	fun onCraftShield(event: CraftItemEvent) {
		val team = UHC.game?.teams?.playersTeam(event.whoClicked.uniqueId) ?: return
		val item = event.currentItem ?: return

		if (item.type === SHIELD) {
			val shieldMeta = item.itemMeta as BlockStateMeta
			shieldMeta.blockState = team.bannerPattern
			item.itemMeta = shieldMeta
		}
	}

	fun isSelectLoom(inventory: Inventory, player: Player): Boolean {
		return inventory.type === LOOM && UHC.game == null
	}

	@EventHandler
	fun onCloseInventory(event: InventoryCloseEvent) {
		if (isSelectLoom(event.inventory, event.player as Player)) {
			val items = event.player.inventory.contents ?: return

			for (item in items) {
				if (item != null && (
					dyes.contains(item.type) ||
					banners.contains(item.type)
					)
				) {
					item.amount = 0
				}
			}
		}
	}

	@EventHandler
	fun onDropItem(event: PlayerDropItemEvent) {
		if (isSelectLoom(event.player.openInventory.topInventory, event.player)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onLoom(event: InventoryClickEvent) {
		if (!isSelectLoom(event.inventory, event.whoClicked as Player)) return

		val team = UHC.preGameTeams.playersTeam(event.whoClicked.uniqueId) ?: return

		if (event.slot == 3) {
			team.bannerPattern = bannerMetaToBlockData(event.currentItem ?: return)
		}
	}

	companion object {
		val dyes = arrayOf(
			Material.WHITE_DYE,
			Material.ORANGE_DYE,
			Material.MAGENTA_DYE,
			Material.LIGHT_BLUE_DYE,
			Material.YELLOW_DYE,
			Material.LIME_DYE,
			Material.PINK_DYE,
			Material.GRAY_DYE,
			Material.LIGHT_GRAY_DYE,
			Material.CYAN_DYE,
			Material.PURPLE_DYE,
			Material.BLUE_DYE,
			Material.BROWN_DYE,
			Material.GREEN_DYE,
			Material.RED_DYE,
			Material.BLACK_DYE,
		)

		val banners = arrayOf(
			Material.WHITE_BANNER,
			Material.ORANGE_BANNER,
			Material.MAGENTA_BANNER,
			Material.LIGHT_BLUE_BANNER,
			Material.YELLOW_BANNER,
			Material.LIME_BANNER,
			Material.PINK_BANNER,
			Material.GRAY_BANNER,
			Material.LIGHT_GRAY_BANNER,
			Material.CYAN_BANNER,
			Material.PURPLE_BANNER,
			Material.BLUE_BANNER,
			Material.BROWN_BANNER,
			Material.GREEN_BANNER,
			Material.RED_BANNER,
			Material.BLACK_BANNER,
		)

		fun dyeColorToDye(dyeColor: DyeColor): Material {
			return dyes[dyeColor.ordinal]
		}

		fun dyeColorToBanner(dyeColor: DyeColor): Material {
			return banners[dyeColor.ordinal]
		}

		private val defaultPatterns = arrayOf(
			GRADIENT_UP,
			CIRCLE_MIDDLE,
			BORDER,
			DIAGONAL_LEFT,
			RHOMBUS_MIDDLE,
			CURLY_BORDER,
			CROSS,
		)

		fun randomBannerPattern(color0: DyeColor, color1: DyeColor): Banner {
			val meta = Bukkit.getItemFactory().getItemMeta(SHIELD) as BlockStateMeta
			val blockState = meta.blockState as Banner

			blockState.baseColor = color0
			blockState.addPattern(Pattern(color1, defaultPatterns.random()))

			return blockState
		}

		fun bannerMetaToBlockData(bannerItem: ItemStack): Banner {
			val shieldMeta = Bukkit.getItemFactory().getItemMeta(SHIELD) as BlockStateMeta
			val blockState = shieldMeta.blockState as Banner

			blockState.baseColor = DyeColor.values()[banners.indexOf(bannerItem.type)]
			blockState.patterns = (bannerItem.itemMeta as BannerMeta).patterns

			return blockState
		}

		fun blockDataToBannerMeta(blockState: Banner): ItemStack {
			val bannerMaterial = banners[blockState.baseColor.ordinal]
			val meta = Bukkit.getItemFactory().getItemMeta(bannerMaterial) as BannerMeta

			meta.patterns = blockState.patterns

			val stack = ItemStack(bannerMaterial, 1)
			stack.itemMeta = meta
			return stack
		}

		fun checkBannerColors(blockState: Banner, color0: DyeColor, color1: DyeColor): Boolean {
			return (blockState.baseColor == color0 || blockState.baseColor == color1) && blockState.patterns.all { pattern ->
				pattern.color === color0 || pattern.color === color1
			}
		}
	}
}
