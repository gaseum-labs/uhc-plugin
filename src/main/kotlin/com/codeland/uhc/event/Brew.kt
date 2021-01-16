package com.codeland.uhc.event

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.BrewerInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

class Brew : Listener {
	fun createCustomPotion(potionType: PotionType, duration: Int, amplifier: Int): ItemStack {
		val effectType = potionType.effectType ?: PotionEffectType.POISON

		val potion = ItemStack(Material.POTION)

		val meta = potion.itemMeta as PotionMeta

		meta.setDisplayName("${ChatColor.RESET}Potion of ${effectType.name.toLowerCase().capitalize()}")
		meta.color = effectType.color
		meta.addCustomEffect(PotionEffect(effectType, duration, amplifier), true)

		potion.itemMeta = meta

		return potion
	}

	fun createDefaultPotion(potionData: PotionData): ItemStack {
		val potion = ItemStack(Material.POTION)

		val meta = potion.itemMeta as PotionMeta

		meta.basePotionData = potionData

		potion.itemMeta = meta

		return potion
	}

	class PotionInfo(val type: PotionType, val baseDuration: Int, val extendedDuration: Int, val amplifiedDuration: Int)

	val POISON_INFO = PotionInfo(PotionType.POISON, 150, 72, 175)
	val REGEN_INFO = PotionInfo(PotionType.REGEN, 250, 225, 500)

	val potionInfoList = arrayOf(POISON_INFO, REGEN_INFO)

	fun isExtended(meta: PotionMeta): Boolean {
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

	fun isUpgraded(meta: PotionMeta): Boolean {
		return if (meta.hasCustomEffects()) {
			meta.customEffects[0].amplifier == 1
		} else {
			meta.basePotionData.isUpgraded
		}
	}

	fun isType(meta: PotionMeta, type: PotionType): Boolean {
		return if (meta.hasCustomEffects()) {
			meta.customEffects[0].type == type.effectType
		} else {
			meta.basePotionData.type == type
		}
	}

	class PotionPath(val ingredient: Material, val affectedType: PotionType, val create: (PotionMeta) -> ItemStack)

	fun customCreator(info: PotionInfo, extend: Boolean, upgrade: Boolean): (PotionMeta) -> ItemStack {
		return { meta ->
			val extended = isExtended(meta) || extend
			val upgraded = isUpgraded(meta) || upgrade
			createCustomPotion(info.type, when { extended -> info.extendedDuration; upgraded -> info.amplifiedDuration; else -> info.baseDuration }, if (upgraded) 1 else 0)
		}
	}

	fun defaultCreator(type: PotionType, extend: Boolean, upgrade: Boolean): (PotionMeta) -> ItemStack {
		return { meta ->
			createDefaultPotion(PotionData(type, isExtended(meta) || extend, isUpgraded(meta) || upgrade))
		}
	}

	val potionPathList = arrayOf(
		PotionPath(Material.SPIDER_EYE, PotionType.AWKWARD, customCreator(POISON_INFO, false, false)),
		PotionPath(Material.REDSTONE, PotionType.POISON, customCreator(POISON_INFO, true, false)),
		PotionPath(Material.GLOWSTONE_DUST, PotionType.POISON, customCreator(POISON_INFO, false, true)),
		PotionPath(Material.FERMENTED_SPIDER_EYE, PotionType.POISON, defaultCreator(PotionType.INSTANT_DAMAGE, false, false)),
		PotionPath(Material.GHAST_TEAR, PotionType.AWKWARD, customCreator(REGEN_INFO, false, false)),
		PotionPath(Material.REDSTONE, PotionType.REGEN, customCreator(REGEN_INFO, true, false)),
		PotionPath(Material.GLOWSTONE_DUST, PotionType.REGEN, customCreator(REGEN_INFO, false, true))
	)

	@EventHandler
	fun onInventory(event: InventoryClickEvent) {
		if (event.clickedInventory?.type == InventoryType.BREWING) {
			val brewInventory = event.inventory as BrewerInventory
			val stand = brewInventory.holder

			if (stand != null) {
				stand.brewingTime = 45
			}
		}
	}

	@EventHandler
	fun onBrew(event: BrewEvent) {
		val contents = event.contents

		for (i in contents.contents.indices) {
			val potionStack = contents.contents[i]

			if (potionStack != null) {
				val meta = potionStack.itemMeta as PotionMeta

				potionPathList.any { potionPath ->
					if (potionPath.ingredient == contents.ingredient?.type && isType(meta, potionPath.affectedType)) {
						contents.setItem(i, potionPath.create(meta))
						event.isCancelled = true
						true
					} else {
						false
					}
				}
			}
		}
	}
}
