package com.codeland.uhc.lobbyPvp.arena

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.lobbyPvp.Arena
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.ArenaType
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class ParkourArena(teams: ArrayList<ArrayList<UUID>>): Arena(ArenaType.PARKOUR, teams) {
	lateinit var start: Block

	val checkpoints = HashMap<UUID, Block>()
	var owner: UUID = if (teams[0].isEmpty()) UUID(0, 0) else teams[0][0]

	companion object {
		fun playersParkour(uuid: UUID) = ArenaManager.ongoing.find { arena ->
			arena is ParkourArena && arena.owner == uuid
		} as ParkourArena?

		fun load(data: String, world: World): Arena? {
			val parts = data.split(',')
			if (parts.size != 2) return null

			val key = parts[0].toLongOrNull() ?: return null
			val uuid = try {
				UUID.fromString(parts[1])
			} catch (ex: Exception) {
				null
			} ?: return null

			val arena = ParkourArena(arrayListOf(arrayListOf()))

			arena.start = world.getBlockAtKey(key)
			arena.owner = uuid

			return arena
		}
	}

	/* override */

	fun enterPlayer(player: Player, forceParticipate: Boolean) {
		Lobby.resetPlayerStats(player)

		val isOwner = player.uniqueId == owner

		if (!forceParticipate && isOwner) {
			enterBuilder(player)
		} else {
			enterParticipant(player)
		}

		CommandItemType.giveItem(CommandItemType.LOBBY_RETURN, player.inventory)
		if (isOwner) Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin, {
			if (playerIsParticipating(player.uniqueId)) {
				CommandItemType.giveItem(CommandItemType.PARKOUR_TEST, player.inventory, 8)
			}
		}, 40)

		if (!teams[0].contains(player.uniqueId)) teams[0].add(player.uniqueId)

		player.teleport(playerLocation(player.uniqueId))
	}

	fun playerLocation(uuid: UUID): Location {
		return (checkpoints[uuid] ?: start).location.add(0.5, 1.0, 0.5)
	}

	private fun enterParticipant(player: Player) {
		player.gameMode = GameMode.ADVENTURE

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin, {
			if (playerIsParticipating(player.uniqueId)) {
				CommandItemType.giveItem(CommandItemType.PARKOUR_CHECKPOINT, player.inventory, 1)
			}
		}, 40)

		player.sendTitle("${ChatColor.AQUA}RUN", "", 0, 20, 10)
	}

	private fun enterBuilder(player: Player) {
		player.gameMode = GameMode.CREATIVE

		/* give items */
		player.inventory.addItem(GuiItem.name(ItemStack(Material.LAPIS_BLOCK), "${ChatColor.BLUE}Lapis (Parkour Start)"))
		player.inventory.addItem(GuiItem.name(ItemStack(Material.GOLD_BLOCK), "${ChatColor.YELLOW}Gold (Checkpoint)"))

		player.sendTitle("${ChatColor.GOLD}BUILD", "", 0, 20, 10)
	}

	fun defaultStart(): Block {
		val world = WorldManager.getPVPWorld()
		val (centerX, centerZ) = getCenter()

		return world.getBlockAt(centerX, Util.topBlockY(world, centerX, centerZ), centerZ)
	}

	override fun customPerSecond(): Boolean {
		return false
	}

	override fun startingPositions(teams: ArrayList<ArrayList<UUID>>): List<List<Position>> {
		val (centerX, centerZ) = getCenter()

		return teams.map { team -> team.map {
			Position(centerX, centerZ, 0.0f)
		}}
	}

	override fun customStartPlayer(player: Player, playerData: PlayerData) {
		enterPlayer(player, false)
	}

	override fun prepareArena(world: World) {
		start = defaultStart()
	}

	override fun startText() = "Starting parkour in"

	override fun shutdownOnLeave() = false

	override fun customSave(): String {
		return "${start.blockKey},${owner}"
	}
}
