package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BrewingStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.BrewerInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

class Brew : Listener {
	fun createCustomPotion(potionType: PotionType, material: Material, name: String, duration: Int, amplifier: Int): ItemStack {
		val effectType = potionType.effectType ?: PotionEffectType.POISON

		val potion = ItemStack(material)

		val meta = potion.itemMeta as PotionMeta

		meta.setDisplayName("${ChatColor.RESET}Potion of $name")
		meta.color = effectType.color
		meta.addCustomEffect(PotionEffect(effectType, duration, amplifier), true)

		potion.itemMeta = meta

		return potion
	}

	fun createDefaultPotion(potionData: PotionData, material: Material): ItemStack {
		val potion = ItemStack(material)

		val meta = potion.itemMeta as PotionMeta

		meta.basePotionData = potionData

		potion.itemMeta = meta

		return potion
	}

	class PotionInfo(val type: PotionType, val name: String, val baseDuration: Int, val extendedDuration: Int, val amplifiedDuration: Int)

	val POISON_INFO = PotionInfo(PotionType.POISON, "Poison", 150, 175, 72)
	val REGEN_INFO = PotionInfo(PotionType.REGEN, "Regeneration", 250, 500, 225)
	val STRENGTH_INFO = PotionInfo(PotionType.STRENGTH, "Strength", 1800, 3600, 600)

	val potionInfoList = arrayOf(POISON_INFO, REGEN_INFO, STRENGTH_INFO)

	fun isExtended(itemStack: ItemStack): Boolean {
		val meta = itemStack.itemMeta as PotionMeta

		if (meta.hasCustomEffects()) {
			val effect = meta.customEffects[0]

			for (potionInfo in potionInfoList) {
				if (potionInfo.type.effectType == effect.type) {
					return effect.duration == potionInfo.extendedDuration
				}
			}

			return false

		} else {
			return meta.basePotionData.isExtended
		}
	}

	fun isUpgraded(itemStack: ItemStack): Boolean {
		val meta = itemStack.itemMeta as PotionMeta

		return if (meta.hasCustomEffects()) meta.customEffects[0].amplifier == 1
		else meta.basePotionData.isUpgraded
	}

	fun isSplash(itemStack: ItemStack): Boolean {
		return itemStack.type == Material.SPLASH_POTION
	}

	fun isType(itemStack: ItemStack, type: PotionType): Boolean {
		val meta = itemStack.itemMeta as PotionMeta

		return if (meta.hasCustomEffects()) meta.customEffects[0].type == type.effectType
		else meta.basePotionData.type == type
	}

	val PATH_EXTEND = 1
	val PATH_UPGRADE = 2
	val PATH_SPLASH = 4

	class PotionPath(
		val ingredient: Material,
		val affectedType: PotionType,
		val createdType: PotionType,
		val extend: Boolean,
		val upgrade: Boolean,
		val splash: Boolean,
		val creator: (ItemStack) -> ItemStack
	)

	private fun baseCreatePath(ingredient: Material, affectedType: PotionType, createdType: PotionType, modifiers: Int, creator: (ItemStack) -> ItemStack): PotionPath {
		val extend = modifiers.and(1) == 1
		val upgrade = modifiers.shr(1).and(1) == 1
		val splash = modifiers.shr(2).and(1) == 1

		return PotionPath(ingredient, affectedType, createdType, extend, upgrade, splash, creator)
	}

	private fun finalModifiers(itemStack: ItemStack, modifiers: Int): Triple<Boolean, Boolean, Boolean> {
		val extend = modifiers.and(1) == 1
		val upgrade = modifiers.shr(1).and(1) == 1
		val splash = modifiers.shr(2).and(1) == 1

		return Triple(
			isExtended(itemStack) || extend,
			isUpgraded(itemStack) || upgrade,
			isSplash(itemStack) || splash
		)
	}

	fun createCustomPath(ingredient: Material, affectedType: PotionType, info: PotionInfo, modifiers: Int = 0): PotionPath {
		return baseCreatePath(ingredient, affectedType, info.type, modifiers) { itemStack ->
			val (extended, upgraded, splashed) = finalModifiers(itemStack, modifiers)
			createCustomPotion(info.type, if (splashed) Material.SPLASH_POTION else Material.POTION, info.name, when { extended -> info.extendedDuration; upgraded -> info.amplifiedDuration; else -> info.baseDuration }, if (upgraded) 1 else 0)
		}
	}

	fun createDefaultPath(ingredient: Material, affectedType: PotionType, potionType: PotionType, modifiers: Int = 0): PotionPath {
		return baseCreatePath(ingredient, affectedType, potionType, modifiers) { itemStack ->
			val (extended, upgraded, splashed) = finalModifiers(itemStack, modifiers)
			createDefaultPotion(PotionData(potionType, extended, upgraded), if (splashed) Material.SPLASH_POTION else Material.POTION)
		}
	}

	val replacerPotionPathList = arrayOf(
		createCustomPath(Material.SPIDER_EYE, PotionType.AWKWARD, POISON_INFO),
		createCustomPath(Material.GHAST_TEAR, PotionType.AWKWARD, REGEN_INFO),
		createCustomPath(Material.BLAZE_POWDER, PotionType.AWKWARD, STRENGTH_INFO)
	)

