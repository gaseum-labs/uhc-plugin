package com.codeland.uhc.dropFix

import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class DropEntry(val onDrop: (looting: Int, entity: Entity) -> Array<ItemStack?>) {
	companion object {
		fun nothing(): DropEntry {
			return DropEntry { _, _ -> emptyArray() }
		}

		fun item(material: Material): DropEntry {
			return DropEntry { _, _ -> arrayOf(ItemStack(material)) }
		}

		fun potion(potionType: PotionType): DropEntry {
			val item = ItemStack(Material.POTION)

			val meta = item.itemMeta as PotionMeta
			meta.basePotionData = PotionData(potionType, false, false)
			item.itemMeta = meta

			return DropEntry { _, _ -> arrayOf(item) }
		}

		fun multi(material: Material, amount: Int): DropEntry {
			return DropEntry { _, _ -> arrayOf(ItemStack(material, amount)) }
		}

		fun loot(material: Material, lootAmount: (looting: Int) -> Int): DropEntry {
			return DropEntry { looting, _ -> arrayOf(ItemStack(material, lootAmount(looting))) }
		}

		fun lootItem(looting: Int): Int {
			return looting + 1
		}

		fun lootMulti(base: Int): (Int) -> Int {
			return { looting -> looting + base }
		}

		fun lootEntity(entityMaterial: (entity: Entity) -> Material?, lootAmount: (looting: Int) -> Int): DropEntry {
			return DropEntry { looting, entity ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material, lootAmount(looting)))
			}
		}

		fun entity(entityMaterial: (entity: Entity) -> Material?): DropEntry {
			return DropEntry { _, entity ->
				val material = entityMaterial(entity)

				if (material == null) emptyArray()
				else arrayOf(ItemStack(material))
			}
		}

		fun onFire(unCooked: Material, cooked: Material): (Entity) -> Material? {
			return { entity ->
				if (entity.fireTicks == -1) unCooked else cooked
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

		fun horseInventory(): DropEntry {
			return DropEntry { _, entity -> (entity as AbstractHorse).inventory.contents as Array<ItemStack?> }
		}

		fun noBaby(entityMaterial: (Entity) -> Material?): (Entity) -> Material? {
			return { entity ->
				if ((entity as Ageable).isAdult) entityMaterial(entity) else null
			}
		}

		fun hasTrident(): (Entity) -> Material? {
			return { entity ->
				entity as Drowned

				if (
					entity.equipment?.itemInMainHand?.type == Material.TRIDENT ||
					entity.equipment?.itemInOffHand?.type == Material.TRIDENT
				) Material.TRIDENT else null
			}
		}
	}
}