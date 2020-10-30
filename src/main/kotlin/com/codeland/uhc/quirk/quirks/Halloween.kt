package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiItem.Companion.enabledName
import com.codeland.uhc.quirk.BoolProperty
import com.codeland.uhc.quirk.BoolToggle
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.*
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class Halloween(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	override fun onEnable() {}

	override fun onDisable() {

	}

	companion object {
		fun onEntitySpawn(entity: Entity) {
			if (entity as? LivingEntity != null) {
				entity.equipment?.helmet = ItemStack(if (Math.random() < 0.5) CARVED_PUMPKIN else JACK_O_LANTERN)
				entity.equipment?.helmetDropChance = 0.25f
			}
		}

		fun onEntityDeath(entity: Entity) {
			if (entity is LivingEntity && Math.random() < 0.25) {
				entity.world.spawnEntity(entity.location, BAT, CreatureSpawnEvent.SpawnReason.CUSTOM)
			}
		}

		fun addDrops(entity: Entity, drops: MutableList<ItemStack>) {
			if (entity is Monster) {
				val random = Math.random()

				when {
					random < 0.025 -> drops.add(ItemStack(CAKE))
					random < 0.050 -> drops.add(ItemStack(PUMPKIN_PIE))
					random < 0.100 -> drops.add(ItemStack(HONEY_BOTTLE))
					random < 0.125 -> drops.add(ItemStack(COOKIE))
					random < 0.150 -> drops.add(ItemStack(SWEET_BERRIES))
				}
			}
		}

		fun replaceSpawn(entity: Entity): Boolean {
			return if (entity is Monster && entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) {
				if (Math.random() < 0.01) {
					entity.world.spawnEntity(entity.location, WITCH)
					true

				} else {
					false
				}
			} else {
				false
			}
		}
	}
}