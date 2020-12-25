package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.CustomSpawning
import com.codeland.uhc.core.GameRunner
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

		fun teleportPlayerCenter(uhc: UHC, player: Player) {
			player.teleport(Location(Bukkit.getWorlds()[0], uhc.lobbyX + 0.5, Util.topBlockYTop(Bukkit.getWorlds()[0], 254, uhc.lobbyX, uhc.lobbyZ) + 1.0, uhc.lobbyZ + 0.5))
		}

		val loadingTips = arrayOf(
			"All types of leaves drop apples",
			"Melons spawn in most biomes and drop 1 melon slice when broken",
			"Blazes and nether wart spawn randomly in the nether",
			"Diamond, lapis, and gold ores can only be found in caves",
			"Look for extra sugar cane in surface water pools",
			"Spiders always drop 1 string",
			"1 in 25 giant mushroom blocks drops a mushroom",
			"The lower level in endgame will push you up",
			"Blazes will always drop 1 blaze rod",
			"Each nether wart plant broken in the nether drops 1 nether wart",
			"Remember to get sand early if you plan to brew potions",
			"Axes have a 25% chance to disable a shield for 5 seconds",
			"Trident drowneds will always drop their trident",
			"Extra brown and red mushrooms spawn in caves below Y level 32",
			"Granite found below Y level 32 indicates a cave to the north",
			"Diorite found below Y level 32 indicates a cave to the east",
			"Andesite found below Y level 32 indicates a cave to the south",
			"Dirt found below Y level 32 indicates a cave to the west",
			"The lava fill level in caves is lowered allowing deeper exploration",
			"Sugar cane spawns at the same rate in all biomes",
			"Use /sharecoords to quickly tell your teammate where you are",
			"Glowing is applied to all players every 15 seconds in endgame",
			"Arrows cannot critical hit",
			"All players in the nether will be killed when endgame starts",
			"If the border overtakes your portal, a new one will be created",
			"There is not border in the nether",
			"portal coordinates are the same in the nether and overworld",
			"Use /uhc gui to see the current game setup",
			"Stand still for 15 second in lobby PVP to exit",
			"The border will start damaging you immediately if you move outside it",
			"Oxeye daisies are rarer than default",
			"You regenerate two hearts for killing another team",
			"Use @[username] in chat to mention a player",
			"You cannot regenerate health after grace period ends",
			"Ore veins for diamond, lapis, and gold are guaranteed to be contiguous",
			"2 spectral arrows are produced from 1 arrow and 4 glowstone dust",
			"Suspicious stew crafted with an oxeye daisy gives regeneration",
			"Ghast tears produce Instant health when brewing",
			"Strength II cannot be brewed",
			"Two blaze rods are required to brew",
			"Final endgame range will be at least 9 blocks tall",
			"You can build up to 3 blocks on top of the final range in endgame",
			"You can extinguish yourself in the nether with a cauldron",
			"Two players can tie if they die in the same second",
			"Chickens drop 0.5 feathers on average",
			"Cows, horses, and llamas each drop 1 leather when killed",
			"Skeletons cannot drop their bow",
			"Remember to kill creepers for gunpowder for splash potions",
			"Bonemeal can be used in plains and flower forests to obtain oxeye daisies",
			"Use /uhc compass to tell what direction cave indcators point in",
			"If you can't find melons for regen, you can go for ghast tears instead",
			"Baby zombies cannot spawn",
			"Striders will always drop 3 string",
			"Use warped fungus to scare away hoglins",
			"Use warped fungus to lure striders onto land for killing",
			"Witches will always drop a potion",
			"You can respawn during grace period",
			"Spectators are not allowed to help living players",
			"Efficiency on an axe increases its chance to disable shields",
			"You cannot critical hit while sprinting at full speed",
			"Axes will always disable a shield while sprinting",
			"You can fill water bottles in the nether using a cauldron",
			"Remember to get water on the surface in case you can't find it in caves",
			"prefix your message with ! to talk in global chat",
			"1 in 10 gravel blocks drops a flint",
			"Mining quartz is a good source of XP",
			"Inventory management is key",
			"Sneak to hide your location underground",
			"Use a trapdoor to crawl and strip mine faster",
			"Toggle sneak allows you to access block inventories while sneaking",
			"End crystals deal more damage if the explosion hits the target's feet",
			"Piercing on crossbows ignores shields",
			"Structures are turned off except for dungeons",
			"selected UHC seeds have no oceans in the playable area",
			"The maximum hunger after grace period is 17, allowing you to eat whenever",
			"If you combat log, a dummy will be put in your place",
			"Use /uhc color [color] to change your team's color",
			"Splash potions' effects last longer the closer they hit to the target",
			"You only need to mine the logs touching the leaves for leaves to decay",
			"Watch out for creepers falling on you in ravines",
			"Leaves can be instantly mined with an iron hoe",
			"You will be automatically moved into a voice call with your team"
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

			LobbyPvp.createArena(world, x, z, uhc.lobbyRadius)
		}

		if (uhc.lobbyPvpX == -1) {
			val (x, z) = findSpot(-1, -1)
			uhc.lobbyPvpX = x
			uhc.lobbyPvpZ = z

			LobbyPvp.createArena(world, x, z, uhc.lobbyRadius)
			LobbyPvp.determineHeight(uhc, world, x, z, uhc.lobbyRadius)
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

		TeamData.removeAllTeams { player ->
			uhc.setParticipating(player, false)
		}

		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.inventory.clear()
			onPlayerJoin(player)
		}

		CustomSpawning.stopSpawning()
	}

	override fun customEnd() {
		Bukkit.getWorlds().forEach { world ->
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true)
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true)
		}

		LobbyPvp.allInPvp { player, pvpData ->
			pvpData.inPvp = false
			NameManager.updateName(player)
		}
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return 1.0
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {
		Bukkit.getOnlinePlayers().forEach { player ->
			ParkourCheckpoint.updateCheckpoint(player)
		}

		LobbyPvp.onTick()
	}

	override fun perSecond(remainingSeconds: Int) {
		val numSlides = 3
		val perSlide = 6

		fun slideN(n: Int): Boolean {
			return remainingSeconds % (numSlides * perSlide) < perSlide * (n + 1)
		}

		fun isFirst(): Boolean {
			return remainingSeconds % perSlide == 0
		}

		fun tip(player: Player, pvpData: LobbyPvp.PvpData) {
			if (isFirst()) pvpData.loadingTip = (Math.random() * loadingTips.size).toInt()

			player.sendActionBar("${ChatColor.GOLD}UHC Tips: ${ChatColor.WHITE}${ChatColor.BOLD}${loadingTips[pvpData.loadingTip]}")
		}

		Bukkit.getOnlinePlayers().forEach { player ->
			val pvpData = LobbyPvp.getPvpData(player)
			val team = TeamData.playersTeam(player.uniqueId)

			if (!pvpData.inPvp) {
				when {
					slideN(0) -> {
						if (uhc.usingBot) {
							val linked = GameRunner.bot?.isLinked(player.uniqueId)

							if (linked == null || linked) tip(player, pvpData)
							else player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}You are not linked! ${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}\"%link [your minecraft username]\" ${ChatColor.GOLD}in discord")
						} else tip(player, pvpData)
					}
					slideN(1) -> {
						tip(player, pvpData)
					}
					slideN(2) -> {
						if (team == null) tip(player, pvpData)
						else player.sendActionBar("${ChatColor.GOLD}Team name: ${team.colorPair.colorStringModified(team.displayName, ChatColor.BOLD)} ${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}/uhc name [name] ${ChatColor.GOLD}to set your team's name")
					}
				}
			}
		}
	}

	override fun endPhrase() = "Game starts in"

	fun onPlayerJoin(player: Player) {
		player.exp = 0.0F
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.foodLevel = 20
		player.fallDistance = 0f
		teleportPlayerCenter(uhc, player)
		player.gameMode = GameMode.CREATIVE

		Pests.makeNotPest(player)

		/* get them on the health scoreboard */
		player.damage(0.05)

		val inventory = player.inventory

		CommandItemType.giveItem(CommandItemType.GUI_OPENER, inventory)
		CommandItemType.giveItem(CommandItemType.JOIN_PVP, inventory)
		CommandItemType.giveItem(CommandItemType.PARKOUR_CHECKPOINT, inventory)
	}

}
