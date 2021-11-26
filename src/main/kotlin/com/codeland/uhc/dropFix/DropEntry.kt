package com.codeland.uhc.dropFix

import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class DropEntry(val onDrop: (entity: Entity, looting: Int) -> Array<ItemStack?>) {
	companion object {
		/* general drop entries */

		fun nothing(): DropEntry {
			return DropEntry { _, _ -> emptyArray() }
		}

		fun item(entityMaterial: Material): DropEntry {
			return DropEntry { entity, looting ->
				arrayOf(ItemStack(entityMaterial, 1))
			}
		}

		fun item(entityMaterial: Material, lootAmount: Int): DropEntry {
			return DropEntry { entity, looting ->
				arrayOf(ItemStack(entityMaterial, lootAmount))
			}
		}

		fun item(entityMaterial: Material, lootAmount: (looting: Int) -> Int): DropEntry {
			return DropEntry { entity, looting ->
				arrayOf(ItemStack(entityMaterial, lootAmount(looting)))
			}
		}

		fun item(entityMaterial: (entity: Entity) -> Material?): DropEntry {
			return DropEntry { entity, _ ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material, 1))
			}
		}

		fun item(entityMaterial: (entity: Entity) -> Material?, lootAmount: Int): DropEntry {
			return DropEntry { entity, _ ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material, lootAmount))
			}
		}

		fun item(entityMaterial: (entity: Entity) -> Material?, lootAmount: (looting: Int) -> Int): DropEntry {
			return DropEntry { entity, looting ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material, lootAmount(looting)))
			}
		}

		fun stackItem(createStack: (pair: Pair<Entity, Int>) -> ItemStack?): DropEntry {
			return DropEntry { entity, looting -> arrayOf(createStack(Pair(entity, looting))) }
		}

		fun arrayItem(createArray: (pair: Pair<Entity, Int>) -> Array<ItemStack?>): DropEntry {
			return DropEntry { entity, looting -> createArray(Pair(entity, looting)) }
		}

		/* specialized drop entries */

		fun potion(potionType: PotionType): DropEntry {
			val item = ItemStack(Material.POTION)

			val meta = item.itemMeta as PotionMeta
			meta.basePotionData = PotionData(potionType, false, false)
			item.itemMeta = meta

			return DropEntry { _, _ -> arrayOf(item) }
		}

		fun slownessArrow(): DropEntry {
			return DropEntry { _, looting ->
				val stack = ItemStack(Material.TIPPED_ARROW, looting + 1)

				val meta = stack.itemMeta as PotionMeta
				meta.basePotionData = PotionData(PotionType.SLOWNESS, false, false)
				stack.itemMeta = meta

				arrayOf(stack)
			}
		}

		fun horseInventory(): DropEntry {
			return DropEntry { entity, _ -> (entity as AbstractHorse).inventory.contents as Array<ItemStack?> }
		}

		fun mobInventory(): DropEntry {
			return DropEntry { entity, _ ->
				(entity as LivingEntity)
				val equipment = entity.equipment ?: return@DropEntry emptyArray()

				arrayOf(
					equipment.helmet?.clone(),
					equipment.chestplate?.clone(),
					equipment.leggings?.clone(),
					equipment.boots?.clone(),
					equipment.itemInMainHand?.clone(),
					equipment.itemInOffHand?.clone(),
				)
			}
		}

		fun mobArmor(): DropEntry {
			return DropEntry { entity, _ ->
				(entity as LivingEntity)
				val equipment = entity.equipment ?: return@DropEntry emptyArray()

				arrayOf(
					equipment.helmet?.clone(),
					equipment.chestplate?.clone(),
					equipment.leggings?.clone(),
					equipment.boots?.clone()
				)
			}
		}

		fun endermanHolding(): DropEntry {
			return DropEntry { entity, _ ->
				(entity as Enderman)
				val material = entity.carriedBlock?.material

				arrayOf(if (material == null) material else ItemStack(material))
			}
		}

		/* helper function generators */

		fun amount(amount: Int): (Int) -> Int {
			return { amount }
		}

		fun lootItem(looting: Int): Int {
			return looting + 1
		}

		fun lootMulti(base: Int): (Int) -> Int {
			return { it + base }
		}

		fun material(material: Material): (Entity) -> Material? {
			return { material }
		}

		fun onFire(unCooked: Material, cooked: Material): (Entity) -> Material? {
			return { entity ->
				if (entity.fireTicks > -1) cooked else unCooked
			}
		}

		fun isSize(material: Material, size: Int): (Entity) -> Material? {
			return { entity ->
				entity as MagmaCube
				if (entity.size == size) material else null
			}
		}

		fun saddle(entity: Entity): Material? {
			return if ((entity as Steerable).hasSaddle()) Material.SADDLE else null
		}

		fun noBaby(entityMaterial: (Entity) -> Material?): (Entity) -> Material? {
			return { entity ->
				if ((entity as Ageable).isAdult) entityMaterial(entity) else null
			}
		}
	}
}
