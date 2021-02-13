package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.CustomSpawning
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.util.Util
import com.codeland.uhc.gui.item.ParkourCheckpoint
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.team.TeamData
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.entity.Player

class WaitingDefault : Phase() {
	companion object {
		val oceans = arrayOf(
			Biome.OCEAN,
			Biome.DEEP_OCEAN,
			Biome.COLD_OCEAN,
			Biome.DEEP_COLD_OCEAN,
			Biome.FROZEN_OCEAN,
			Biome.DEEP_FROZEN_OCEAN,
			Biome.LUKEWARM_OCEAN,
			Biome.DEEP_LUKEWARM_OCEAN,
			Biome.WARM_OCEAN
		)

		fun validLobbySpot(world: World, x: Int, z: Int, radius: Int): Boolean {
			val halfRadius = radius / 2

			return Util.topLiquidSolidY(world, x, z).first == -1 &&
				Util.topLiquidSolidY(world, x + halfRadius, z + halfRadius).first == -1 &&
				Util.topLiquidSolidY(world, x - halfRadius, z + halfRadius).first == -1 &&
				Util.topLiquidSolidY(world, x + halfRadius, z - halfRadius).first == -1 &&
				Util.topLiquidSolidY(world, x - halfRadius, z - halfRadius).first == -1
		}

		val loadingTips = arrayOf(
			/* generation */
			"Melons generate in the world and drop 1 melon slice when broken",
			"Sugar cane spawns at the same rate in all biomes",
			"Look for extra sugar cane in surface water pools",
			"All types of leaves drop apples",
			"Oxeye daisies generate rarely in plains and flower forests",
			"Bonemeal can be used in plains and flower forests to obtain oxeye daisies",
			"1 in 25 giant mushroom blocks drops a mushroom",
			"1 in 200 leaves drops an apple",
			"Structures are turned off except for dungeons",
			"Enchanted golden apples cannot generate as dungeon loot",

			/* nether */
			"Each nether wart crop in the nether drops 1 nether wart",
			"Blazes and nether wart spawn naturally in the nether",
			"If the border overtakes your nether portal, a new one will be created",
			"There is no border in the nether",
			"All players in the nether will be killed when endgame starts",
			"portal coordinates are the same in the nether and overworld",
			"You can extinguish yourself in the nether with a cauldron",
			"Use warped fungus to scare away hoglins",
			"Use warped fungus to lure striders onto land",
			"Mining quartz is a good source of XP",
			"Have a block in your offhand while strip mining in the nether",
			"You can fill water bottles in the nether using a cauldron",

			/* brewing */
			"Remember to get sand for glass bottles",
			"Two blaze rods are required to brew",
			"The duration of strength potions is reduced",
			"The duration of regeneration potions is reduced",
			"The duration of poison potions is reduced",
			"brew melon for instant health and ghast tear for regeneration",
			"Poison can be brewed into instant damage with a fermented spider eye",
			"Weakness potions do not require nether wart",
			"Remember to kill creepers for gunpowder for splash potions",

			/* combat */
			"Axes have a 25% chance to disable a shield for 5 seconds",
			"Efficiency on an axe increases the chance to disable shields",
			"Axes will always disable shields when you are sprinting",
			"An arrow shot with the piercing enchantment hits through shields",
			"You cannot critical hit while sprinting",
			"Arrows cannot critical hit",
			"Splash potions' effects last longer the closer they hit to the target",
			"Protect yourself from an end crystal explosion by standing beneath it",

			/* caves */
			"Diamond, lapis, and gold ores only generate on the side of caves",
			"the generation of Coal, iron, and redstone ores is not changed",
			"Extra brown and red mushrooms spawn in caves below Y level 32",
			"Granite found below Y level 32 indicates a cave to the north",
			"Diorite found below Y level 32 indicates a cave to the east",
			"Andesite found below Y level 32 indicates a cave to the south",
			"Dirt found below Y level 32 indicates a cave to the west",
			"The lava fill level in caves is lowered allowing deeper exploration",
			"Ore veins of diamond, lapis, and gold are guaranteed to be contiguous",
			"Use a trapdoor to crawl and strip mine faster",
			"Watch out for creepers falling from the top of ravines",

			/* commands */
			"Use /sharecoords to quickly tell your teammates where you are",
			"Use /uhc gui to see the current game setup",
			"Use /uhc compass to tell what direction cave indcators point in",
			"Use @[username] in chat to mention a player",
			"Use /uhc color [color] to change your team's color",
			"prefix your message with ! to talk in global chat when on a team",

			/* game flow */
			"The border will start damaging you immediately if you move outside it",
			"Glowing is applied to all players every 15 seconds in endgame",
			"Your team regenerates two hearts each for killing another team",
			"You cannot naturally regenerate health after grace period ends",
			"Final endgame range will be at least 9 blocks",
			"You can build up to 3 blocks on top of the final range in endgame",
			"The rising floor in endgame will push you up",
			"Two players can tie if they die in the same second",
			"You can respawn during grace period",
			"If you combat log, a dummy will be put in your place",
			"selected UHC seeds have no oceans in the playable area",
			"You will be automatically moved into a voice call with your team",
			"The maximum hunger after grace period is 17, allowing you to eat whenever",
			"Do not help living players as a spectator",

			/* lobby */
			"Stand still for 15 seconds in lobby PVP to exit",
			"You are given random gear every time you spawn in lobby PVP",

			/* crafting */
			"2 spectral arrows are produced from 1 arrow and 4 glowstone dust",
			"Suspicious stew crafted with an oxeye daisy gives regeneration",

			/* drops */
			"Spiders always drop 1 string",
			"A trident drowned will always drop a trident",
			"Chickens drop 0.5 feathers on average",
			"Cows, horses, and llamas each drop 1 leather when killed",
			"Skeletons cannot drop a bow",
			"Blazes will always drop 1 blaze rod",
			"Striders will always drop 3 string",
			"Witches will always drop a potion",

			/* mob spawning */
			"Baby zombies cannot spawn",
			"Each player has their own mob cap",
			"Mobs cannot spawn directly above or below you",
			"Mobs tend to spawn at your same Y level",

			/* misc */
			"1 in 10 gravel blocks drops a flint",
			"You only need to mine the logs touching the leaves for leaves to decay",
			"Inventory management is key",
			"Sneak to hide your location underground",
			"Leaves can be instantly mined with an iron hoe",
			"Toggle sneak allows you to access block inventories while sneaking",
			"Remember to get water on the surface in case you can't find it in caves",
		)
	}

