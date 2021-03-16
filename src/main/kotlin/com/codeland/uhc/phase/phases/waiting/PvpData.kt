package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.phase.phases.endgame.AbstractEndgame
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.*
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt

class PvpData(
	var inPvp: Boolean,
	var exiting: Boolean,
	var stillTime: Int,
	var killstreak: Int,
	var killstreakListIndex: Int,
	var killstreakList: Array<Int>,
	var lastLocation: Location,
	var oldGamemode: GameMode,
	var oldInventoryContents: Array<out ItemStack>,
) {
	companion object {
		fun defaultPvpData(): PvpData {
			return PvpData(false, false, 0, 0, 0, genKillstreakList(), Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0), GameMode.CREATIVE, emptyArray())
		}

		val STILL_TIME = 10 * 20

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
			LobbyPvpItems::genWaterBucket,
			LobbyPvpItems::genGapples,
			LobbyPvpItems::genRegenPotion,
			LobbyPvpItems::genPoisonPotion,

			/* inventory */
			LobbyPvpItems::genFood,
			LobbyPvpItems::genArrows,
			LobbyPvpItems::genPick,
			LobbyPvpItems::genBlocks,
			LobbyPvpItems::genAnvil,
		)

		fun enablePvp(player: Player, save: Boolean, teleport: Boolean): Location {
			val pvpData = PlayerData.getLobbyPvp(player.uniqueId)

			/* save before pvp state */
			if (save) {
				pvpData.oldInventoryContents = player.inventory.contents.clone()
				pvpData.oldGamemode = player.gameMode
			}

			/* enter into pvp */
			pvpData.inPvp = true
			pvpData.exiting = false
			pvpData.stillTime = 0
			pvpData.killstreak = 0
			pvpData.killstreakListIndex = 0
			Util.shuffleArray(pvpData.killstreakList)

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
			player.foodLevel = 17
			player.saturation = 5f
			player.exhaustion = 0f
			player.fireTicks = -1
			player.fallDistance = 0f
			player.setStatistic(Statistic.TIME_SINCE_REST, 0)

			/* tell player how to exit */
			player.sendActionBar("${GOLD}You have entered PVP. Use ${WHITE}${BOLD}/uhc lobby ${GOLD}to exit")

			val location = pvpSpawnLocation(GameRunner.uhc, player)
			if (teleport) player.teleport(location)

			allInPvp { sendPlayer, pvpData ->
				GameRunner.sendGameMessage(sendPlayer, "${player.name} entered pvp")
			}

			/* for some reason XP cannot get set immediately on respawn */
			SchedulerUtil.nextTick {
				player.exp = 0.0f
				player.level = 5
			}

			return location
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

		fun pvpSpawnLocation(uhc: UHC, player: Player): Location {
			val world = WorldManager.getPVPWorld()

			val centerX = 0
			val centerZ = 0
			val radius = uhc.lobbyRadius - 5

			val currentlyInPvp = ArrayList<Player>()
			allInPvp { otherPlayer, pvpData -> if (player !== otherPlayer) currentlyInPvp.add(otherPlayer) }

			var greatestDistance = 0.0
			var teleportX = 0
			var teleportZ = 0

			for (i in 0 until 100) {
				var leastDistance = Double.MAX_VALUE

				val thisTeleportX = Util.randRange(centerX - radius, centerX + radius)
				val thisTeleportZ = Util.randRange(centerZ - radius, centerZ + radius)

				currentlyInPvp.forEach { otherPlayer ->
					val distance =
						sqrt((otherPlayer.location.x - thisTeleportX).pow(2) + (otherPlayer.location.z - thisTeleportZ).pow(2))
					if (distance < leastDistance) leastDistance = distance
				}

				if (leastDistance > greatestDistance) {
					greatestDistance = leastDistance
					teleportX = thisTeleportX
					teleportZ = thisTeleportZ
				}
			}

			val (liquidY, solidY) = Util.topLiquidSolidYTop(world, 254, teleportX, teleportZ)

			return if (liquidY == -1) {
				Location(world, teleportX + 0.5, solidY + 1.0, teleportZ + 0.5)

			} else {
				world.getBlockAt(teleportX, liquidY, teleportZ).setType(Material.STONE, false)
				Location(world, teleportX + 0.5, liquidY + 1.0, teleportZ + 0.5)
			}
		}

		data class KillstreakKit(val name: String, val items: Array<() -> ItemStack>)

		/* killstreaks */

		fun isUpgradable(itemStack: ItemStack?): Boolean {
			if (itemStack == null) return false

			return when (itemStack.type) {
				Material.IRON_SWORD -> true
				Material.IRON_AXE -> true
				Material.IRON_BOOTS -> true
				Material.IRON_LEGGINGS -> true
				Material.IRON_CHESTPLATE -> true
				Material.IRON_HELMET -> true
				else -> false
			}
		}

		fun upgradeItem(itemStack: ItemStack) {
			itemStack.type = when (itemStack.type) {
				Material.IRON_SWORD -> Material.DIAMOND_SWORD
				Material.IRON_AXE -> Material.DIAMOND_AXE
				Material.IRON_BOOTS -> Material.DIAMOND_BOOTS
				Material.IRON_LEGGINGS -> Material.DIAMOND_LEGGINGS
				Material.IRON_CHESTPLATE -> Material.DIAMOND_CHESTPLATE
				Material.IRON_HELMET -> Material.DIAMOND_HELMET
				else -> Material.CACTUS
			}
		}

		fun upgradeDiamond(player: Player) {
			val potentialUpgrades = ArrayList<ItemStack>()

			for (i in 0..8) {
				val stack = player.inventory.getItem(i)
				if (isUpgradable(stack)) potentialUpgrades.add(stack!!)
			}

			for (i in 36..39) {
				val stack = player.inventory.getItem(i)
				if (isUpgradable(stack)) potentialUpgrades.add(stack!!)
			}

			if (potentialUpgrades.size > 0)
				upgradeItem(potentialUpgrades[(Math.random() * potentialUpgrades.size).toInt()])
		}

		val killstreakKits = arrayOf(
			KillstreakKit("Enchanted Books", arrayOf(
				LobbyPvpItems::genEnchantedBook, LobbyPvpItems::genEnchantedBook
			)),
			KillstreakKit("End Crystal Kit", arrayOf(
				{ ItemStack(Material.END_CRYSTAL) }, { ItemStack(Material.OBSIDIAN, 6) }
			)),
			KillstreakKit("Good Potions", arrayOf(
				LobbyPvpItems::genInstantHealthPotion, LobbyPvpItems::genSpeedPotion
			)),
			KillstreakKit("Offensive Potions", arrayOf(
				LobbyPvpItems::genWeaknessPotion, LobbyPvpItems::genInstantDamagePotion
			))
		)

		fun genKillstreakList(): Array<Int> {
			val indexList = Array(killstreakKits.size) { i -> i }
			Util.shuffleArray(indexList)
			return indexList
		}

		fun getNextKillstreak(pvpData: PvpData): KillstreakKit {
			val kit = killstreakKits[pvpData.killstreakListIndex]

			if (++pvpData.killstreakListIndex == killstreakKits.size) {
				pvpData.killstreakListIndex = 0
				Util.shuffleArray(pvpData.killstreakList)
			}

			return kit
		}

		fun tellKillstreak(player: Player, killstreak: Int, message: String) {
			player.sendTitle("${ChatColor.RED}Killstreak: ${ChatColor.DARK_RED}${killstreak}", "${ChatColor.RED}$message", 0, 40, 10)
		}

		fun giveKillstreakItem(player: Player, makeItems: Array<() -> ItemStack>) {
			/* attempt to add the items to the inventory */
			val couldNotAdd = player.inventory.addItem(*Array(makeItems.size) { i -> makeItems[i]() })

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

				upgradeDiamond(killer)
				giveKillstreakItem(killer, arrayOf(LobbyPvpItems::genResupplyArrows))
				giveKillstreakItem(killer, arrayOf(LobbyPvpItems::genResupplyGapples))

				val kit = getNextKillstreak(killerPvpData)
				giveKillstreakItem(killer, kit.items)
				tellKillstreak(killer, killerPvpData.killstreak, kit.name)
			}
		}

		fun resetStillTimer(player: Player) {
			val pvpData = PlayerData.getLobbyPvp(player.uniqueId)
			pvpData.stillTime = 0
			pvpData.exiting = false
		}

		fun onTick(currentTick: Int) {
			allInPvp { player, pvpData ->
				val newLocation = player.location
				if (newLocation.world != pvpData.lastLocation.world) pvpData.lastLocation = newLocation

				val distance = newLocation.distance(pvpData.lastLocation)
				pvpData.lastLocation = newLocation

				/* only counts as standing still if you are not moving and not sneaking */
				/* also check if not dead and on the respawn menu */
				if (distance == 0.0 && !player.isSneaking && player.health > 0) {
					++pvpData.stillTime

					if (pvpData.stillTime % 20 == 0) {
						if (pvpData.stillTime >= STILL_TIME) {
							if (pvpData.exiting) {
								disablePvp(player)
							} else {
								player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 50, 1, false, false, false))
								player.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 50, 1, false, false, false))
								player.sendActionBar("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Regenerating...")
							}
						} else if (pvpData.stillTime >= 5 * 20 || pvpData.exiting) {
							if (pvpData.exiting) {
								player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Returning to lobby in ${(STILL_TIME / 20) - (pvpData.stillTime / 20)}...")
							} else {
								player.sendActionBar("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Regeneration in ${(STILL_TIME / 20) - (pvpData.stillTime / 20)}...")
							}
						}
					}
				/* reset still timer when moved/sneaked */
				} else {
					pvpData.stillTime = 0
					pvpData.exiting = false
				}

				/* apply glowing to this player */
				if (pvpData.killstreak > 0) {
					val glowInterval = (30 - 5 * (pvpData.killstreak - 1)).coerceAtLeast(0)

					if ((currentTick % 20) == 0 && (glowInterval == 0 || (currentTick / 20) % glowInterval == 0)) player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, true))
				}
			}
		}
	}
}
