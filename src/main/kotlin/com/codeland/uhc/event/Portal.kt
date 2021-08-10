package com.codeland.uhc.event

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.core.phase.phases.Endgame
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Orientable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent

class Portal : Listener {
	companion object {
		data class PortalEntry(val player: Player, var time: Int)

		var portalEntries = ArrayList<PortalEntry>()

		val PORTAL_TIME = 80

		fun portalTick() {
			Bukkit.getOnlinePlayers().forEach { player ->
				if (player.location.block.type === Material.NETHER_PORTAL) {
					if (portalEntries.find { it.player === player } == null) {
						portalEntries.add(PortalEntry(player, PORTAL_TIME))
					}
				}
			}

			portalEntries.removeIf { entry ->
				if (entry.player.location.block.type !== Material.NETHER_PORTAL) {
					true

				} else {
					if (--entry.time <= 0) {
						onPlayerPortal(entry.player)
						true
					} else {
						false
					}
				}
			}
		}

		fun searchAround(center: Block): Block {
			/* if we don't need to search */
			if (center.type == Material.NETHER_PORTAL) return center

			/* one of the portal blocks could be within a 3x3 cube centered on the player */
			for (x in -1..1) for (y in -1..1) for (z in -1..1) {
				val searchBlock = center.getRelative(x, y, z)

				if (searchBlock.type === Material.NETHER_PORTAL) return searchBlock
			}

			/* if somehow there was no portal found */
			return center
		}

		fun portalExtent(block: Block, direction: BlockFace): Block {
			var extent = block

			/* limit search to 23 blocks for weird circumstances */
			for (i in 0 until 23) {
				val next = extent.getRelative(direction)

				if (next.type == Material.NETHER_PORTAL) {
					extent = next
				} else {
					return extent
				}
			}

			return extent
		}

		fun findPortalCenter(exitPortalBlock: Block): Location {
			val portalBlock = portalExtent(searchAround(exitPortalBlock), BlockFace.DOWN)

			val axis = (portalBlock.blockData as Orientable).axis

			return if (axis === Axis.X) {
				val westMost = portalExtent(portalBlock, BlockFace.WEST)
				val eastMost = portalExtent(portalBlock, BlockFace.EAST)

				Location(portalBlock.world, (eastMost.x + westMost.x) / 2.0 + 0.5, portalBlock.y.toDouble(), portalBlock.z + 0.5, 0.0F, 0.0F)
			} else { /* z axis portal */
				val northMost = portalExtent(portalBlock, BlockFace.NORTH)
				val southMost = portalExtent(portalBlock, BlockFace.SOUTH)

				Location(portalBlock.world, portalBlock.x + 0.5, portalBlock.y.toDouble(), (southMost.z + northMost.z) / 2.0 + 0.5, 90.0F, 0.0F)
			}
		}

		fun reducePortalCorner(portalBlock: Block): Block {
			return portalExtent(portalExtent(portalExtent(portalBlock, BlockFace.NORTH), BlockFace.WEST), BlockFace.DOWN)
		}

		/**
		 * @param world the player's world, pre teleport
		 * @param location the player's location, pre teleport
		 * @return the block in the lowest coordinate of a portal that a player is standing in
		 */
		fun findPlayersPortal(world: World, location: Location): Block {
			return reducePortalCorner(searchAround(location.block))
		}

		/**
		 * @return the portal coordinate of an existing portal at this x z coordinate
		 * or NULL if no portal exists at this x z coordinate
		 */
		fun findExistingPortal(world: World, x: Int, z: Int): Block? {
			/* look for an existing portal at this x z coordinate */
			for (y in 1..126) {
				val block = world.getBlockAt(x, y, z)
				if (block.type == Material.NETHER_PORTAL) return reducePortalCorner(block)
			}

			return null
		}

		/**
		 * determines the resulting position of a portal teleport
		 * @return the block the portal is at OR the block the portal should be at
		 * AND a status of whether to create the exit portal or not
		 */
		fun getExitPortalPosition(exitWorld: World, x: Int, z: Int, idealPortalY: (World, Int, Int) -> Int): Pair<Block, Boolean> {
			/* look for an existing portal at this x z coordinate */
			val existingPortal = findExistingPortal(exitWorld, x, z)
			if (existingPortal != null) return Pair(existingPortal, false)

			/* there was no existing portal */
			return Pair(exitWorld.getBlockAt(x, idealPortalY(exitWorld, x, z), z), true)
		}

		fun teleportToPortal(player: Player, exitPortalBlock: Block) {
			player.teleport(findPortalCenter(exitPortalBlock))
		}

		fun setWithinBorder(border: WorldBorder, entranceX: Int, entranceZ: Int): Pair<Int, Int> {
			val borderX = border.center.blockX
			val borderZ = border.center.blockZ
			val borderRadius = ((border.size / 2) - 10).toInt()

			val inBorderX = if (entranceX < borderX - borderRadius)
				borderX - borderRadius
			else if (entranceX > borderX + borderRadius)
				borderX + borderRadius
			else
				entranceX

			val inBorderZ = if (entranceZ < borderZ - borderRadius)
				borderZ - borderRadius
			else if (entranceZ > borderZ + borderRadius)
				borderZ + borderRadius
			else
				entranceZ

			return Pair(inBorderX, inBorderZ)
		}

		/**
		 * (0, 0, 0) for generated portals is the portal block with the smallest coordinate
		 */
		fun buildPortal(portalBlock: Block) {
			val world = portalBlock.world

			/* place air buffer around portal entrances */
			for (x in -1..1) for (z in -1..2) for (y in 0..3)
				world.getBlockAt(portalBlock.x + x, portalBlock.y + y, portalBlock.z + z).setType(Material.AIR, false)

			/* place portal obsidian frame */
			for (z in -1..2) for (y in -1..3)
				world.getBlockAt(portalBlock.x, portalBlock.y + y, portalBlock.z + z).setType(Material.OBSIDIAN, false)

			/* place portal within frame */
			for (z in 0..1) for (y in 0..2) {
				val block = world.getBlockAt(portalBlock.x, portalBlock.y + y, portalBlock.z + z)
				block.setType(Material.NETHER_PORTAL, false)

				val data = block.blockData as Orientable
				data.axis = Axis.Z
				block.blockData = data
			}

			/* place portal landing pad */
			for (x in -1..1) for (z in 0..1) {
				val block = world.getBlockAt(portalBlock.x + x, portalBlock.y - 1, portalBlock.z + z)
				if (block.isPassable) block.setType(Material.OBSIDIAN, false)
			}
		}

		fun onPlayerPortal(player: Player) {
			val playerData = PlayerData.getPlayerData(player.uniqueId)
			val pvpGame = ArenaManager.playersArena(player.uniqueId)

			/* lobby pvpers can't escape through the never */
			if (pvpGame != null) {

			/* prevent peeking the center during waiting */
			} else if (!playerData.participating && UHC.game == null) {

			/* prevent going to the nether after nether closes */
			} else if (UHC.game?.phase is Endgame) {
				val location = player.location
				val world = location.world

				/* break the portal */
				world.getBlockAt(location).type = Material.AIR
				Commands.errorMessage(player, "Nether is closed!")

				/* portal coordinate fix */
			} else {
				/* override default portal creation behavior */

				/* going to the nether if in the game world */
				/* going to the game world if in nether */
				val exitWorld = if (player.world.name == WorldManager.GAME_WORLD_NAME)
					WorldManager.getNetherWorldGame()
				else
					WorldManager.getGameWorldGame()

				val entrancePortalBlock = findPlayersPortal(player.world, player.location)

				val (exitPortalX, exitPortalZ) = setWithinBorder(exitWorld.worldBorder, entrancePortalBlock.x, entrancePortalBlock.z)

				val (exitPortalBlock, needCreatePortal) = getExitPortalPosition(
					exitWorld,
					exitPortalX,
					exitPortalZ,
					if (exitWorld.environment == World.Environment.NORMAL)
						::idealPortalYOverworld
					else
						::idealPortalYNether
				)

				if (needCreatePortal) buildPortal(exitPortalBlock)

				teleportToPortal(player, exitPortalBlock)

				/* nether achievement fix */
				if (exitWorld.name == WorldManager.NETHER_WORLD_NAME) {
					val weNeedToGoDeeper = Bukkit.getServer().getAdvancement(NamespacedKey.minecraft("story/enter_the_nether"))

					if (weNeedToGoDeeper != null) {
						val progress = player.getAdvancementProgress(weNeedToGoDeeper)
						progress.remainingCriteria.forEach { progress.awardCriteria(it) }
					}
				}
			}
		}

		fun concentricSquare(centerX: Int, centerZ: Int, radius: Int, onBlock: (Int, Int) -> Int): Int {
			if (radius == 0) return onBlock(centerX, centerZ)

			for (x in centerX - radius..centerX + radius) {
				val y0 = onBlock(x, centerZ - radius)
				if (y0 != -1) return y0

				val y1 = onBlock(x, centerZ + radius)
				if (y1 != -1) return y1
			}

			for (z in centerZ - radius + 1..centerZ + radius - 1) {
				val y0 = onBlock(centerX - radius, z)
				if (y0 != -1) return y0

				val y1 = onBlock(centerX + radius, z)
				if (y1 != -1) return y1
			}

			return -1
		}

		fun isAir(block: Block): Boolean {
			return block.type == Material.AIR || block.type == Material.CAVE_AIR
		}

		fun isSolid(block: Block): Boolean {
			return !block.isPassable
		}

		fun idealPortalYNether(nether: World, x: Int, z: Int): Int {
			/* try up to 8 blocks away from the portal */
			for (radius in 0..8) {
				val squareY = concentricSquare(x, z, radius) { blockX, blockZ ->
					for (y in 32..107) {
						val midBlock = nether.getBlockAt(blockX, y, blockZ)

						if (isAir(midBlock) && isAir(midBlock.getRelative(BlockFace.UP)) && isSolid(midBlock.getRelative(BlockFace.DOWN)))
							return@concentricSquare y
					}

					return@concentricSquare -1
				}

				if (squareY != -1) return squareY
			}

			return 63
		}

		fun idealPortalYOverworld(overworld: World, x: Int, z: Int): Int {
			var solidCounter = 0

			for (y in 255 downTo 0) {
				if (isSolid(overworld.getBlockAt(x, y, z))) ++solidCounter
				if (solidCounter == 10) return y
			}

			return 63
		}
	}

	@EventHandler
	fun onPlayerPortalEvent(event: PlayerPortalEvent) {
		event.isCancelled = true
		event.canCreatePortal = false
	}
}