	override fun customStart() {
		val world = Bukkit.getWorlds()[0]
		
		fun findSpot(signX: Int, signZ: Int): Pair<Int, Int> {
			var x: Int
			var z: Int
			var tries = 0

			do {
				x = Util.randRange(10000, 100000) * signX
				z = Util.randRange(10000, 100000) * signZ
				++tries
			} while (!validLobbySpot(world, x, z, uhc.lobbyRadius) && tries < 100)

			return Pair(x, z)
		}

		if (uhc.lobbyX == -1) {
			val (x, z) = findSpot(1, 1)
			uhc.lobbyX = x
			uhc.lobbyZ = z

			PvpData.createArena(world, x, z, uhc.lobbyRadius)
		}

		if (uhc.lobbyPvpX == -1) {
			val (x, z) = findSpot(-1, -1)
			uhc.lobbyPvpX = x
			uhc.lobbyPvpZ = z

			PvpData.createArena(world, x, z, uhc.lobbyRadius)
			PvpData.determineHeight(uhc, world, x, z, uhc.lobbyRadius)
		}

		world.setSpawnLocation(uhc.lobbyX, Util.topBlockYTop(world, 254, uhc.lobbyX, uhc.lobbyZ) + 1, uhc.lobbyZ)
		world.worldBorder.reset()

		Bukkit.getWorlds().forEach { otherWorld ->
			otherWorld.isThundering = false
			otherWorld.setStorm(false)
			otherWorld.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
			otherWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
			otherWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)

			otherWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
			otherWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
			otherWorld.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)

			otherWorld.time = 6000
			otherWorld.difficulty = Difficulty.NORMAL
		}

		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.inventory.clear()
			onPlayerJoin(player)
		}
	}

	override fun customEnd() {
		Bukkit.getWorlds().forEach { world ->
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true)
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true)
		}
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return 1.0
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {}

	override fun endPhrase() = "Game starts in"

	fun onPlayerJoin(player: Player) {
		AbstractLobby.onSpawnLobby(player)
	}
}
