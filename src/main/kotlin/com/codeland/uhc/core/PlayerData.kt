package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max

class PlayerData(var participating: Boolean, var alive: Boolean, var optingOut: Boolean) {
	var quirkData = HashMap<QuirkType, Any>()

	var skull = ItemStack(Material.PLAYER_HEAD)

	var actionsQueue: Queue<(Player) -> Unit> = LinkedList()

	var offlineZombie: Zombie? = null
	set(value: Zombie?) {
		field = if (value == null) {
			val oldValue = offlineZombie
			if (oldValue != null) {
				oldValue.world.unloadChunk(oldValue.chunk)
				oldValue.world.setChunkForceLoaded(oldValue.chunk.x, oldValue.chunk.z, false)
			}

			null
		} else {
			value.world.setChunkForceLoaded(value.chunk.x, value.chunk.z, true)
			value.world.loadChunk(value.chunk)

			Util.log("is chunk loaded?: ${value.chunk.isForceLoaded}")

			value
		}
	}

	fun setSkull(player: Player) {
		skull = ItemStack(Material.PLAYER_HEAD)
		val meta = skull.itemMeta as SkullMeta
		meta.owningPlayer = player
		skull.itemMeta = meta
	}

	private fun internalCreateZombie(location: Location, uuid: UUID, name: String, inventory: Array<ItemStack>, experience: Int): Zombie {
		val zombie = location.world.spawn(location, Zombie::class.java)

		zombie.setMetadata(INVENTORY_TAG, FixedMetadataValue(UHCPlugin.plugin, inventory))
		zombie.setMetadata(XP_TAG, FixedMetadataValue(UHCPlugin.plugin, experience))
		zombie.setMetadata(UUID_TAG, FixedMetadataValue(UHCPlugin.plugin, uuid))

		zombie.customName = name
		zombie.setAI(false)
		zombie.canPickupItems = false
		zombie.setShouldBurnInDay(false)
		zombie.conversionTime = Int.MIN_VALUE

		zombie.isBaby = !location.world.getBlockAt(location).getRelative(BlockFace.UP).isPassable

		return zombie
	}

	/**
	 * @param player the online player right before they log out
	 * @return a zombie that represents the player when offline
	 *
	 * place this into the offlineZombie field in PlayerData
	 */
	fun createZombie(player: Player): Zombie {
		val clonedInventory = Array(player.inventory.contents.size) { i ->
			player.inventory.contents[i]?.clone()
		} as Array<ItemStack> // do not remove cast

		val zombie = internalCreateZombie(player.location, player.uniqueId, player.playerListName, clonedInventory, player.totalExperience)

		zombie.health = player.health
		zombie.fireTicks = player.fireTicks
		zombie.addPotionEffects(player.activePotionEffects)

		zombie.equipment?.helmetDropChance = 0.0f
		zombie.equipment?.chestplateDropChance = 0.0f
		zombie.equipment?.leggingsDropChance = 0.0f
		zombie.equipment?.bootsDropChance = 0.0f
		zombie.equipment?.itemInMainHandDropChance = 0.0f
		zombie.equipment?.itemInOffHandDropChance = 0.0f

		zombie.equipment?.helmet = skull
		zombie.equipment?.chestplate = player.inventory.chestplate?.clone()
		zombie.equipment?.leggings = player.inventory.leggings?.clone()
		zombie.equipment?.boots = player.inventory.boots?.clone()
		zombie.equipment?.setItemInMainHand(player.inventory.itemInMainHand.clone())
		zombie.equipment?.setItemInOffHand(player.inventory.itemInOffHand.clone())

		return zombie
	}

	/**
	 * @return a zombie that represents the player when offline with no inventory
	 *
	 * place this into the offlineZombie field in PlayerData
	 */
	fun createDefaultZombie(uuid: UUID, location: Location): Zombie {
		return internalCreateZombie(location, uuid, Bukkit.getOfflinePlayer(uuid).name ?: "NULL", emptyArray(), 0)
	}

	fun replaceZombieWithPlayer(player: Player) {
		Util.log("REMOVING ZOMBIEE")
		val zombie = offlineZombie ?: return

		Util.log("zombie was not null")

		val (inventory, experience) = getZombieData(zombie)

		player.teleport(zombie.location)
		player.inventory.contents = inventory
		player.totalExperience = experience
		player.fireTicks = zombie.fireTicks
		player.health = zombie.health

		player.activePotionEffects.clear()
		player.addPotionEffects(zombie.activePotionEffects)

		/* load chunk */
		zombie.world.getChunkAt(zombie.location)

		/* no more offline zombie */
		zombie.remove()
		offlineZombie = null

		Util.log("offline zombie is now ${offlineZombie}")
	}

	companion object {
		val INVENTORY_TAG = "_UHC_Zombie_inv"
		val XP_TAG = "_UHC_Zombie_xp"
		val UUID_TAG = "_UHC_Zombie_uuid"

		/**
		 * @return -1 experience if this is not a valid offline zombie
		 */
		fun getZombieData(entity: Entity): Triple<Array<ItemStack>, Int, UUID> {
			val badReturn = Triple(emptyArray<ItemStack>(), -1, UUID.randomUUID())

			if (entity.type != EntityType.ZOMBIE) return badReturn

			val inventoryMeta = entity.getMetadata(INVENTORY_TAG)
			if (inventoryMeta.isEmpty()) return badReturn
			val inventory = inventoryMeta[0].value() as Array<ItemStack>

			val xpMeta = entity.getMetadata(XP_TAG)
			if (xpMeta.isEmpty()) return badReturn
			val xp = xpMeta[0].asInt()

			val uuidMeta = entity.getMetadata(UUID_TAG)
			if (uuidMeta.isEmpty()) return badReturn
			val uuid = uuidMeta[0].value() as UUID

			return Triple(inventory, xp, uuid)
		}

		fun isZombie(entity: Entity): Boolean {
			if (entity.type != EntityType.ZOMBIE) return false
			if (entity.getMetadata(INVENTORY_TAG).size == 0) return false

			return true
		}

		var zombieBorderTaskID = 0

		fun startZombieBorderTask() {
			Bukkit.getScheduler().cancelTask(zombieBorderTaskID)
			zombieBorderTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
				val borderRadius = Bukkit.getWorlds()[0].worldBorder.size / 2.0

				GameRunner.uhc.playerDataList.forEach { (uuid, playerData) ->
					val zombie = playerData.offlineZombie

					if (zombie != null && zombie.world === Bukkit.getWorlds()[0]) {
						val x = abs(zombie.location.x)
						val z = abs(zombie.location.z)

						val dist = max(x - borderRadius, z - borderRadius) - 5.0
						if (dist > 0) zombie.damage(dist)
					}
				}
			}, 0, 20)
		}

		fun endZombieBorderTask() {
			Bukkit.getScheduler().cancelTask(zombieBorderTaskID)
		}
	}
}
