import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.ItemUtil
import com.codeland.uhc.quirk.ItemUtil.randFromArray
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

class CarePackages(type: QuirkType) : Quirk(type) {
	override fun onEnable() {
		currentRunnable = generateRunnable()
		currentRunnable?.runTaskTimer(GameRunner.plugin, 0, 20)

		var scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		scoreboard.getObjective("nextLocation")?.unregister()

		objective = scoreboard.registerNewObjective("nextLocation", "dummy", "Carepackage Drop Location")
		objective?.displaySlot = DisplaySlot.SIDEBAR
	}

	override fun onDisable() {
		currentRunnable?.cancel()
		currentRunnable = null

		objective?.unregister()
	}

	var objective = null as Objective?
	var currentRunnable = null as BukkitRunnable?

	fun generateRunnable(): BukkitRunnable {
		return object : BukkitRunnable() {
			var running = false

			var minTime = 2L * 60L
			var maxTime = 10L * 60L

			var timer = 0L

			var nextLocation = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)

			fun reset() {
				timer = 10L//GameRunner.randRange(minTime, maxTime)
				nextLocation = findDropSpot(timer, 16, Bukkit.getWorlds()[0].worldBorder)

				var scoreboard = Bukkit.getScoreboardManager().mainScoreboard

				objective?.getScore("X")?.score = nextLocation.blockX
				objective?.getScore("Y")?.score = nextLocation.blockY
				objective?.getScore("Z")?.score = nextLocation.blockZ
				objective?.getScore("TimeLeft")?.score = timer.toInt()
			}

			override fun run() {
				if (running) {
					objective?.getScore("TimeLeft")?.score = timer.toInt()

					--timer
					if (timer == 0L) {
						generateDrop(13, nextLocation)

						reset()
					}

				} else if (GameRunner.uhc.isPhase(PhaseType.GRACE) || GameRunner.uhc.isPhase(PhaseType.SHRINK)) {
					running = true
					reset()
				}
			}
		}
	}

	companion object {
		const val GOLD = 0
		const val IRON = 1
		const val DIAMOND = 2
		const val NETHERITE = 3

		class ToolInfo(val materials: Array<Material>, val enchants: Array<Array<Enchantment>>)

		val weapons = arrayOf(
			ToolInfo(arrayOf(Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.LOOT_BONUS_MOBS),
				arrayOf(Enchantment.FIRE_ASPECT),
				arrayOf(Enchantment.KNOCKBACK),
				arrayOf(Enchantment.SWEEPING_EDGE),
				arrayOf(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD)
			)),
			ToolInfo(arrayOf(Material.GOLDEN_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH),
				arrayOf(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD)
			))
		)

		val tools = arrayOf(
			ToolInfo(arrayOf(Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH)
			)),
			ToolInfo(arrayOf(Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH)
			)),
			ToolInfo(arrayOf(Material.GOLDEN_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH)
			))
		)

		val armor = arrayOf(
			ToolInfo(arrayOf(Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.OXYGEN),
				arrayOf(Enchantment.WATER_WORKER),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			)),
			ToolInfo(arrayOf(Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			)),
			ToolInfo(arrayOf(Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			)),
			ToolInfo(arrayOf(Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.PROTECTION_FALL),
				arrayOf(Enchantment.SOUL_SPEED),
				arrayOf(Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			))
		)

		val bows = arrayOf(
			ToolInfo(arrayOf(Material.BOW), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING, Enchantment.ARROW_INFINITE),
				arrayOf(Enchantment.ARROW_FIRE),
				arrayOf(Enchantment.ARROW_KNOCKBACK),
				arrayOf(Enchantment.ARROW_DAMAGE)
			)),
			ToolInfo(arrayOf(Material.CROSSBOW), arrayOf(
				arrayOf(Enchantment.PIERCING, Enchantment.MULTISHOT),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.QUICK_CHARGE)
			))
		)

		fun randTool(toolArray: Array<ToolInfo>, material: Int, enchantChance: Double): ItemStack {
			var toolInfo = randFromArray(toolArray)
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[material]), toolInfo.enchants, enchantChance)
		}

		fun randTool(toolArray: Array<ToolInfo>, enchantChance: Double): ItemStack {
			var toolInfo = randFromArray(toolArray)
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[0]), toolInfo.enchants, enchantChance)
		}

		class LootEntry(var count: Int, var makeStack: () -> ItemStack)

		val lootEntries = arrayOf(
			/* common */
			LootEntry(4) { randTool(weapons, GOLD, 0.5) },
			LootEntry(4) { randTool(tools, GOLD, 0.5) },
			LootEntry(4) { randTool(armor, GOLD, 0.5) },
			LootEntry(4) { randTool(bows, 0.25) },
			LootEntry(4) { ItemStack(       Material.LEATHER, GameRunner.randRange(2,  6)) },
			LootEntry(4) { ItemStack(         Material.PAPER, GameRunner.randRange(2,  6)) },
			LootEntry(4) { ItemStack(    Material.SUGAR_CANE, GameRunner.randRange(3,  7)) },
			LootEntry(4) { ItemStack(   Material.COOKED_BEEF, GameRunner.randRange(2,  5)) },
			LootEntry(4) { ItemStack(    Material.IRON_INGOT, GameRunner.randRange(3, 10)) },
			LootEntry(4) { ItemStack(        Material.STRING, GameRunner.randRange(3, 10)) },
			LootEntry(4) { ItemStack(  Material.RED_MUSHROOM, GameRunner.randRange(4, 12)) },
			LootEntry(4) { ItemStack(Material.BROWN_MUSHROOM, GameRunner.randRange(4, 12)) },
			LootEntry(4) { ItemStack(   Material.OXEYE_DAISY, GameRunner.randRange(4, 12)) },

			/* medium */
			LootEntry(3) { randTool(weapons, IRON, 0.25) },
			LootEntry(3) { randTool(tools, IRON, 0.25) },
			LootEntry(3) { randTool(armor, IRON, 0.25) },
			LootEntry(3) { ItemUtil.fireworkStar(GameRunner.randRange(1, 7), Color.fromRGB(GameRunner.randRange(0, 0xffffff))) },
			LootEntry(3) { ItemStack(   Material.GOLD_NUGGET, GameRunner.randRange(5,  9)) },
			LootEntry(3) { ItemStack(    Material.GOLD_INGOT, GameRunner.randRange(1,  6)) },
			LootEntry(3) { ItemStack( Material.GOLDEN_CARROT, GameRunner.randRange(3,  6)) },
			LootEntry(3) { ItemStack(           Material.TNT, GameRunner.randRange(1,  9)) },
			LootEntry(3) { ItemStack(      Material.OBSIDIAN, GameRunner.randRange(3,  6)) },

			/* rare */
			LootEntry(2) { randTool(weapons, DIAMOND, 0.25) },
			LootEntry(2) { randTool(tools, DIAMOND, 0.25) },
			LootEntry(2) { randTool(armor, DIAMOND, 0.25) },
			LootEntry(2) { ItemUtil.randomEnchantedBook() },
			LootEntry(2) { ItemStack(       Material.DIAMOND, GameRunner.randRange(1,  3)) },
			LootEntry(2) { ItemStack(     Material.BLAZE_ROD, GameRunner.randRange(1,  3)) },
			LootEntry(2) { ItemStack(  Material.BLAZE_POWDER, GameRunner.randRange(1,  6)) },
			LootEntry(2) { ItemStack(   Material.NETHER_WART, GameRunner.randRange(1,  7)) },

			/* mythic */
			LootEntry(1) { randTool(weapons, NETHERITE, 0.25) },
			LootEntry(1) { randTool(tools, NETHERITE, 0.25) },
			LootEntry(1) { randTool(armor, NETHERITE, 0.25) }
		)

		val lootIndices: Array<Int>

		init {
			/* find the length of the all the loot counts combined */
			var count = 0
			lootEntries.forEach { lootEntry ->
				count += lootEntry.count
			}

			lootIndices = Array<Int>(count) { 0 }

			/* add all of the entries to the indices */
			count = 0
			lootEntries.forEachIndexed { entryIndex, lootEntry ->
				for (i in count until count + lootEntry.count)
					lootIndices[i] = entryIndex

				count += lootEntry.count
			}
		}

		fun generateLoot(amount: Int, inventory: Inventory) {
			for (i in 0 until amount) {
				var space = GameRunner.randRange(0, inventory.size - 1)

				while (inventory.getItem(space) != null) {
					++space
					space %= inventory.size
				}

				var loot = lootEntries[ItemUtil.randFromArray(lootIndices)]

				inventory.setItem(space, loot.makeStack())
			}
		}

		fun findDropSpot(timeUntil: Long, buffer: Int, worldBorder: WorldBorder): Location {
			var currentRadius = worldBorder.size / 2

			var startRadius = GameRunner.uhc.preset.startRadius
			var endRadius = GameRunner.uhc.preset.endRadius

			/*var timeUntil = if (GameRunner.uhc.isPhase(PhaseType.GRACE)) {
				GameRunner.uhc.currentPhase?.getTimeRemaining()?.plus(GameRunner.uhc.preset.shrinkTime)
			} else if (GameRunner.uhc.isPhase(PhaseType.SHRINK)) {
				GameRunner.uhc.currentPhase?.getTimeRemaining()
			} else {
				null
			} ?: return null*/

			/* distance over time */
			var speed = (startRadius - endRadius).toFloat() / (GameRunner.uhc.preset.shrinkTime).toFloat()

			var maxRadius = (currentRadius - (speed * timeUntil) - buffer).toInt()

			var world = Bukkit.getWorlds()[0]

			var x = GameRunner.randRange(-maxRadius, maxRadius)
			var z = GameRunner.randRange(-maxRadius, maxRadius)
			var y = GameRunner.topBlockY(world, x, z) + 1

			return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
		}

		fun generateDrop(amount: Int, location: Location) {
			var world = Bukkit.getWorlds()[0]

			var block = world.getBlockAt(location)
			block.type = Material.CHEST

			generateLoot(amount, (block.state as Chest).blockInventory)

			var firework = world.spawnEntity(location.add(0.5, 0.5, 0.5), EntityType.FIREWORK) as Firework

			/* add effect to the firework */
			var meta = firework.fireworkMeta
			meta.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).withColor(Color.AQUA).withFlicker().build())
			firework.fireworkMeta = meta

			firework.detonate()
		}
	}
}
