package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.GraceType
import com.codeland.uhc.phaseType.UHCPhase
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS
import org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
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
	fun onEntitySpawn(event : EntitySpawnEvent) {
		if (GameRunner.phase == UHCPhase.WAITING) {
			event.isCancelled = true
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

	fun enchantThing(item : ItemStack, enchant : Enchantment, level : Int) {
		val meta = item.itemMeta.clone()
		meta.addEnchant(enchant, level, true)
		item.itemMeta = meta
	}

	@EventHandler
	fun onBreakBlock(event : BlockBreakEvent) {
		var block = event.block;
		var player = event.player;

		val getTool = {
			/* get what the player is holding */
			/* pretend it isn't air if it is */
			var tool = player.inventory.itemInMainHand;
			if (tool.type == Material.AIR)
				tool = ItemStack(Material.PORKCHOP);

			/* get drops from block as if held item */
			/* had fortune */
			var fakeTool = tool.clone();
			if (GameRunner.abundance) enchantThing(fakeTool, LOOT_BONUS_BLOCKS, 5);

			fakeTool;
		}

		/* these replace regular block breaking behavior */
		event.isCancelled = GameRunner.unsheltered || GameRunner.abundance;

		if (GameRunner.unsheltered) {
			/* regular block breaking behavior for acceptable blocks */
			if (binarySearch(block.type, acceptedBlocks, {mat -> mat.ordinal})) {
				event.isCancelled = false;
				return;
			}

			var broken = block.state.getMetadata("broken");

			/* if we have not applied broken label or broken is explicitly set to false */
			/* proceed to mine then set broken to true */
			if (broken.size == 0 || !broken[0].asBoolean()) {
				/* manually drop items instead of the block breaking */
				var drops = block.getDrops(getTool());
				for (drop in drops)
					player.world.dropItem(block.location, ItemStack(drop.type, drop.amount));

				/* make sure we can't break this block again */
				block.state.setMetadata("broken", FixedMetadataValue(GameRunner.plugin as Plugin, true));
			} else {
				var message = TextComponent("Block already broken!");
				message.isBold = true;
				message.color = ChatColor.GOLD;

				player.sendMessage(message);
			}
		} else if (GameRunner.abundance) {
			block.breakNaturally(getTool());
		}
	}

	val acceptedBlocks = {
		var arr = arrayOf<Material>(
				Material.CRAFTING_TABLE,
				Material.FURNACE,
				Material.BREWING_STAND,
				Material.WHEAT_SEEDS,
				Material.BLAST_FURNACE,
				Material.SMOKER,
				Material.WATER,
				Material.LAVA,
				Material.LADDER,
				Material.ENCHANTING_TABLE,
				Material.BOOKSHELF,
				Material.SMITHING_TABLE,
				Material.LOOM,
				Material.ANVIL,
				Material.FLETCHING_TABLE,
				Material.COMPOSTER,
				Material.CHEST,
				Material.BARREL
		);
		arr.sortBy { mat -> mat.ordinal };

		arr;
	}();

	fun <T>binarySearch(value: T, array: Array<T>, sort: (T)->Int): Boolean {
		var start = 0;
		var end = array.size - 1;
		var lookFor = sort(value);

		while (true) {
			var position = (end + start) / 2;
			var compare = sort(array[position]);

			when {
				lookFor == compare -> return true;
				  end - start == 1 -> return false;
				 lookFor < compare -> end = position;
				 lookFor > compare -> start = position;
			}
		}
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		if (GameRunner.unsheltered) {
			var block = event.block;

			if (!binarySearch(block.type, acceptedBlocks, { mat -> mat.ordinal })) {
				event.isCancelled = true;
			}
		}
	}
}
