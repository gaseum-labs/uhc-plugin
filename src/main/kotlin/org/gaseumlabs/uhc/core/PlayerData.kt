package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.customSpawning.CustomSpawningType
import org.gaseumlabs.uhc.customSpawning.SpawningPlayerData
import org.gaseumlabs.uhc.gui.gui.LoadoutGui
import org.gaseumlabs.uhc.gui.gui.QueueGUI
import org.gaseumlabs.uhc.lobbyPvp.*
import org.gaseumlabs.uhc.chc.CHC
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.util.PropertyGroup
import java.util.*
import kotlin.collections.ArrayList

class PlayerData(val uuid: UUID) {
	/* the main 3 */
	var participating = false
	var alive = false
	var optingOut = false

	/* lobby pvp stuff */

	private val queueGroup = PropertyGroup { GuiManager.update(QueueGUI::class) }
	private val slotGroups = Array(Loadouts.NUM_SLOTS) { i -> PropertyGroup {
		GuiManager.update(LoadoutGui::class, Bukkit.getPlayer(uuid))
	} }

	var lobbyInventory = emptyArray<ItemStack?>()
	val recentPlatforms = ArrayList<UUID>()
	var lastPlayed: UUID? = null

	var loadoutSlot by queueGroup.delegate(0)
	var inLobbyPvpQueue by queueGroup.delegate(0, onChange = {
		when (it) {
			0 -> PvpQueue.remove(uuid)
			PvpQueue.TYPE_1V1 -> PvpQueue.add(uuid, PvpQueue.TYPE_1V1)
			PvpQueue.TYPE_2V2 -> PvpQueue.add(uuid, PvpQueue.TYPE_2V2)
			PvpQueue.TYPE_GAP -> PvpQueue.add(uuid, PvpQueue.TYPE_GAP)
		}
	})
	var parkourIndex by queueGroup.delegate(0)
	var slotCost0 by slotGroups[0].delegate(0)
	var slotCost1 by slotGroups[1].delegate(0)
	var slotCost2 by slotGroups[2].delegate(0)
	fun getSlotCost(i: Int) = when (i) {
		0 -> this::slotCost0
		1 -> this::slotCost1
		2 -> this::slotCost2
		else -> throw Error()
	}

	/* custom spawning */
	val spawningData = CustomSpawningType.values().map {
		SpawningPlayerData()
	}

	/* other stuff */
	class QuirkDataHolder<DataType>(var data: DataType)

	var quirkData: QuirkDataHolder<*>? = null

	var skull = ItemStack(Material.PLAYER_HEAD)

	var shouldGameMode: GameMode? = null
	var actionsQueue: Queue<(Player) -> Unit> = LinkedList()

	var lifeNo = 0

	/* enchant fix */
	var enchantEventFired = false
	var enchantCycle: Int = 0
	var shelves: Int = 0

	var offlineZombie: Zombie? = null
		set(value) {
			if (value == null) {
				val oldValue = offlineZombie
				if (oldValue != null && oldValue.isValid) {
					oldValue.remove()
					oldValue.world.unloadChunk(oldValue.chunk)
					oldValue.world.setChunkForceLoaded(oldValue.chunk.x, oldValue.chunk.z, false)
				}
			} else {
				value.world.setChunkForceLoaded(value.chunk.x, value.chunk.z, true)
				value.world.loadChunk(value.chunk)
			}
			field = value
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

	fun <DataType>setQuirkData(quirk: CHC<DataType>, data: DataType) {
		val holder = getQuirkDataHolder(quirk)
		holder.data = data
	}

	inline fun <DataType>setQuirkDataL(quirk: CHC<DataType>, set: (DataType) -> Unit) {
		val holder = getQuirkDataHolder(quirk)
		set(holder.data)
	}

	fun <DataType>getQuirkData(quirk: CHC<DataType>): DataType {
		return getQuirkDataHolder(quirk).data
	}

	fun <DataType>getQuirkDataHolder(quirk: CHC<DataType>): QuirkDataHolder<DataType> {
		var holder = quirkData
		if (holder == null) {
			holder = QuirkDataHolder(quirk.defaultData())
			quirkData = holder
		}
		return holder as QuirkDataHolder<DataType>
	}

	fun deleteQuirkData() {
		quirkData = QuirkDataHolder(null)
	}

	fun addRecentPlatform(uuid: UUID) {
		val oldIndex = recentPlatforms.indexOf(uuid)
		if (oldIndex != -1) recentPlatforms.removeAt(oldIndex)
		recentPlatforms.add(0, uuid)
		while (recentPlatforms.size > 5) {
			recentPlatforms.removeLast()
		}
	}

	companion object {
		/* THE player data list */
		var playerDataList = HashMap<UUID, PlayerData>()
			private set

		fun prune() {
			playerDataList = playerDataList.filter { (uuid, _) ->
				Bukkit.getPlayer(uuid) != null
			} as HashMap<UUID, PlayerData>
		}

		fun get(uuid: UUID) = playerDataList.getOrPut(uuid) { PlayerData(uuid) }
		fun get(player: Player) = get(player.uniqueId)
		fun get(player: OfflinePlayer) = get(player.uniqueId)
		fun get(player: HumanEntity) = get(player.uniqueId)
	}
}
