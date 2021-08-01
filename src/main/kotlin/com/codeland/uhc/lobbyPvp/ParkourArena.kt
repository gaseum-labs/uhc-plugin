package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.event.Packet
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import it.unimi.dsi.fastutil.Hash
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.*

class ParkourArena(teams: ArrayList<ArrayList<UUID>>): Arena(ArenaType.PARKOUR, teams) {
	var start = defaultStart()

	val checkpoints = HashMap<UUID, Block>()
	val owner: UUID = teams[0][0]

	/* override */

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
		player.gameMode = GameMode.CREATIVE

		/* give items */
		player.inventory.addItem(GuiItem.name(ItemStack(Material.LAPIS_BLOCK), "${ChatColor.BLUE}Lapis (Parkour Start)"))
		player.inventory.addItem(GuiItem.name(ItemStack(Material.GOLD_BLOCK), "${ChatColor.YELLOW}Gold (Checkpoint)"))

		player.sendTitle("${ChatColor.GOLD}BUILD", "", 0, 20, 10)
	}

	override fun prepareArena(world: World) {}
}
