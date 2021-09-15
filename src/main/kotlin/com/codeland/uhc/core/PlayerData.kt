package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.customSpawning.CustomSpawningType
import com.codeland.uhc.event.Enchant
import com.codeland.uhc.gui.GuiManager
import com.codeland.uhc.gui.gui.LoadoutGui
import com.codeland.uhc.gui.gui.LobbyPvpGui
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.ArenaType
import com.codeland.uhc.lobbyPvp.Loadouts
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.lobbyPvp.PvpQueue
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.max

class PlayerData(val uuid: UUID) {
	/* the main 3 */
	var participating = false
	var alive = false
	var optingOut = false

	/* lobby pvp stuff */

	var lobbyInventory = emptyArray<ItemStack>()
	var lastPlayed: UUID? = null
	var loadoutSlot = UHCProperty(0)
	var inLobbyPvpQueue = UHCProperty(0) { set ->
		when (set) {
			0 -> PvpQueue.remove(uuid)
			PvpArena.TYPE_1V1 -> PvpQueue.add(uuid, PvpArena.TYPE_1V1)
			PvpArena.TYPE_2V2 -> PvpQueue.add(uuid, PvpArena.TYPE_2V2)
		}
		set
	}
	var slotCosts = Array(Loadouts.NUM_SLOTS) { i ->
		UHCProperty(0)
	}
	val parkourIndex = UHCProperty(-1)
	var lobbyPvpGui = LobbyPvpGui(this)
	val slotGuis = Array(Loadouts.NUM_SLOTS) { i -> LoadoutGui(this, i) }

	val guis = arrayOf(lobbyPvpGui, *slotGuis)

	init {
		parkourIndex.set(ArenaManager.typeList<ParkourArena>(ArenaType.PARKOUR).lastIndex)
	}

	/* custom spawning */
	val spawningData = CustomSpawningType.values().map {
		CustomSpawningType.SpawningPlayerData()
	}

	/* other stuff */
	class QuirkDataHolder(var applied: Boolean, var data: Any)
	var quirkDataList = HashMap<QuirkType, QuirkDataHolder>()

	var skull = ItemStack(Material.PLAYER_HEAD)

	var loadingTip = 0

	var actionsQueue: Queue<(Player) -> Unit> = LinkedList()

	var lifeNo = 0

	/* enchant fix */
	var enchantCycle: Int = 0
	var storedOffers: Array<EnchantmentOffer?> = arrayOf(null, null, null)
	var storedType: Enchant.EnchantType = Enchant.EnchantType.ARMOR
	var storedShelves: Int = 0
	var storedHash: Int = 0

	var offlineZombie: Zombie? = null
	set(value) {
		field = if (value == null) {
			val oldValue = offlineZombie

			if (oldValue != null) {
				oldValue.remove()
				oldValue.world.unloadChunk(oldValue.chunk)
				oldValue.world.setChunkForceLoaded(oldValue.chunk.x, oldValue.chunk.z, false)
			}

			null
		} else {
			value.world.setChunkForceLoaded(value.chunk.x, value.chunk.z, true)
			value.world.loadChunk(value.chunk)

			value
		}
	}

	/* begin functions */

	fun current() = participating && alive
	fun undead() = participating && !alive

	fun setSkull(player: Player) {
		skull = ItemStack(Material.PLAYER_HEAD)
		val meta = skull.itemMeta as SkullMeta
		meta.owningPlayer = player
		skull.itemMeta = meta
	}

	private fun internalCreateZombie(location: Location, uuid: UUID, name: Component, inventory: Array<ItemStack>, experience: Int): Zombie {
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

		zombie.equipment?.helmet = skull
		zombie.equipment?.helmetDropChance = 0.0f
		zombie.equipment?.chestplateDropChance = 0.0f
		zombie.equipment?.leggingsDropChance = 0.0f
		zombie.equipment?.bootsDropChance = 0.0f
		zombie.equipment?.itemInMainHandDropChance = 0.0f
		zombie.equipment?.itemInOffHandDropChance = 0.0f

		/* small zombie when disconnecting while crouching */
		if (!location.world.getBlockAt(location).getRelative(BlockFace.UP).isPassable) zombie.setBaby()

		return zombie
	}

