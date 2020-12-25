package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

object LobbyPvp {
	val STILL_TIME = 15 * 20

	class PvpData(
		var inPvp: Boolean = false,
		var stillTime: Int = 0,
		var lastPosition: Location,
		var gameMode: GameMode,
		var inventoryContents: Array<out ItemStack>,
		var loadingTip: Int
	)

	val pvpMap = mutableMapOf<UUID, PvpData>()

	fun allInPvp(onPlayer: (Player, PvpData) -> Unit) {
		pvpMap.forEach { (uuid, pvpData) ->
			if (pvpData.inPvp) {
				val player = Bukkit.getPlayer(uuid)
				if (player != null) {
					onPlayer(player, pvpData)
				}
			}
		}
	}

	fun getPvpData(player: Player): PvpData {
		if (pvpMap[player.uniqueId] == null) pvpMap[player.uniqueId] = PvpData(gameMode = player.gameMode, inventoryContents = player.inventory.contents, lastPosition = player.location, loadingTip = 0)
		return pvpMap[player.uniqueId]!!
	}

	val itemsList = arrayOf(
		arrayOf(
			LobbyPvpItems::genAxe,
			LobbyPvpItems::genSword,
			LobbyPvpItems::genBow,
			LobbyPvpItems::genCrossbow,
			LobbyPvpItems::genWaterBucket,
			LobbyPvpItems::genGapples,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,

			LobbyPvpItems::genArrows,
			LobbyPvpItems::genSpectralArrows,
			LobbyPvpItems::genFood,
			LobbyPvpItems::genPick,
			LobbyPvpItems::genShovel,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genPotion
		),
		arrayOf(
			LobbyPvpItems::genAxe,
			LobbyPvpItems::genSword,
			LobbyPvpItems::genLavaBucket,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genCrossbow,
			LobbyPvpItems::genGapples,
			LobbyPvpItems::genPick,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,

			LobbyPvpItems::genBow,
			LobbyPvpItems::genArrows,
			LobbyPvpItems::genSpectralArrows,
			LobbyPvpItems::genFood,
			LobbyPvpItems::genPick,
			LobbyPvpItems::genShovel,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genWaterBucket
		),
		arrayOf(
			LobbyPvpItems::genAxe,
			LobbyPvpItems::genSword,
			LobbyPvpItems::genBow,
			LobbyPvpItems::genPick,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genGapples,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,

			LobbyPvpItems::genCrossbow,
			LobbyPvpItems::genArrows,
			LobbyPvpItems::genSpectralArrows,
			LobbyPvpItems::genFood,
			LobbyPvpItems::genShovel,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genWaterBucket,
			LobbyPvpItems::genLavaBucket,
			LobbyPvpItems::genPotion
		),
		arrayOf(
			LobbyPvpItems::genAxe,
			LobbyPvpItems::genSword,
			LobbyPvpItems::genBow,
			LobbyPvpItems::genEndCrystal,
			LobbyPvpItems::genObsidian,
			LobbyPvpItems::genGapples,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,

			LobbyPvpItems::genArrows,
			LobbyPvpItems::genSpectralArrows,
			LobbyPvpItems::genFood,
			LobbyPvpItems::genPick,
			LobbyPvpItems::genShovel,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genCrossbow,
			LobbyPvpItems::genWaterBucket,
			LobbyPvpItems::genLavaBucket
		)
	)

	fun enablePvp(player: Player) {
		val pvpData = getPvpData(player)

		pvpData.inPvp = true
		pvpData.stillTime = 0

		// save
		pvpData.inventoryContents = player.inventory.contents.clone()
		pvpData.gameMode = player.gameMode

		player.gameMode = GameMode.SURVIVAL
		player.inventory.clear()

		player.inventory.setArmorContents(arrayOf(LobbyPvpItems.genBoots(), LobbyPvpItems.genLeggings(), LobbyPvpItems.genChestplate(), LobbyPvpItems.genHelmet()))
		player.inventory.setItemInOffHand(LobbyPvpItems.genShield())

		Util.randFromArray(itemsList).forEach { gen -> player.inventory.addItem(gen()) }

		for (activePotionEffect in player.activePotionEffects)
			player.removePotionEffect(activePotionEffect.type)
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.absorptionAmount = 0.0
		player.exp = Math.random().toFloat()
		player.level = 4
		player.foodLevel = 17
		player.saturation = 5f
		player.exhaustion = 0f
		player.fireTicks = -1
		player.fallDistance = 0f
		player.setStatistic(Statistic.TIME_SINCE_REST, 0)

		teleportPlayerIn(GameRunner.uhc, player)

		allInPvp { sendPlayer, pvpData ->
			GameRunner.sendGameMessage(sendPlayer, "${player.name} entered pvp")
		}

		NameManager.updateName(player)
	}

