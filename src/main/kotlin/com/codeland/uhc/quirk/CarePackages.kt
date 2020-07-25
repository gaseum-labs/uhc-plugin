import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Util
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.ItemUtil
import com.codeland.uhc.quirk.ItemUtil.randFromArray
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor.*
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.*

class CarePackages(type: QuirkType) : Quirk(type) {
	val OBJECTIVE_NAME = "carePackageDrop"

	override fun onEnable() {
		/* start the carepackage timer loop */
		currentRunnable = generateRunnable()
		currentRunnable?.runTaskTimer(GameRunner.plugin, 0, 20)

		/* get rid of any lingering objectives from this quirk */
		var scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		scoreboard.getObjective(OBJECTIVE_NAME)?.unregister()

		objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", "Next Care Package Drop")
		objective?.displaySlot = DisplaySlot.SIDEBAR
	}

	override fun onDisable() {
		currentRunnable?.cancel()
		currentRunnable = null

		objective?.unregister()
	}

	var objective = null as Objective?
	var currentRunnable = null as BukkitRunnable?

	var scoreT = null as Score?
	var scoreP = null as Score?

	fun generateRunnable(): BukkitRunnable {
		return object : BukkitRunnable() {
			val minTime = 2 * 60
			val maxTime = 10 * 60

			val minItems = 9
			val maxItems = 18

			var running = false

			var timer = 0
			var nextLocation = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)

			lateinit var dropTimes: Array<Int>
			var dropIndex = 0

			fun setScore(objective: Objective?, score: Score?, value: String, index: Int): Score {
				/* trick kotlin in allowing us to return */
				if (objective == null) return Bukkit.getScoreboardManager().mainScoreboard.registerNewObjective("", "", "").getScore("")

				/* remove the previous score if applicable */
				if (score != null) {
					val scoreboard = objective.scoreboard
					scoreboard?.resetScores(score.entry)
				}

				val ret = objective.getScore(value)
				ret.score = index

				return ret
			}

			fun shutOff() {
				running = false
				objective?.displaySlot = null
			}

			fun reset() {
				/* don't keep doing this outside of shrinking */
				if (!GameRunner.uhc.isPhase(PhaseType.SHRINK) || dropIndex == dropTimes.size) {
					shutOff()
					return
				}

				timer = dropTimes[dropIndex]
				++dropIndex

				nextLocation = findDropSpot(timer, 16, Bukkit.getWorlds()[0].worldBorder)

				val coordinateString = "at (${ChatColor.GOLD}${BOLD}${nextLocation.blockX}${RESET}, ${ChatColor.GOLD}${BOLD}${nextLocation.blockY}${RESET}, ${ChatColor.GOLD}${BOLD}${nextLocation.blockZ}${RESET})"

				scoreT = setScore(objective, scoreT, "in ${ChatColor.GOLD}${BOLD}${Util.timeString(timer)}", 1)
				scoreP = setScore(objective, scoreP, coordinateString, 0)
			}

			fun generateDropTimes(shrinkTime: Int): Array<Int> {
				/* we want about 1 every 5 minutes */
				val targetTime = 5 * 60

				val numDrops = shrinkTime / targetTime
				val averageTime = shrinkTime / numDrops

				val ret = Array(numDrops) { averageTime }

				/* add randomness in the times */
				for (i in ret.indices) {
					/* half a minute to a minute differences */
					var shiftAmount = Util.randRange(60, 120)

					var reduceIndex = Util.randRange(0, ret.lastIndex)
					var gainIndex = Util.randRange(0, ret.lastIndex)

					var oldReduce = ret[reduceIndex]
					ret[reduceIndex] -= shiftAmount

					/* don't reduce any time to less than a minute */
					/* if it would, find the actual amount it got reduced by */
					if (ret[reduceIndex] < 60) {
						ret[reduceIndex] = 60
						shiftAmount = oldReduce - ret[reduceIndex]
					}

					ret[gainIndex] += shiftAmount
				}

				return ret
			}

			fun generateDropAmount(dropIndex: Int, dropTimes: Array<Int>): Int {
				val lastDropIndex = dropTimes.lastIndex

				val along = dropIndex.toFloat() / lastDropIndex.toFloat()

				return ((maxItems - minItems) * along).toInt() + minItems
			}

			override fun run() {
				if (running) {
					--timer
					if (timer == 0) {
						generateDrop(generateDropAmount(dropIndex, dropTimes), nextLocation)
						reset()
					} else {
						scoreT = setScore(objective, scoreT, "in ${ChatColor.GOLD}${BOLD}${Util.timeString(timer)}", 1)
					}

				/* start making drops during shrinking round */
				} else if (GameRunner.uhc.isPhase(PhaseType.SHRINK)) {
					running = true
					dropTimes = generateDropTimes(GameRunner.uhc.getTime(PhaseType.SHRINK))

					dropTimes.forEach { drop ->
						Util.log("time: $drop")
					}

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

		val elytra = ToolInfo(arrayOf(Material.ELYTRA), arrayOf(
			arrayOf(Enchantment.MENDING),
			arrayOf(Enchantment.DURABILITY)
		))

		val trident = ToolInfo(arrayOf(Material.TRIDENT), arrayOf(
			arrayOf(Enchantment.MENDING),
			arrayOf(Enchantment.DURABILITY),
			arrayOf(Enchantment.IMPALING),
			arrayOf(Enchantment.CHANNELING, Enchantment.LOYALTY, Enchantment.RIPTIDE)
		))

		fun randTool(toolArray: Array<ToolInfo>, material: Int, enchantChance: Double): ItemStack {
			var toolInfo = randFromArray(toolArray)
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[material]), toolInfo.enchants, enchantChance)
		}

		fun randTool(toolArray: Array<ToolInfo>, enchantChance: Double): ItemStack {
			var toolInfo = randFromArray(toolArray)
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[0]), toolInfo.enchants, enchantChance)
		}

		fun aTool(toolInfo: ToolInfo, enchantChance: Double): ItemStack {
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[0]), toolInfo.enchants, enchantChance)
		}

		class LootEntry(var count: Int, var makeStack: () -> ItemStack)

		val lootEntries = arrayOf(
			/* common */
			LootEntry(4) { randTool(weapons, GOLD, 0.5) },
			LootEntry(4) { randTool(tools, GOLD, 0.5) },
			LootEntry(4) { randTool(armor, GOLD, 0.5) },
			LootEntry(4) { randTool(bows, 0.25) },
			LootEntry(4) { ItemStack(       Material.LEATHER, Util.randRange(2,  6)) },
			LootEntry(4) { ItemStack(         Material.PAPER, Util.randRange(2,  6)) },
			LootEntry(4) { ItemStack(   Material.COOKED_BEEF, Util.randRange(2,  5)) },
			LootEntry(4) { ItemStack(    Material.IRON_INGOT, Util.randRange(3, 10)) },
			LootEntry(4) { ItemStack(   Material.IRON_NUGGET, Util.randRange(5, 18)) },
			LootEntry(4) { ItemStack(        Material.STRING, Util.randRange(3, 10)) },
			LootEntry(4) { ItemStack(  Material.RED_MUSHROOM, Util.randRange(4, 12)) },
			LootEntry(4) { ItemStack(Material.BROWN_MUSHROOM, Util.randRange(4, 12)) },
			LootEntry(4) { ItemStack(   Material.OXEYE_DAISY, Util.randRange(4, 12)) },
			LootEntry(4) { ItemStack(     Material.GUNPOWDER, Util.randRange(5, 12)) },
			LootEntry(4) { ItemStack(          Material.BOOK, Util.randRange(1,  4)) },

			/* medium */
			LootEntry(3) { randTool(weapons, IRON, 0.25) },
			LootEntry(3) { randTool(tools, IRON, 0.25) },
			LootEntry(3) { randTool(armor, IRON, 0.25) },
			LootEntry(3) { ItemStack(   Material.GOLD_NUGGET, Util.randRange(5, 18)) },
			LootEntry(3) { ItemStack(    Material.GOLD_INGOT, Util.randRange(1,  6)) },
			LootEntry(3) { ItemStack( Material.GOLDEN_CARROT, Util.randRange(3,  6)) },
			LootEntry(3) { ItemStack(           Material.TNT, Util.randRange(1,  9)) },
			LootEntry(3) { ItemStack(      Material.OBSIDIAN, Util.randRange(3,  6)) },
			LootEntry(3) { ItemStack(  Material.LAPIS_LAZULI, Util.randRange(8, 16)) },
			LootEntry(3) { ItemUtil.randomFireworkStar(Util.randRange(3, 7)) },
			LootEntry(3) { ItemUtil.randomRocket(Util.randRange(3, 6)) },
			LootEntry(3) { ItemUtil.randomEnchantedBook() },
			LootEntry(3) { ItemUtil.randomPotion(true, Math.random() < 0.5) },

			/* rare */
			LootEntry(2) { randTool(weapons, DIAMOND, 0.25) },
			LootEntry(2) { randTool(tools, DIAMOND, 0.25) },
			LootEntry(2) { randTool(armor, DIAMOND, 0.25) },
			LootEntry(2) { ItemStack(       Material.DIAMOND, Util.randRange(1,  3)) },
			LootEntry(2) { ItemStack(     Material.BLAZE_ROD, Util.randRange(1,  3)) },
			LootEntry(2) { ItemStack(  Material.BLAZE_POWDER, Util.randRange(1,  6)) },
			LootEntry(2) { ItemStack(   Material.NETHER_WART, Util.randRange(1,  7)) },
			LootEntry(2) { ItemStack(  Material.GOLDEN_APPLE, Util.randRange(1,  3)) },
			LootEntry(2) { ItemUtil.randomPotion(false, true) },

			/* mythic */
			LootEntry(1) { ItemStack(Material.NETHERITE_INGOT) },
			LootEntry(1) { randTool(weapons, NETHERITE, 0.25) },
			LootEntry(1) { randTool(tools, NETHERITE, 0.25) },
			LootEntry(1) { randTool(armor, NETHERITE, 0.25) },
			LootEntry(1) { ItemStack(Material.ENCHANTED_GOLDEN_APPLE) },
			LootEntry(1) { aTool(elytra, 0.25) },
			LootEntry(1) { aTool(trident, 0.25) }
		)

		private val lootIndices: Array<Int>

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
				var space = Util.randRange(0, inventory.size - 1)

				while (inventory.getItem(space) != null) {
					++space
					space %= inventory.size
				}

				var loot = lootEntries[ItemUtil.randFromArray(lootIndices)]

				inventory.setItem(space, loot.makeStack())
			}
		}

		fun findDropSpot(timeUntil: Int, buffer: Int, worldBorder: WorldBorder): Location {
			var currentRadius = worldBorder.size / 2

			var startRadius = GameRunner.uhc.preset.startRadius
			var endRadius = GameRunner.uhc.preset.endRadius

			/* distance over time */
			var speed = (startRadius - endRadius).toFloat() / (GameRunner.uhc.preset.shrinkTime).toFloat()

			var maxRadius = (currentRadius - (speed * timeUntil) - buffer).toInt()

			var world = Bukkit.getWorlds()[0]

			var x = Util.randRange(-maxRadius, maxRadius)
			var z = Util.randRange(-maxRadius, maxRadius)
			var y = Util.topBlockY(world, x, z) + 1

			return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
		}

		fun generateDrop(amount: Int, location: Location) {
			var world = Bukkit.getWorlds()[0]

			var block = world.getBlockAt(location)
			block.type = Material.CHEST

			var chest = block.getState(false) as Chest
			chest.customName = "${ChatColor.GOLD}${ChatColor.BOLD}Care Package"

			generateLoot(amount, chest.blockInventory)

			var firework = world.spawnEntity(location.add(0.5, 0.5, 0.5), EntityType.FIREWORK) as Firework

			/* add effect to the firework */
			var meta = firework.fireworkMeta
			meta.addEffect(ItemUtil.randomFireworkEffect())
			firework.fireworkMeta = meta

			firework.detonate()
		}
	}
}
