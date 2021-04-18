package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class Halloween(type: QuirkType) : Quirk(type) {
	var hasGottenDiamonds = false

	override fun onEnable() {
		hasGottenDiamonds = false
	}

	override fun onDisable() {}

	override fun modifyEntityDrops(entity: Entity, killer: Player?, drops: MutableList<ItemStack>): Boolean {
		if (entity is Monster) {
			val random = Math.random()

			val candy = when {
				random < 0.025 -> ItemStack(CAKE)
				random < 0.050 -> ItemStack(PUMPKIN_PIE)
				random < 0.100 -> ItemStack(HONEY_BOTTLE)
				random < 0.125 -> ItemStack(COOKIE)
				random < 0.150 -> ItemStack(SWEET_BERRIES)
				else -> null
			}

			if (candy != null) drops.add(candy)
		}

		if (entity is LivingEntity && Math.random() < 0.25) {
			entity.world.spawnEntity(entity.location, BAT, CreatureSpawnEvent.SpawnReason.CUSTOM)
		}

		return false
	}

	override val representation: ItemStack
		get() = ItemStack(PUMPKIN_PIE)

	companion object {
		fun onEntitySpawn(entity: Entity) {
			if (entity as? LivingEntity != null) {
				entity.equipment?.helmet = ItemStack(if (Math.random() < 0.5) CARVED_PUMPKIN else JACK_O_LANTERN)
				entity.equipment?.helmetDropChance = 0.25f
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

		fun jumpScare(player: Player) {
			val creeperLocation = player.location.clone()
			val direction = creeperLocation.direction
			direction.y = 0.0
			creeperLocation.add(direction.multiply(0.5))
			creeperLocation.direction = direction.multiply(-1)

			val creeper = player.world.spawnEntity(creeperLocation, CREEPER)
			creeper as Creeper
			creeper.isIgnited = true
			creeper.maxFuseTicks = 200

			Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin, {
				creeper.remove()
			}, 20)

			player.playSound(player.location, Sound.ENTITY_GHAST_SCREAM, 2.0f, 1.0f)
			player.playSound(player.location, Sound.ENTITY_GHAST_HURT, 2.0f, 1.0f)
			player.playSound(player.location, Sound.ENTITY_GHAST_DEATH, 2.0f, 1.0f)
			player.playSound(player.location, Sound.ENTITY_GHAST_SHOOT, 2.0f, 1.0f)
			player.playSound(player.location, Sound.ENTITY_GHAST_WARN, 2.0f, 1.0f)

			SchedulerUtil.nextTick {
				player.playSound(player.location, Sound.ENTITY_GHAST_SCREAM, 2.0f, 1.0f)
				player.playSound(player.location, Sound.ENTITY_GHAST_HURT, 2.0f, 1.0f)
				player.playSound(player.location, Sound.ENTITY_GHAST_DEATH, 2.0f, 1.0f)
				player.playSound(player.location, Sound.ENTITY_GHAST_SHOOT, 2.0f, 1.0f)
				player.playSound(player.location, Sound.ENTITY_GHAST_WARN, 2.0f, 1.0f)
			}
		}
	}
}