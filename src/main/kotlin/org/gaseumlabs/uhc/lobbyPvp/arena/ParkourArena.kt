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
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.util.BlockPos
import org.gaseumlabs.uhc.util.Coords
import java.util.*
import kotlin.collections.ArrayList

class ParkourArena(
	players: ArrayList<UUID>,
	coords: Coords,
	val owner: UUID,
	var startPosition: BlockPos,
) : Arena(arrayListOf(players), coords) {
	data class ParkourData(var checkpoint: BlockPos, var timer: Int, var timerGoing: Boolean)

	private val parkourDataList = HashMap<UUID, ParkourData>()

	fun getParkourData(uuid: UUID): ParkourData {
		return parkourDataList.getOrPut(uuid) { ParkourData(startPosition, 0, false) }
	}

	fun defaultStart() = defaultStart(Coords(x, z))

	companion object {
		var premiereArena: ParkourArena? = null

		fun playersParkour(uuid: UUID) = ArenaManager.ongoing.find { arena ->
			arena is ParkourArena && arena.owner == uuid
		} as ParkourArena?

		fun defaultStart(coords: Coords): BlockPos {
			val (centerX, centerZ) = getCenter(coords.x, coords.z)
			return BlockPos(centerX, Util.topBlockY(WorldManager.pvpWorld, centerX, centerZ), centerZ)
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
		val location = getParkourData(player.uniqueId).checkpoint.block(WorldManager.pvpWorld).location.add(0.5, 1.0, 0.5)
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

	override fun prepareArena(world: World) {}

	override fun arenaStart(onlinePlayers: List<Player>) {}

	override fun startText() = "Starting parkour in"

	override fun shutdownOnLeave() = false
}
