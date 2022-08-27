package org.gaseumlabs.uhc.core

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.gaseumlabs.uhc.UHCPlugin
import java.util.*
import kotlin.math.abs
import kotlin.math.max

object OfflineZombie {
	const val INVENTORY_TAG = "_UHC_Zombie_inv"
	const val XP_TAG = "_UHC_Zombie_xp"
	const val UUID_TAG = "_UHC_Zombie_uuid"

	private fun internalCreateZombie(
		location: Location,
		uuid: UUID,
		name: Component,
		skull: ItemStack,
		inventory: Array<ItemStack?>,
		experience: Int,
	): Zombie {
		val zombie = location.world.spawn(location, Zombie::class.java)

		zombie.setMetadata(INVENTORY_TAG, FixedMetadataValue(UHCPlugin.plugin, inventory))
		zombie.setMetadata(XP_TAG, FixedMetadataValue(UHCPlugin.plugin, experience))
		zombie.setMetadata(UUID_TAG, FixedMetadataValue(UHCPlugin.plugin, uuid))

		zombie.customName(name)
		zombie.setAI(false)
		zombie.canPickupItems = false
		zombie.setShouldBurnInDay(false)
		zombie.conversionTime = -1
		zombie.removeWhenFarAway = false
		zombie.equipment.helmet = skull

		/* small zombie when disconnecting while crouching */
		if (!location.world.getBlockAt(location).getRelative(BlockFace.UP).isPassable) zombie.setBaby()

		return zombie
	}

	fun getZombieInventory(offlineZombie: Zombie): Array<ItemStack?>? {
		val inventoryMeta = offlineZombie.getMetadata(INVENTORY_TAG)
		if (inventoryMeta.isEmpty()) return null
		return inventoryMeta[0].value() as Array<ItemStack?>
	}

	/**
	 * @param player the online player right before they log out
	 * @return a zombie that represents the player when offline
	 *
	 * place this into the offlineZombie field in PlayerData
	 */
	fun createZombie(player: Player, playerData: PlayerData) {
		val inventoryContents = player.inventory.contents

		val clonedInventory = inventoryContents.clone()

		val team = UHC.getTeams().playersTeam(player.uniqueId)

		val zombie = internalCreateZombie(
			player.location,
			player.uniqueId,
			team?.apply(player.name) ?: Component.text(player.name),
			playerData.skull,
			clonedInventory,
			player.totalExperience
		)

		zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 20.0
		zombie.health = player.health
		zombie.fireTicks = player.fireTicks
		zombie.addPotionEffects(player.activePotionEffects)

		zombie.equipment.chestplate = player.inventory.chestplate?.clone()
		zombie.equipment.leggings = player.inventory.leggings?.clone()
		zombie.equipment.boots = player.inventory.boots?.clone()
		zombie.equipment.setItemInMainHand(player.inventory.itemInMainHand.clone())
		zombie.equipment.setItemInOffHand(player.inventory.itemInOffHand.clone())

		playerData.offlineZombie = zombie
	}

	fun createZombie(player: Player) = createZombie(player, PlayerData.get(player))

	/**
	 * @return a zombie that represents the player when offline with no inventory
	 *
	 * place this into the offlineZombie field in PlayerData
	 */
	fun createDefaultZombie(uuid: UUID, location: Location): Zombie {
		val team = UHC.getTeams().playersTeam(uuid)
		val playerName = Bukkit.getOfflinePlayer(uuid).name ?: "NULL"

		return internalCreateZombie(
			location,
			uuid,
			team?.apply(playerName) ?: Component.text(playerName),
			PlayerData.get(uuid).skull,
			emptyArray(),
			0
		)
	}

	fun replaceZombieWithPlayer(player: Player) {
		val playerData = PlayerData.get(player.uniqueId)
		val zombie = playerData.offlineZombie ?: return
		val (inventory, experience) = getZombieData(zombie) ?: return

		player.teleport(zombie.location)
		player.inventory.contents = inventory
		player.totalExperience = experience
		player.fireTicks = zombie.fireTicks
		player.health = zombie.health
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 20.0

		player.activePotionEffects.clear()
		player.addPotionEffects(zombie.activePotionEffects)

		/* load chunk */
		zombie.world.getChunkAt(zombie.location)

		/* no more offline zombie */
		playerData.offlineZombie = null
	}

	fun getZombieData(entity: Entity): Triple<Array<ItemStack?>, Int, UUID>? {
		if (entity !is Zombie) return null
		return Triple(
			entity.getMetadata(INVENTORY_TAG).firstOrNull()?.value() as Array<ItemStack?>? ?: return null,
			entity.getMetadata(XP_TAG).firstOrNull()?.asInt() ?: return null,
			entity.getMetadata(UUID_TAG).firstOrNull()?.value() as UUID? ?: return null,
		)
	}

	fun getZombieUUID(entity: Entity): UUID? {
		if (entity !is Zombie) return null
		return entity.getMetadata(UUID_TAG).firstOrNull()?.value() as UUID?
	}

	fun zombieBorderTick(currentTick: Int, game: Game) {
		if (currentTick % 20 == 0) {
			val borderWorld = game.world
			val borderRadius = borderWorld.worldBorder.size / 2.0

			PlayerData.playerDataList.forEach { (_, playerData) ->
				val zombie = playerData.offlineZombie

				if (zombie != null && zombie.world === borderWorld) {
					val x = abs(zombie.location.x)
					val z = abs(zombie.location.z)

					val dist = max(x - borderRadius, z - borderRadius)
					if (dist > 0) zombie.damage(dist)
				}
			}
		}
	}
}