	val customPotionPathList = arrayOf(
		createCustomPath(      Material.REDSTONE, PotionType.POISON, POISON_INFO,  PATH_EXTEND),
		createCustomPath(Material.GLOWSTONE_DUST, PotionType.POISON, POISON_INFO, PATH_UPGRADE),
		createCustomPath(     Material.GUNPOWDER, PotionType.POISON, POISON_INFO,  PATH_SPLASH),

		createDefaultPath(Material.FERMENTED_SPIDER_EYE, PotionType.POISON, PotionType.INSTANT_DAMAGE),

		createCustomPath(      Material.REDSTONE,  PotionType.REGEN,  REGEN_INFO,  PATH_EXTEND),
		createCustomPath(Material.GLOWSTONE_DUST,  PotionType.REGEN,  REGEN_INFO, PATH_UPGRADE),
		createCustomPath(     Material.GUNPOWDER,  PotionType.REGEN,  REGEN_INFO,  PATH_SPLASH),

		createCustomPath(      Material.REDSTONE, PotionType.STRENGTH, STRENGTH_INFO,  PATH_EXTEND),
		createCustomPath(Material.GLOWSTONE_DUST, PotionType.STRENGTH, STRENGTH_INFO, PATH_UPGRADE),
		createCustomPath(     Material.GUNPOWDER, PotionType.STRENGTH, STRENGTH_INFO,  PATH_SPLASH)
	)

	val brewTaskMap = HashMap<Block, Int>()

	fun registerBrewTask(stand: BrewingStand) {
		val block = stand.block
		var time = 400

		val taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			stand.brewingTime = time
			--time

			if (time == 0) {
				replaceItems(stand.inventory, customPotionPathList)

				--stand.fuelLevel

				cancelBrewTask(block)
			}
		}, 0, 1)

		brewTaskMap[block] = taskID
	}

	fun isBrewing(stand: BrewingStand): Boolean {
		return brewTaskMap[stand.block] != null
	}

	fun cancelBrewTask(block: Block) {
		val task = brewTaskMap.remove(block)

		if (task != null) Bukkit.getScheduler().cancelTask(task)
	}

	private fun internalOnInventory(inventory: Inventory) {
		if (inventory !is BrewerInventory) return
		val stand = inventory.holder?.block?.getState(false) as BrewingStand?

		/* if the player has a brewing inventory open */
		if (stand != null) {
			/* before the event resolves, was there already an ingredient */
			/* used to tell if the ingredient is just now being added or removed */
			val oldIngredientType = inventory.ingredient?.type

			SchedulerUtil.nextTick {
				val ingredient = inventory.ingredient

				/* when the type of brew changes, cancel current brew */
				if (oldIngredientType != ingredient?.type) {
					cancelBrewTask(stand.block)

					/* start brewing when a new ingredient is placed */
					/* and the stand is fueled */
					if (ingredient != null && stand.fuelLevel > 0) {
						if(customPotionPathList.any { potionPath ->
							potionPath.ingredient == ingredient.type && containsAffected(inventory, potionPath)
						}) registerBrewTask(stand)
					}
				}
			}
		}
	}

	/*
	 * events fired whenever the player clicks in a brewing inventory
	 * or whenever a hopper dispenses into a brewing inventory
	 */

	@EventHandler
	fun onInventoryDrag(event: InventoryDragEvent) {
		internalOnInventory(event.inventory)
	}
	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		internalOnInventory(event.inventory)
	}
	@EventHandler
	fun onHopper(event: InventoryMoveItemEvent) {
		internalOnInventory(event.destination)
	}

	fun containsAffected(brewerInventory: BrewerInventory, potionPath: PotionPath): Boolean {
		for (i in 0..2) if (itemAffected(brewerInventory.getItem(i), potionPath)) return true
		return false
	}

	fun itemAffected(itemStack: ItemStack?, potionPath: PotionPath): Boolean {
		if (itemStack == null) return false

		val extended = isExtended(itemStack)
		val upgraded = isUpgraded(itemStack)

		return isType(itemStack, potionPath.affectedType) &&
			(!potionPath.extend || (!extended && !upgraded)) &&
			(!potionPath.upgrade || (!upgraded && !extended)) &&
			(!potionPath.splash || !isSplash(itemStack)) &&
			(!extended || potionPath.createdType.isExtendable) &&
			(!upgraded || potionPath.createdType.isUpgradeable)
	}

	fun replaceItems(inventory: BrewerInventory, pathList: Array<PotionPath>): Boolean {
		var replaced = false
		val ingredientStack = inventory.ingredient

		/* check every bottle slot to try to replace */
		for (i in 0..2) {
			val potionStack = inventory.getItem(i)

			if (potionStack != null) {
				/* check every replacer brewing recipe to see if it applies */
				pathList.any { potionPath ->
					if (ingredientStack?.type == potionPath.ingredient && itemAffected(potionStack, potionPath)) {
						/* replace the old bottle with the result of the custom recipe */
						inventory.setItem(i, potionPath.creator(potionStack))

						/* mark that at least one potion was replaced */
						replaced = true
						true

					} else {
						false
					}
				}
			}
		}

		/* use up the ingredient */
		if (replaced && ingredientStack != null) --ingredientStack.amount

		return replaced
	}

	/**
	 * this is called whenever a non-fake brew is completed
	 * EX: when the default awkward to poison is brewed
	 *
	 * replaces the results of the brew
	 */
	@EventHandler
	fun onBrew(event: BrewEvent) {
		if (replaceItems(event.contents, replacerPotionPathList)) event.isCancelled = true
	}

	@EventHandler
	fun onDestroy(event: BrewEvent) {
		if (event.block.type == Material.BREWING_STAND) {
			cancelBrewTask(event.block)
		}
	}
}
