package org.gaseumlabs.uhc.lobbyPvp.arena

import org.gaseumlabs.uhc.core.Lobby
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.CommandItemType
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.lobbyPvp.*
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.*

class ParkourArena(teams: ArrayList<ArrayList<UUID>>, val owner: UUID) : Arena(ArenaType.PARKOUR, teams) {
	lateinit var start: Block

	data class ParkourData(var checkpoint: Block, var timer: Int, var timerGoing: Boolean)

	private val parkourDataList = HashMap<UUID, ParkourData>()

	fun getParkourData(uuid: UUID): ParkourData {
		return parkourDataList.getOrPut(uuid) { ParkourData(start, 0, false) }
	}

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

			val arena = ParkourArena(arrayListOf(arrayListOf()), uuid)

			arena.start = world.getBlockAtKey(key)

			return arena
		}
	}

	fun enterPlayer(player: Player, forceParticipate: Boolean, teleport: Boolean) {
		Lobby.resetPlayerStats(player)

		val isOwner = player.uniqueId == owner

		if (!forceParticipate && isOwner) {
			enterBuilder(player)
		} else {
			enterParticipant(player)
		}

		CommandItemType.LOBBY_RETURN.giveItem(player.inventory)
		if (isOwner) Bukkit.getScheduler().scheduleSyncDelayedTask(org.gaseumlabs.uhc.UHCPlugin.plugin, {
			if (playerIsParticipating(player.uniqueId)) {
				CommandItemType.PARKOUR_TEST.giveItem(player.inventory, 8)
			}
		}, 40)

		if (!teams[0].contains(player.uniqueId)) teams[0].add(player.uniqueId)

		if (teleport) player.teleport(playerLocation(player))
	}

	fun playerLocation(player: Player): Location {
		val location = getParkourData(player.uniqueId).checkpoint.location.add(0.5, 1.0, 0.5)
		location.pitch = player.location.pitch
		location.yaw = player.location.yaw
		return location
	}

	private fun enterParticipant(player: Player) {
		player.gameMode = GameMode.ADVENTURE

		Bukkit.getScheduler().scheduleSyncDelayedTask(org.gaseumlabs.uhc.UHCPlugin.plugin, {
			if (playerIsParticipating(player.uniqueId)) {
				CommandItemType.PARKOUR_CHECKPOINT.giveItem(player.inventory)
				CommandItemType.PARKOUR_RESET.giveItem(player.inventory)
			}
		}, 40)

		player.showTitle(Title.title(Component.text("RUN", NamedTextColor.AQUA), Component.empty()))
	}

	private fun enterBuilder(player: Player) {
		player.gameMode = GameMode.CREATIVE

		/* give items */
		player.inventory.addItem(ItemCreator.display(Material.LAPIS_BLOCK)
			.name(Component.text("Lapis (Parkour Start)", BLUE)).create())
		player.inventory.addItem(ItemCreator.display(Material.GOLD_BLOCK)
			.name(Component.text("Gold (Checkpoint)", YELLOW))
			.create())

		player.showTitle(Title.title(Component.text("BUILD", NamedTextColor.GOLD), Component.empty()))
	}

	fun defaultStart(): Block {
		val world = WorldManager.pvpWorld
		val (centerX, centerZ) = getCenter()

		return world.getBlockAt(centerX, Util.topBlockY(world, centerX, centerZ), centerZ)
	}

	override fun customPerSecond(onlinePlayers: List<Player>): Boolean {
		return false
	}

	override fun startingPositions(teams: ArrayList<ArrayList<UUID>>): List<List<Position>> {
		val (centerX, centerZ) = getCenter()

		return teams.map { team ->
			team.map {
				Position(centerX, centerZ, 0.0f, null)
			}
		}
	}

	override fun customStartPlayer(player: Player, playerData: PlayerData) {
		enterPlayer(player, false, false)
	}

	override fun prepareArena(world: World) {
		start = defaultStart()
	}

	override fun arenaStart(onlinePlayers: List<Player>) {}

	override fun startText() = "Starting parkour in"

	override fun shutdownOnLeave() = false

	override fun customSave(): String {
		return "${start.blockKey},${owner}"
	}
}