	fun getZombieInventory(): Array<ItemStack?>? {
		val zombie = offlineZombie ?: return null

		val inventoryMeta = zombie.getMetadata(INVENTORY_TAG)
		if (inventoryMeta.isEmpty()) return null
		return inventoryMeta[0].value() as Array<ItemStack?>
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
		}

		val team = TeamData.playersTeam(player.uniqueId)

		val zombie = internalCreateZombie(
			player.location,
			player.uniqueId,
			team?.apply(player.name) ?: Component.text(player.name),
			clonedInventory as Array<ItemStack>,
			player.totalExperience
		)

		zombie.health = player.health
		zombie.fireTicks = player.fireTicks
		zombie.addPotionEffects(player.activePotionEffects)

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
		val team = TeamData.playersTeam(uuid)
		val playerName = Bukkit.getOfflinePlayer(uuid).name ?: "NULL"

		return internalCreateZombie(location, uuid, team?.apply(playerName) ?: Component.text(playerName), emptyArray(), 0)
	}

	fun replaceZombieWithPlayer(player: Player) {
		val zombie = offlineZombie ?: return

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
	}

	fun setQuirkData(quirkType: QuirkType, data: Any): QuirkDataHolder {
		val dataHolder = quirkDataList.getOrPut(quirkType) { QuirkDataHolder(false, 0) }

		dataHolder.data = data

		return dataHolder
	}

	fun getQuirkData(quirk: Quirk): Any {
		return quirkDataList.getOrPut(quirk.type) { QuirkDataHolder(false, quirk.defaultData()) }.data
	}

	companion object {
		const val INVENTORY_TAG = "_UHC_Zombie_inv"
		const val XP_TAG = "_UHC_Zombie_xp"
		const val UUID_TAG = "_UHC_Zombie_uuid"

		/* THE player data list */
		var playerDataList = HashMap<UUID, PlayerData>()
		private set

		fun prune() {
			playerDataList = playerDataList.filter { (uuid, _) ->
				Bukkit.getPlayer(uuid) != null
			} as HashMap<UUID, PlayerData>
		}

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

		fun zombieBorderTick(currentTick: Int, game: Game) {
			if (currentTick % 20 == 0) {
				val borderWorld = game.world
				val borderRadius = borderWorld.worldBorder.size / 2.0

				playerDataList.forEach { (_, playerData) ->
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

		/* access operations for player data list */

		fun isParticipating(uuid: UUID): Boolean {
			return getPlayerData(uuid).participating
		}

		fun isAlive(uuid: UUID): Boolean {
			return getPlayerData(uuid).alive
		}

		fun isOptingOut(uuid: UUID): Boolean {
			return getPlayerData(uuid).optingOut
		}

		fun isCurrent(uuid: UUID): Boolean {
			val playerData = getPlayerData(uuid)
			return playerData.participating && playerData.alive
		}

		fun isUndead(uuid: UUID): Boolean {
			val playerData = getPlayerData(uuid)
			return playerData.participating && !playerData.alive
		}

		/* setter operations for player data list */

		fun setAlive(uuid: UUID, alive: Boolean) {
			getPlayerData(uuid).alive = alive
		}

		fun setParticipating(uuid: UUID, participating: Boolean) {
			getPlayerData(uuid).participating = participating
		}

		fun setOptOut(uuid: UUID, optOut: Boolean) {
			getPlayerData(uuid).optingOut = optOut
		}

		fun getPlayerData(uuid: UUID): PlayerData {
			return playerDataList.getOrPut(uuid) { PlayerData(uuid) }
		}

		/* quirkData getters */

		fun getQuirkDataHolder(uuid: UUID, type: QuirkType, game: Game): QuirkDataHolder {
			return getQuirkDataHolder(getPlayerData(uuid), type, game)
		}

		fun getQuirkDataHolder(playerData: PlayerData, type: QuirkType, game: Game): QuirkDataHolder {
			return playerData.quirkDataList.getOrPut(type) { QuirkDataHolder(false, game.getQuirk<Quirk>(type)?.defaultData() ?: 0) }
		}

		fun <DataType> getQuirkData(uuid: UUID, type: QuirkType, game: Game): DataType {
			return getQuirkDataHolder(uuid, type, game).data as DataType
		}

		fun <DataType> getQuirkData(playerData: PlayerData, type: QuirkType, game: Game): DataType {
			return getQuirkDataHolder(playerData, type, game).data as DataType
		}
	}
}
