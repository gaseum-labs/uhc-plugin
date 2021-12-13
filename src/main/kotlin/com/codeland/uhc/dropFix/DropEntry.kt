package com.codeland.uhc.dropFix

import com.codeland.uhc.gui.ItemCreator
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

data class RareEntry(val size: Int, val dropEntry: DropEntry)

class DropEntry(val onDrop: (entity: Entity, looting: Int, cycle: Int) -> Array<ItemStack?>) {
	companion object {
		/* templates for drop entries */

		/* they take in the entity */
		/* the looting level on the killing weapon */
		/* and the total number of this kind of entity killed */

		/* output potentially multiple items to be dropped */

		fun nothing(): DropEntry {
			return DropEntry { _, _, _ -> emptyArray() }
		}

		fun item(material: Material): DropEntry {
			return DropEntry { _, _, _ ->
				arrayOf(ItemStack(material, 1))
			}
		}

		fun item(material: Material, amount: Int): DropEntry {
			return DropEntry { _, _, _ ->
				arrayOf(ItemStack(material, amount))
			}
		}

		fun item(material: Material, lootAmount: (Int, Int) -> Int): DropEntry {
			return DropEntry { _, looting, cycle ->
				arrayOf(ItemStack(material, lootAmount(looting, cycle)))
			}
		}

		fun item(entityMaterial: (Entity) -> Material?): DropEntry {
			return DropEntry { entity, _, _ ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material, 1))
			}
		}

		fun item(entityMaterial: (Entity) -> Material?, amount: Int): DropEntry {
			return DropEntry { entity, _, _ ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material, amount))
			}
		}

		fun item(entityMaterial: (Entity) -> Material?, lootAmount: (Int, Int) -> Int): DropEntry {
			return DropEntry { entity, looting, cycle ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material, lootAmount(looting, cycle)))
			}
		}

		/* specialized drop entries */

		fun potion(potionType: PotionType) = DropEntry { _, _, _ ->
			arrayOf(
				ItemCreator.fromType(Material.POTION, false)
					.customMeta<PotionMeta> { it.basePotionData = PotionData(potionType, false, false) }
					.create()
			)
		}

		fun slownessArrow() = DropEntry { _, looting, _ ->
			val stack = ItemStack(Material.TIPPED_ARROW, looting + 1)

			val meta = stack.itemMeta as PotionMeta
			meta.basePotionData = PotionData(PotionType.SLOWNESS, false, false)
			stack.itemMeta = meta

			arrayOf(stack)
		}

		fun horseInventory() =
			DropEntry { entity, _, _ -> (entity as AbstractHorse).inventory.contents as Array<ItemStack?> }

		fun mobInventory() = DropEntry { entity, _, _ ->
			entity as LivingEntity
			val equipment = entity.equipment ?: return@DropEntry emptyArray()

			arrayOf(
				equipment.helmet?.clone(),
				equipment.chestplate?.clone(),
				equipment.leggings?.clone(),
				equipment.boots?.clone(),
				equipment.itemInMainHand.clone(),
				equipment.itemInOffHand.clone(),
			)
		}

		fun mobArmor() = DropEntry { entity, _, _ ->
			(entity as LivingEntity)
			val equipment = entity.equipment ?: return@DropEntry emptyArray()

			arrayOf(
				equipment.helmet?.clone(),
				equipment.chestplate?.clone(),
				equipment.leggings?.clone(),
				equipment.boots?.clone()
			)
		}

		fun endermanHolding() = DropEntry { entity, _, _ ->
			arrayOf(ItemStack((entity as Enderman).carriedBlock?.material ?: return@DropEntry emptyArray()))
		}

		/* helper function generators */

		const val LOOT_UP_TO_CYCLE = 12

		fun amount(amount: Int): (Int) -> Int {
			return { amount }
		}

		fun lootItem(looting: Int, cycle: Int): Int {
			return looting + 1
		}

		fun lootUpTo(looting: Int, cycle: Int): Int {
			return (cycle % (looting + 1)) + 1
		}

		fun lootMulti(base: Int): (Int, Int) -> Int {
			return { looting, _ -> looting + base }
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
