package com.codeland.uhc.dropFix

import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Steerable
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack

class DropEntry(val onDrop: (looting: Int, entity: Entity) -> ItemStack?) {
	companion object {
		fun nothing(): DropEntry {
			return DropEntry { _, _ -> null }
		}

		fun item(material: Material): DropEntry {
			return DropEntry { _, _ -> ItemStack(material) }
		}

		fun multi(material: Material, amount: Int): DropEntry {
			return DropEntry { _, _ -> ItemStack(material, amount) }
		}

		fun loot(material: Material, lootAmount: (looting: Int) -> Int): DropEntry {
			return DropEntry { looting, _ -> ItemStack(material, lootAmount(looting)) }
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

				if (material == null) null
				else ItemStack(material, lootAmount(looting))
			}
		}

		fun entity(entityMaterial: (entity: Entity) -> Material?): DropEntry {
			return DropEntry { _, entity ->
				val material = entityMaterial(entity)

				if (material == null) null
				else ItemStack(material)
			}
		}

		fun onFire(unCooked: Material, cooked: Material): (Entity) -> Material? {
			return { entity ->
				Util.log("firetick: ${entity.fireTicks}")
				if (entity.fireTicks == -1) unCooked else cooked
			}
		}

		fun saddle(entity: Entity): Material? {
			return if ((entity as Steerable).hasSaddle()) Material.SADDLE else null
		}
	}
}