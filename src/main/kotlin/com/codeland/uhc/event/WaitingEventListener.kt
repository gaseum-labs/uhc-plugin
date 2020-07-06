package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.GraceType
import com.codeland.uhc.phaseType.UHCPhase
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS
import org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.logging.Level


class WaitingEventListener() : Listener {

	private val gameRunner = GameRunner

	@EventHandler
	fun onPlayerHurt(e : EntityDamageEvent) {
		if (gameRunner.phase != UHCPhase.WAITING) {
			return
		}
		if (e.entityType == EntityType.PLAYER) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onPlayerJoin(e : PlayerJoinEvent) {
		if (gameRunner.phase != UHCPhase.WAITING) {
			if (GameRunner.playersTeam(e.player.name) == null) {
				e.player.gameMode = GameMode.SPECTATOR
			}
			return
		}
		e.player.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, Int.MAX_VALUE, 0, false, false, false))
		e.player.gameMode = GameMode.ADVENTURE;
	}

	@EventHandler
	fun onWorldLoad(e : WorldLoadEvent) {
		e.world.setSpawnLocation(10000, 70, 10000)
		e.world.worldBorder.setCenter(10000.0, 10000.0)
		e.world.worldBorder.size = 50.0
		e.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		e.world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
		e.world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false) // could cause issue with dynamic spawn limit if true
		e.world.time = 1000
		e.world.difficulty = Difficulty.NORMAL
	}

	@EventHandler
	fun onPlayerDeath(e : PlayerDeathEvent) {
		e.entity.gameMode = GameMode.SPECTATOR
		GameRunner.playerDeath(e.entity)
	}

	@EventHandler
	fun onMessage(e : AsyncPlayerChatEvent) {
		if (GameRunner.phase != UHCPhase.WAITING) {
			if (!e.message.startsWith("!")) {
				val team = GameRunner.playersTeam(e.player.displayName)
				if (team != null) {
					e.isCancelled = true
					PaperPluginLogger.getGlobal().log(Level.INFO, "PLAYER SENT MESSAGE IN TEAM CHAT")
					for (entry in team.entries) {
						val precomp = TextComponent("<")
						val name = TextComponent(e.player.displayName)
						name.color = team.color.asBungee()
						name.isUnderlined = true
						val remaining = TextComponent("> ${e.message}")
						Bukkit.getPlayer(entry)?.sendMessage(precomp, name, remaining)
					}
				}
			} else {
				e.message = e.message.substring(1)
			}
		}
	}

	@EventHandler
	fun onPlayerTeleport(e : PlayerTeleportEvent) {
		if (!GameRunner.netherIsAllowed()) {
			if (e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && e.player.gameMode == GameMode.SURVIVAL) {
				e.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onEntitySpawn(e : CreatureSpawnEvent) {
		if (GameRunner.phase == UHCPhase.WAITING) {
			e.isCancelled = true
		}
	}

	/*@EventHandler
	fun onPlayerHold(e : PlayerItemHeldEvent) {
		if (GameRunner.uhc.graceType != GraceType.HALFZATOICHI) {
			return
		}
		if (e.player.inventory.itemInMainHand.type == Material.IRON_SWORD) {
			if (e.player.inventory.itemInMainHand.itemMeta.displayName == "Half Zatoichi") {
				e.isCancelled = true
			}
		}
	}*/

	@EventHandler
	fun onPlayerDropItem(e : PlayerDropItemEvent) {
		if (GameRunner.uhc.graceType != GraceType.HALFZATOICHI) {
			return
		}
		if (isHalfZatoichi(e.itemDrop.itemStack)) {
			e.isCancelled = true
		}
	}

	/*@EventHandler
	fun onSwapHandItemEvent(e : PlayerSwapHandItemsEvent) {
		if (GameRunner.uhc.graceType != GraceType.HALFZATOICHI) {
			return
		}
		if (isHalfZatoichi(e.offHandItem)) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onInventoryMoveEvent (e : InventoryClickEvent) {
		if (GameRunner.uhc.graceType != GraceType.HALFZATOICHI) {
			return
		}
		if (e.inventory.holder is Player) {
			val player = e.inventory.holder as Player
			if (player.inventory.itemInMainHand == e.currentItem) {
				if (isHalfZatoichi(e.currentItem)) {
					e.isCancelled = true
				}
			}
		}
		if (e.currentItem?.hasItemMeta() == true) {
			PaperPluginLogger.getGlobal().log(Level.INFO, "inventory move event, item = " + e.currentItem?.itemMeta?.displayName)
		} else {
			PaperPluginLogger.getGlobal().log(Level.INFO, "inventory move event, item = " + e.currentItem?.type)
		}
	}
*/
	@EventHandler
	fun onEntityDamageEvent(e : EntityDamageByEntityEvent) {
		if (GameRunner.uhc.graceType != GraceType.HALFZATOICHI) {
			return
		}
		if (e.damager is Player) {
			val attacker = e.damager as Player
		}
		if (e.damager is Player && e.entity is Player) {
			val defender = e.entity as Player
			val attacker = e.damager as Player
			if (isHalfZatoichi(attacker.inventory.itemInMainHand)) {
				if (isHalfZatoichi(defender.inventory.itemInMainHand)) {
					defender.health = 0.0
					defender.absorptionAmount = 0.0
				}
				if (defender.health + defender.absorptionAmount < e.finalDamage) {
					if (attacker.health < 10.0) {
						attacker.health += 10.0
					} else {
						attacker.absorptionAmount += attacker.health - 10.0
						attacker.health = 20.0
					}
					val meta = attacker.inventory.itemInMainHand.itemMeta.clone()
					meta.setDisplayName("Half Zatoichi (bloody)")
					attacker.inventory.itemInMainHand.itemMeta = meta
					PaperPluginLogger.getGlobal().log(Level.INFO, "damaged entity")
				}
			}
		}
	}

	fun isHalfZatoichi(item : ItemStack?) : Boolean {
		if (item?.type == Material.IRON_SWORD) {
			if (item.itemMeta.displayName.startsWith("Half Zatoichi")) {
				return true
			}
		}
		return false
	}

	val TOOLS = arrayOf(
		Material.DIAMOND_PICKAXE,
		Material.GOLDEN_PICKAXE,
		Material.IRON_PICKAXE,
		Material.STONE_PICKAXE,
		Material.WOODEN_PICKAXE,
		Material.DIAMOND_AXE,
		Material.GOLDEN_AXE,
		Material.IRON_AXE,
		Material.STONE_AXE,
		Material.WOODEN_AXE,
		Material.DIAMOND_HOE,
		Material.GOLDEN_HOE,
		Material.IRON_HOE,
		Material.STONE_HOE,
		Material.WOODEN_HOE,
		Material.DIAMOND_SHOVEL,
		Material.GOLDEN_SHOVEL,
		Material.IRON_SHOVEL,
		Material.STONE_SHOVEL,
		Material.WOODEN_SHOVEL)

	val SWORDS = arrayOf(
		Material.DIAMOND_SWORD,
		Material.GOLDEN_SWORD,
		Material.IRON_SWORD,
		Material.STONE_SWORD,
		Material.WOODEN_SWORD)

	private fun isContained(material: Material, array: Array<Material>): Boolean {
		for (element in array) if (element === material) return true
		return false
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		val result = event.recipe.result
		val type = result.type
		PaperPluginLogger.getGlobal().log(Level.INFO, "item crafted")
		if (isContained(type, TOOLS)) {
			PaperPluginLogger.getGlobal().log(Level.INFO, "tool crafted")
			enchantThing(result, LOOT_BONUS_BLOCKS, 3)
			event.isCancelled = true
		} else if (isContained(type, SWORDS)) {
			PaperPluginLogger.getGlobal().log(Level.INFO, "sword crafted")
			enchantThing(result, LOOT_BONUS_MOBS, 3)
			event.isCancelled = true
		} else {
			return
		}
		event.inventory.contents.forEach {
			it.amount -= 1
		}
		Bukkit.getServer().getPlayer(event.whoClicked.name)?.inventory?.addItem(result)
		PaperPluginLogger.getGlobal().log(Level.INFO, "gave item")
	}
	fun enchantThing(item : ItemStack, enchant : Enchantment, level : Int) {
		val meta = item.itemMeta.clone()
		meta.addEnchant(enchant, level, true)
		item.itemMeta = meta
	}
}