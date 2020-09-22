package com.codeland.uhc.event

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.block.data.Orientable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class Portal : Listener {
	fun findPortal(world: World, x: Int, y: Int, z: Int): Triple<Int, Int, Int> {
		var teleportedX = x
		var teleportedY = y
		var teleportedZ = z

		run {
			for (x in -2..2) for (y in -2..2) for (z in -2..2) {
				if (world.getBlockAt(teleportedX + x, teleportedY + y, teleportedZ + z).type == Material.NETHER_PORTAL) {
					teleportedX += x
					teleportedY += y
					teleportedZ += z

					return@run
				}
			}
		}

		/* find the lowest corner of the portal */
		while (world.getBlockAt(teleportedX, teleportedY, teleportedZ).type == Material.NETHER_PORTAL) --teleportedX
		++teleportedX
		while (world.getBlockAt(teleportedX, teleportedY, teleportedZ).type == Material.NETHER_PORTAL) --teleportedY
		++teleportedY
		while (world.getBlockAt(teleportedX, teleportedY, teleportedZ).type == Material.NETHER_PORTAL) --teleportedZ
		++teleportedZ

		return Triple(teleportedX, teleportedY, teleportedZ)
	}

	fun teleportToPortal(player: Player, world: World, x: Int, y: Int, z: Int) {
		player.teleport(Location(world, x + 0.5, y.toDouble(), z + 1.0))
	}

	@EventHandler
	fun onPlayerPortal(event: PlayerPortalEvent) {
		val player = event.player

		/* prevent peeking the center during waiting */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			event.isCancelled = true

		/* prevent going to the nether after nether closes */
		} else if (
			!GameRunner.netherIsAllowed() &&
			event.player.gameMode == GameMode.SURVIVAL
		) {
			val location = event.player.location
			val world = location.world

			/* break the portal */
			world.getBlockAt(location).type = Material.AIR
			Commands.errorMessage(event.player, "Nether is closed!")

			event.isCancelled = true

		/* portal coordinate fix */
		} else {
			val toWorld = if (player.world.environment == World.Environment.NORMAL) Bukkit.getWorlds()[1]
			else Bukkit.getWorlds()[0]

			event.canCreatePortal = false

			var (teleportedX, teleportedY, teleportedZ) = findPortal(player.world, player.location.blockX, player.location.blockY, player.location.blockZ)

			/* make sure destination portal is within border */
			val borderX = toWorld.worldBorder.center.blockX
			val borderZ = toWorld.worldBorder.center.blockZ
			val borderRadius = ((toWorld.worldBorder.size / 2) - 10).toInt()

			if (teleportedX < borderX - borderRadius) teleportedX = borderX - borderRadius
			else if (teleportedX > borderX + borderRadius) teleportedX = borderX + borderRadius
			if (teleportedZ < borderZ - borderRadius) teleportedZ = borderZ - borderRadius
			else if (teleportedZ > borderZ + borderRadius) teleportedZ = borderZ + borderRadius

			/* move destination portal within a range to not go too high or too low */
			teleportedY = if (toWorld.environment == World.Environment.NETHER)
				round(Util.interpClamp(32f, 119f, Util.invInterp(6f, 252f, teleportedY.toFloat()))).toInt()
			else
				round(Util.interpClamp(6f, 252f, Util.invInterp(32f, 119f, teleportedY.toFloat()))).toInt()

			var foundPortalY = -1

			/* look for portals in a 15 block y range */
			for (y in max(0, teleportedY - 7)..min(255, teleportedY + 7)) {
				if (toWorld.getBlockAt(teleportedX, y, teleportedZ).type == Material.NETHER_PORTAL) {
					foundPortalY = y
					break
				}
			}

			/* generate the portal if it isn't there */
			if (foundPortalY == -1) {
				/* (0, 0, 0) for generated portals is the portal block with the smallest coordinate */

				/* place air buffer around portal entrances */
				for (x in -1..1) for (z in -1..2) for (y in 0..3)
					toWorld.getBlockAt(teleportedX + x, teleportedY + y, teleportedZ + z).setType(Material.AIR, false)

				/* place portal obsidian border */
				for (z in -1..2) for (y in -1..3)
					toWorld.getBlockAt(teleportedX, teleportedY + y, teleportedZ + z).setType(Material.OBSIDIAN, false)

				/* place portal within border */
				for (z in 0..1) for (y in 0..2) {
					val block = toWorld.getBlockAt(teleportedX, teleportedY + y, teleportedZ + z)
					block.setType(Material.NETHER_PORTAL, false)

					val data = block.blockData as Orientable
					data.axis = Axis.Z
					block.blockData = data
				}

				/* place portal landing pad */
				for (x in -1..1) for (z in 0..1) {
					val block = toWorld.getBlockAt(teleportedX + x, teleportedY - 1, teleportedZ + z)
					if (block.isPassable) block.setType(Material.OBSIDIAN, false)
				}

				teleportToPortal(player, toWorld, teleportedX, teleportedY, teleportedZ)

			} else {
				var (destinationX, foundPortalY, destinationZ) = findPortal(toWorld, teleportedX, teleportedY, teleportedZ)

				teleportToPortal(player, toWorld, destinationX, foundPortalY, destinationZ)
			}
		}
	}
}
