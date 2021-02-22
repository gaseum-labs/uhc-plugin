package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.phase.phases.endgame.AbstractEndgame
import com.codeland.uhc.util.Util
import com.destroystokyo.paper.Title
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class PvpData(
	var inPvp: Boolean,
	var stillTime: Int,
	var killstreak: Int,
	var lastLocation: Location,
	var oldGamemode: GameMode,
	var oldInventoryContents: Array<out ItemStack>,
) {
	companion object {
		fun defaultPvpData(): PvpData {
			return PvpData(false, 0, 0, Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0), GameMode.CREATIVE, emptyArray())
		}

		val STILL_TIME = 15 * 20

		fun allInPvp(onPlayer: (Player, PvpData) -> Unit) {
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if (playerData.lobbyPVP.inPvp) {
					val player = Bukkit.getPlayer(uuid)

					if (player != null) onPlayer(player, playerData.lobbyPVP)
				}
			}
		}

		val itemsList = arrayOf(
			/* hotbar */
			LobbyPvpItems::genAxe,
			LobbyPvpItems::genSword,
			LobbyPvpItems::genLavaBucket,
			LobbyPvpItems::genBow,
			LobbyPvpItems::genCrossbow,
			LobbyPvpItems::genGapples,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genPotion,

			/* inventory */
			LobbyPvpItems::genSpectralArrows,
			LobbyPvpItems::genArrows,
			LobbyPvpItems::genFood,
			LobbyPvpItems::genPick,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genPotion,
			LobbyPvpItems::genWaterBucket,
		)

		fun enablePvp(player: Player) {
			val pvpData = PlayerData.getLobbyPvp(player.uniqueId)

			/* save before pvp state */
			pvpData.oldInventoryContents = player.inventory.contents.clone()
			pvpData.oldGamemode = player.gameMode

			/* enter into pvp */
			pvpData.inPvp = true
			pvpData.stillTime = 0
			pvpData.killstreak = 0

			/* give items */
			player.inventory.clear()

			player.inventory.setArmorContents(arrayOf(
				LobbyPvpItems.genBoots(),
				LobbyPvpItems.genLeggings(),
				LobbyPvpItems.genChestplate(),
				LobbyPvpItems.genHelmet()
			))

			player.inventory.setItemInOffHand(LobbyPvpItems.genShield())

			itemsList.forEach { gen -> player.inventory.addItem(gen()) }

			/* reset attributes */
			for (activePotionEffect in player.activePotionEffects) player.removePotionEffect(activePotionEffect.type)
			player.gameMode = GameMode.SURVIVAL
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
		}

		fun disablePvp(player: Player) {
			allInPvp { sendPlayer, pvpData ->
				GameRunner.sendGameMessage(sendPlayer, "${player.name} left pvp")
			}

			/* reset player stats */
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

			player.teleport(AbstractLobby.lobbyLocation(GameRunner.uhc, player))

			/* restore previous state */
			val pvpData = PlayerData.getLobbyPvp(player.uniqueId)
			pvpData.inPvp = false

			player.inventory.contents = pvpData.oldInventoryContents
			player.gameMode = pvpData.oldGamemode
		}

		fun teleportPlayerIn(uhc: UHC, player: Player) {
			val world = WorldManager.getPVPWorld()

			val centerX = 0
			val centerZ = 0
			val radius = uhc.lobbyRadius - 5

			val currentlyInPvp = ArrayList<Player>()
			allInPvp { player, pvpData -> currentlyInPvp.add(player) }

			var greatestDistance = 0.0
			var teleportX = 0
			var teleportZ = 0

			for (i in 0 until 100) {
				var leastDistance = Double.MAX_VALUE

				val thisTeleportX = Util.randRange(centerX - radius, centerX + radius)
				val thisTeleportZ = Util.randRange(centerZ - radius, centerZ + radius)

				currentlyInPvp.forEach { player ->
					val distance =
						sqrt((player.location.x - thisTeleportX).pow(2) + (player.location.z - thisTeleportZ).pow(2))
					if (distance < leastDistance) leastDistance = distance
				}

				if (leastDistance > greatestDistance) {
					greatestDistance = leastDistance
					teleportX = thisTeleportX
					teleportZ = thisTeleportZ
				}
			}

			val (liquidY, solidY) = Util.topLiquidSolidYTop(world, 254, teleportX, teleportZ)

			if (liquidY == -1) {
				player.teleport(Location(world, teleportX + 0.5, solidY + 1.0, teleportZ + 0.5))
			} else {
				world.getBlockAt(teleportX, liquidY, teleportZ).setType(Material.STONE, false)
				player.teleport(Location(world, teleportX + 0.5, liquidY + 1.0, teleportZ + 0.5))
			}
		}

		fun announceKillstreak(player: Player, killstreak: Int, message: String) {
			player.sendTitle("${ChatColor.RED}Killstreak: ${ChatColor.DARK_RED}${killstreak}", "${ChatColor.RED}$message", 0, 20, 10)
		}

		fun giveKillstreakItem(player: Player, vararg itemStack: ItemStack) {
			/* attempt to add the items to the inventory */
			val couldNotAdd = player.inventory.addItem(*itemStack)

			/* how many items to offset left by */
			val addSize = couldNotAdd.size

			/* replace the slots starting at 8 with the items that could not be added */
			/* throw out existing items */
			couldNotAdd.forEach { (index, killstreakItem) ->
				val replaceIndex = 18 + index - addSize

				val thrownItem = player.inventory.getItem(replaceIndex)
				if (thrownItem != null) player.world.dropItem(player.location, thrownItem)

				player.inventory.setItem(replaceIndex, killstreakItem)
			}
		}

		fun onKill(killer: Player) {
			val killerPvpData = PlayerData.getLobbyPvp(killer.uniqueId)

			if (killerPvpData.inPvp) {
				++killerPvpData.killstreak

				when (killerPvpData.killstreak) {
					1 -> {}
					2 -> {
						giveKillstreakItem(killer, ItemStack(Material.ANVIL))
						announceKillstreak(killer, killerPvpData.killstreak, "Anvil Bonus")
					}
					3 -> {
						giveKillstreakItem(killer, ItemStack(Material.END_CRYSTAL), ItemStack(Material.OBSIDIAN, 4))
						announceKillstreak(killer, killerPvpData.killstreak, "End crystal kit")
					}
					else -> {
						giveKillstreakItem(killer, LobbyPvpItems.genPotion())
						announceKillstreak(killer, killerPvpData.killstreak, "Random potion")
					}
				}
			}
		}

		fun onTick() {
			allInPvp { player, pvpData ->
				val newLocation = player.location
				if (newLocation.world != pvpData.lastLocation.world) pvpData.lastLocation = newLocation

				val distance = newLocation.distance(pvpData.lastLocation)
				pvpData.lastLocation = newLocation

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

		/**
		 * if the prepareArena process has already been done to this world
		 *
		 * it leaves a barrier as an indicator
		 */
		fun readArenaMarker(world: World, radius: Int): Pair<Int, Int>? {
			val sign = world.getBlockAt(-radius, 1, -radius).getState(false) as? Sign ?: return null

			val min = sign.getLine(0).toIntOrNull() ?: return null
			val max = sign.getLine(0).toIntOrNull() ?: return null

			return Pair(min, max)
		}

		/**
		 * applies an endgame like effect to the pvp arena to limit players skybasing and hiding underground
 		 */
		fun prepareArena(world: World, radius: Int, uhc: UHC) {
			val arenaMarker = readArenaMarker(world, radius)

			/* arena has not yet been created */
			if (arenaMarker == null) {
				val (min, max) = AbstractEndgame.determineMinMax(world, radius, 100)

				uhc.lobbyPVPMin = min
				uhc.lobbyPVPMax = max + 3

				for (x in -radius..radius) for (z in -radius..radius) {
					for (y in max + 1..255) {
						world.getBlockAt(x, y, z).setType(Material.AIR, false)
					}

					for (y in 0..min - 1) {
						world.getBlockAt(x, y, z).setType(Material.BEDROCK, false)
					}
				}

				/* mark prepared */
				val signBlock = world.getBlockAt(-radius, 1, -radius)
				signBlock.setType(Material.OAK_SIGN, false)
				val sign = signBlock.getState(true) as Sign
				sign.setLine(0, "${uhc.lobbyPVPMin}")
				sign.setLine(1, "${uhc.lobbyPVPMax}")

			/* arena has been created */
			} else {
				uhc.lobbyPVPMin = arenaMarker.first
				uhc.lobbyPVPMax = arenaMarker.second
			}
		}

		/* custom lobby walls currently unused */

		var lobbyCreationTaskID = -1
		var currentLobbyLayer = 0

		class LobbyQueueData(val world: World, val x: Int, val z: Int, val radius: Int)

		val lobbyQueue = LinkedList<LobbyQueueData>() as Queue<LobbyQueueData>

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

		init {
			treeList.sort()
		}

		private fun isTree(block: Block): Boolean {
			return Util.binarySearch(block.type, treeList)
		}

		fun createArena(world: World, x: Int, z: Int, radius: Int) {
			lobbyQueue.add(LobbyQueueData(world, x, z, radius))

			if (lobbyCreationTaskID == -1) {
				currentLobbyLayer = 0
				lobbyCreationTaskID =
					Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, ::lobbyTask, 0, 1)
			}
		}

		private fun stopCreateArena() {
			Bukkit.getScheduler().cancelTask(lobbyCreationTaskID)
		}

		fun lobbyTask() {
			val lobbyData = lobbyQueue.peek()

			/* stop making lobbies when queue is empty */
			if (lobbyData == null) {
				stopCreateArena()

			} else {
				val world = lobbyData.world
				val x = lobbyData.x
				val z = lobbyData.z
				val radius = lobbyData.radius

				fun fillBlock(offX: Int, y: Int, offZ: Int) {
					val block = world.getBlockAt(x + offX, y, z + offZ)
					block.setType(if (block.isPassable || isTree(block)) Material.BARRIER else Material.BEDROCK, false)
				}

				/* final layer, put in ceiling and move onto next lobby */
				if (currentLobbyLayer == 255) {
					for (xOff in -radius..radius) {
						for (zOff in -radius..radius) {
							world.getBlockAt(x + xOff, 255, z + zOff).setType(Material.BARRIER, false)
						}
					}

					currentLobbyLayer = 0
					lobbyQueue.remove()

				} else {
					for (offset in -radius - 1..radius + 1) {
						fillBlock(offset, currentLobbyLayer, -radius - 1)
						fillBlock(offset, currentLobbyLayer, radius + 1)
					}

					for (offset in -radius..radius) {
						fillBlock(-radius - 1, currentLobbyLayer, offset)
						fillBlock(radius + 1, currentLobbyLayer, offset)
					}

					++currentLobbyLayer
				}
			}
		}
	}
}
