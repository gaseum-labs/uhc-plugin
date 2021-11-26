package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BrewingStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.*

class Brew : Listener {
	companion object {
		fun createCustomPotion(
			potionType: PotionType,
			material: Material,
			name: String,
			duration: Int,
			amplifier: Int,
		): ItemCreator {
			val effectType = potionType.effectType ?: PotionEffectType.POISON

			return ItemCreator.fromType(material).name("${ChatColor.RESET}Potion of $name")
				.customMeta<PotionMeta> { meta ->
					meta.color = effectType.color
					meta.addCustomEffect(PotionEffect(effectType, duration, amplifier), true)
				}
		}

		fun externalCreatePotion(
			material: Material,
			info: PotionInfo,
			extended: Boolean,
			amplified: Boolean,
		): ItemCreator {
			return createCustomPotion(
				info.type,
				material,
				info.name,
				if (extended) info.extendedDuration else if (amplified) info.amplifiedDuration else info.baseDuration,
				if (amplified) 1 else 0)
		}

		fun createDefaultPotion(material: Material, potionData: PotionData): ItemCreator {
			return ItemCreator.fromType(material).customMeta<PotionMeta> { it.basePotionData = potionData }
		}

		class PotionInfo(
			val type: PotionType,
			val name: String,
			val baseDuration: Int,
			val extendedDuration: Int,
			val amplifiedDuration: Int,
		)

		val POISON_INFO = PotionInfo(PotionType.POISON, "Poison", 150, 325, 144)
		val REGEN_INFO = PotionInfo(PotionType.REGEN, "Regeneration", 250, 500, 225)
		val STRENGTH_INFO = PotionInfo(PotionType.STRENGTH, "Strength", 0, 0, 0)
		val WEAKNESS_INFO = PotionInfo(PotionType.WEAKNESS, "Weakness", 600, 1200, 0)

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

		val potionInfoList = arrayOf(POISON_INFO, REGEN_INFO, STRENGTH_INFO, WEAKNESS_INFO)

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
			val creator: (ItemStack) -> ItemStack,
		) {
			fun applies(inventory: BrewerInventory): Boolean {
				return ingredient == inventory.ingredient?.type && containsAffected(inventory)
			}

			fun containsAffected(brewerInventory: BrewerInventory): Boolean {
				for (i in 0..2) if (itemAffected(brewerInventory.getItem(i))) return true
				return false
			}

			fun itemAffected(itemStack: ItemStack?): Boolean {
				if (itemStack == null) return false

				val extended = isExtended(itemStack)
				val upgraded = isUpgraded(itemStack)

				return isType(itemStack, affectedType) &&
				(!extend || (!extended && !upgraded)) &&
				(!upgrade || (!upgraded && !extended)) &&
				(!splash || !isSplash(itemStack)) &&
				(!extended || createdType.isExtendable) &&
				(!upgraded || createdType.isUpgradeable)
			}
		}

		private fun baseCreatePath(
			ingredient: Material,
			affectedType: PotionType,
			createdType: PotionType,
			modifiers: Int,
			creator: (ItemStack) -> ItemStack,
		): PotionPath {
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

		fun createCustomPath(
			ingredient: Material,
			affectedType: PotionType,
			info: PotionInfo,
			modifiers: Int,
		): PotionPath {
			return baseCreatePath(ingredient, affectedType, info.type, modifiers) { itemStack ->
				val (extended, upgraded, splashed) = finalModifiers(itemStack, modifiers)
				createCustomPotion(info.type,
					if (splashed) Material.SPLASH_POTION else Material.POTION,
					info.name,
					when {
						extended -> info.extendedDuration; upgraded -> info.amplifiedDuration; else -> info.baseDuration
					},
					if (upgraded) 1 else 0).create()
			}
		}

		fun createDefaultPath(
			ingredient: Material,
			affectedType: PotionType,
			potionType: PotionType,
			modifiers: Int,
		): PotionPath {
			return baseCreatePath(ingredient, affectedType, potionType, modifiers) { itemStack ->
				val (extended, upgraded, splashed) = finalModifiers(itemStack, modifiers)
				createDefaultPotion(if (splashed) Material.SPLASH_POTION else Material.POTION,
					PotionData(potionType, extended, upgraded)).create()
			}
		}

		val replacerPotionPathList = arrayOf(
			createCustomPath(Material.SPIDER_EYE, PotionType.AWKWARD, POISON_INFO, 0),
			createCustomPath(Material.GHAST_TEAR, PotionType.AWKWARD, REGEN_INFO, 0)
		)

		val customPotionPathList = arrayOf(
			createCustomPath(Material.REDSTONE, PotionType.POISON, POISON_INFO, PATH_EXTEND),
			createCustomPath(Material.GLOWSTONE_DUST, PotionType.POISON, POISON_INFO, PATH_UPGRADE),
			createCustomPath(Material.GUNPOWDER, PotionType.POISON, POISON_INFO, PATH_SPLASH),

			createDefaultPath(Material.FERMENTED_SPIDER_EYE, PotionType.POISON, PotionType.INSTANT_DAMAGE, 0),

			createCustomPath(Material.REDSTONE, PotionType.REGEN, REGEN_INFO, PATH_EXTEND),
			createCustomPath(Material.GLOWSTONE_DUST, PotionType.REGEN, REGEN_INFO, PATH_UPGRADE),
			createCustomPath(Material.GUNPOWDER, PotionType.REGEN, REGEN_INFO, PATH_SPLASH),

			createCustomPath(Material.FERMENTED_SPIDER_EYE, PotionType.WATER, WEAKNESS_INFO, 0),
			createCustomPath(Material.REDSTONE, PotionType.WEAKNESS, WEAKNESS_INFO, PATH_EXTEND),
			createCustomPath(Material.GUNPOWDER, PotionType.WEAKNESS, WEAKNESS_INFO, PATH_SPLASH),
		)

		val bannedPaths = arrayOf(
			createCustomPath(Material.BLAZE_POWDER, PotionType.AWKWARD, STRENGTH_INFO, 0),
			createCustomPath(Material.REDSTONE, PotionType.STRENGTH, STRENGTH_INFO, PATH_EXTEND),
			createCustomPath(Material.GLOWSTONE_DUST, PotionType.STRENGTH, STRENGTH_INFO, PATH_UPGRADE),
			createCustomPath(Material.GUNPOWDER, PotionType.STRENGTH, STRENGTH_INFO, PATH_SPLASH)
		)
	}

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
						/* reject ingredients for banned paths */
						if (bannedPaths.any { it.applies(inventory) }) {
							inventory.ingredient = null
							stand.world.dropItem(stand.location.add(0.5, 1.0, 0.5), ingredient.clone())

						} else if (customPotionPathList.any { it.applies(inventory) }) {
							registerBrewTask(stand)
						}
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

	fun replaceItems(inventory: BrewerInventory, pathList: Array<PotionPath>): Boolean {
		var replaced = false
		val ingredientStack = inventory.ingredient

		/* check every bottle slot to try to replace */
		for (i in 0..2) {
			val potionStack = inventory.getItem(i)

			if (potionStack != null) {
				val path = pathList.find { it.applies(inventory) }

				if (path != null) {
					/* replace the old bottle with the result of the custom recipe */
					inventory.setItem(i, path.creator(potionStack))

					/* mark that at least one potion was replaced */
					replaced = true
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
