package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

object LobbyPvp {
	class PvpData(
		var inPvp: Boolean = false,
		var onPvpBlock: Boolean = false,
		var gameMode: GameMode,
		var inventoryContents: Array<out ItemStack>
	)

	val pvpMap = mutableMapOf<UUID, PvpData>()
	val PVP_BLOCK = Material.LAPIS_BLOCK
	val REGEN_BLOCK = Material.EMERALD_BLOCK

	val PVP_ARMOR = Array(4) {
		i -> ItemStack(Material.values()[Material.IRON_HELMET.ordinal + (3 - i)])
	}

	val PVP_ITEMS = listOf(
		ItemStack(Material.IRON_SWORD, 1),
		ItemStack(Material.IRON_AXE, 1),
		{
			val b = ItemStack(Material.BOW, 1)
			b.addEnchantment(Enchantment.ARROW_INFINITE, 1)
			b
		}(),
		ItemStack(Material.ARROW, 1),
		ItemStack(Material.GOLDEN_APPLE, 3)
	)

	fun getPvpData(player: Player): PvpData {
		if (pvpMap[player.uniqueId] == null) pvpMap[player.uniqueId] = PvpData(gameMode = player.gameMode, inventoryContents = player.inventory.contents)
		return pvpMap[player.uniqueId]!!
	}

	fun enablePvp(player: Player) {
		val pvpData = getPvpData(player)
		GameRunner.sendGameMessage(player, "You enabled pvp.")
		pvpData.inPvp = true

		// save
		pvpData.inventoryContents = player.inventory.contents.clone()
		pvpData.gameMode = player.gameMode

		player.gameMode = GameMode.SURVIVAL
		player.inventory.clear()

		fun unbreakable(item: ItemStack): ItemStack {
			val m = item.itemMeta
			m.isUnbreakable = true
			item.itemMeta = m
			return item
		}

		player.inventory.addItem(*PVP_ITEMS.map(::unbreakable).toTypedArray())
		player.inventory.setItemInOffHand(unbreakable(ItemStack(Material.SHIELD)))
		player.inventory.setArmorContents(PVP_ARMOR.map(::unbreakable).toTypedArray())

		teleportPlayerIn(GameRunner.uhc, player)
	}

	fun disablePvp(player: Player) {
		val pvpData = getPvpData(player)

		GameRunner.sendGameMessage(player, "You disabled pvp.")
		pvpData.inPvp = false

		// restore
		player.inventory.contents = pvpData.inventoryContents
		player.gameMode = pvpData.gameMode

		player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
		player.activePotionEffects.clear()

		WaitingDefault.teleportPlayerCenter(GameRunner.uhc, player)
	}

	fun teleportPlayerIn(uhc: UHC, player: Player) {
		val world = Bukkit.getWorlds()[0]

		val x = uhc.lobbyPvpX
		val z = uhc.lobbyPvpZ
		val radius = uhc.lobbyRadius - 5

		val currentlyInPvp = ArrayList<Player>()

		pvpMap.forEach { (uuid, pvpData) ->
			if (pvpData.inPvp) {
				val pvpPlayer = Bukkit.getPlayer(uuid)
				if (pvpPlayer != null) {
					currentlyInPvp.add(player)
				}
			}
		}

		var greatestleastDistance = 1000.0
		var teleportX = 0.0
		var teleportZ = 0.0

		for (i in 0 until 20) {
			var thisLeastDistance = 1001.0
			val thisTeleportX = Util.randRange(x - radius, x + radius).toDouble()
			val thisTeleportZ = Util.randRange(z - radius, z + radius).toDouble()

			currentlyInPvp.forEach { player ->
				val thisDistance = sqrt((player.location.x - teleportX).pow(2) + (player.location.z - teleportZ).pow(2))
				if (thisDistance < thisLeastDistance) thisLeastDistance = thisDistance
			}

			if (thisLeastDistance > greatestleastDistance) {
				greatestleastDistance = thisLeastDistance
				teleportX = thisTeleportX
				teleportZ = thisTeleportZ
			}
		}

		player.teleport(Location(world, teleportX, Util.topBlockYTop(world, 254, teleportX.toInt(), teleportZ.toInt()) + 1.0, teleportZ))
	}

	fun createArena(world: World, x: Int, z: Int, radius: Int) {
		fun fillBlock(offX: Int, y: Int, offZ: Int) {
			val block = world.getBlockAt(x + offX, y, z + offZ)
			block.setType(if (block.isPassable) Material.BARRIER else Material.BEDROCK, false)
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
}