	fun disablePvp(player: Player) {
		allInPvp { sendPlayer, pvpData ->
			GameRunner.sendGameMessage(sendPlayer, "${player.name} left pvp")
		}

		val pvpData = getPvpData(player)
		pvpData.inPvp = false

		// restore
		player.inventory.contents = pvpData.inventoryContents
		player.gameMode = pvpData.gameMode

		for (activePotionEffect in player.activePotionEffects)
			player.removePotionEffect(activePotionEffect.type)
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.absorptionAmount = 0.0
		player.exp = 0f
		player.level = 0
		player.foodLevel = 20
		player.saturation = 5f
		player.exhaustion = 0f
		player.fireTicks = -1
		player.fallDistance = 0f
		player.setStatistic(Statistic.TIME_SINCE_REST, 0)

		WaitingDefault.teleportPlayerCenter(GameRunner.uhc, player)

		NameManager.updateName(player)
	}

	fun teleportPlayerIn(uhc: UHC, player: Player) {
		val world = Bukkit.getWorlds()[0]

		val x = uhc.lobbyPvpX
		val z = uhc.lobbyPvpZ
		val radius = uhc.lobbyRadius - 5

		val currentlyInPvp = ArrayList<Player>()

		allInPvp { player, pvpData ->
			currentlyInPvp.add(player)
		}

		var greatestDistance = 0.0
		var teleportX = 0.0
		var teleportZ = 0.0

		for (i in 0 until 100) {
			var cumulativeDistance = 0.0
			val thisTeleportX = Util.randRange(x - radius, x + radius).toDouble()
			val thisTeleportZ = Util.randRange(z - radius, z + radius).toDouble()

			currentlyInPvp.forEach { player ->
				cumulativeDistance += sqrt((player.location.x - thisTeleportX).pow(2) + (player.location.z - thisTeleportZ).pow(2))
			}

			if (cumulativeDistance > greatestDistance) {
				greatestDistance = cumulativeDistance
				teleportX = thisTeleportX
				teleportZ = thisTeleportZ
			}
		}

		player.teleport(Location(world, teleportX, Util.topBlockYTop(world, 254, teleportX.toInt(), teleportZ.toInt()) + 1.0, teleportZ))
	}

	private val treeList = arrayOf(
		Material.OAK_LOG,
		Material.BIRCH_LOG,
		Material.ACACIA_LOG,
		Material.DARK_OAK_LOG,
		Material.SPRUCE_LOG,
		Material.JUNGLE_LOG,
		Material.OAK_LEAVES,
		Material.BIRCH_LEAVES,
		Material.ACACIA_LEAVES,
		Material.DARK_OAK_LEAVES,
		Material.SPRUCE_LEAVES,
		Material.JUNGLE_LEAVES
	)

	init { treeList.sort() }

	private fun isTree(block: Block): Boolean {
		return Util.binarySearch(block.type, treeList)
	}

	fun createArena(world: World, x: Int, z: Int, radius: Int) {
		fun fillBlock(offX: Int, y: Int, offZ: Int) {
			val block = world.getBlockAt(x + offX, y, z + offZ)
			block.setType(if (block.isPassable || isTree(block)) Material.BARRIER else Material.BEDROCK, false)
		}

		for (y in 0..255) {
			for (offset in -radius - 1..radius + 1) {
				fillBlock( offset    , y, -radius - 1)
				fillBlock( offset    , y,  radius + 1)
			}

			for (offset in -radius..radius) {
				fillBlock(-radius - 1, y,  offset    )
				fillBlock( radius + 1, y,  offset    )
			}
		}

		for (xOff in -radius..radius) {
			for (zOff in -radius..radius) {
				world.getBlockAt(x + xOff, 255, z + zOff).setType(Material.BARRIER, false)
			}
		}
	}

	fun determineHeight(uhc: UHC, world: World, x: Int, z: Int, radius: Int) {
		var maxHeight = 0

		for (xOff in -radius..radius) {
			for (zOff in -radius..radius) {
				val height = Util.topBlockYTop(world, 254, x + xOff, z + zOff)
				if (height > maxHeight)
				maxHeight = height
			}
		}

		uhc.lobbyPvpHeight = maxHeight + 3
	}

	fun onTick() {
		allInPvp { player, pvpData ->
			val newLocation = player.location
			val distance = newLocation.distance(pvpData.lastPosition)
			pvpData.lastPosition = newLocation

			if (distance == 0.0 && !player.isSneaking) {
				val stillTime = pvpData.stillTime + 1

				if (stillTime == STILL_TIME) {
					disablePvp(player)
				} else if (stillTime % 20 == 0 && stillTime >= 10 * 20) {
					player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Returning to Lobby in ${(STILL_TIME / 20) - (stillTime / 20)}...")
				}

				pvpData.stillTime = stillTime

			} else {
				pvpData.stillTime = 0
			}
		}
	}
}
