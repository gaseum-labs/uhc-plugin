package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.event.Chat
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

object AbstractLobby {
	fun lobbyLocation(uhc: UHC, player: Player): Location {
		val world = WorldManager.getLobbyWorld()
		return Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
	}

	fun onSpawnLobby(player: Player): Location {
		player.exp = 0.0F
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.foodLevel = 20
		player.fallDistance = 0f
		player.gameMode = GameMode.CREATIVE

		/* get them on the health scoreboard */
		player.damage(0.05)

		/* reset applied status for all active quirks */
		val playerData = PlayerData.getPlayerData(player.uniqueId)
		GameRunner.uhc.quirks.forEach { quirk ->
			if (quirk.enabled) PlayerData.getQuirkDataHolder(playerData, quirk.type).applied = false
		}

		Pests.makeNotPest(player)

		val location = lobbyLocation(GameRunner.uhc, player)
		player.teleport(location)

		CommandItemType.giveItem(CommandItemType.GUI_OPENER, player.inventory)
		CommandItemType.giveItem(CommandItemType.JOIN_PVP, player.inventory)
		//CommandItemType.giveItem(CommandItemType.PARKOUR_CHECKPOINT, player.inventory)
		CommandItemType.giveItem(CommandItemType.SPECTATE, player.inventory)

		return location
	}

	fun lobbyTipsTick(subTick: Int) {
		if (subTick % 20 == 0) {
			val numSlides = 3
			val perSlide = 6

			fun slideN(n: Int) = (subTick / 20) % (numSlides * perSlide) < perSlide * (n + 1)
			fun isFirst() = (subTick / 20) % perSlide == 0

			fun tip(player: Player, playerData: PlayerData) {
				if (isFirst()) playerData.loadingTip = (Math.random() * loadingTips.size).toInt()

				player.sendActionBar(Component.text("${ChatColor.GOLD}UHC Tips: ${ChatColor.WHITE}${ChatColor.BOLD}${loadingTips[playerData.loadingTip]}"))
			}

			Bukkit.getOnlinePlayers().forEach { player ->
				val playerData = PlayerData.getPlayerData(player.uniqueId)
				val team = TeamData.playersTeam(player.uniqueId)

				if (!playerData.participating && !playerData.lobbyPVP.inPvp) {
					if (player.gameMode == GameMode.SPECTATOR) {
						if (slideN(0)) {
							player.sendActionBar(Component.text("${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}/uhc lobby ${ChatColor.GOLD}to return to lobby"))
						} else {
							player.sendActionBar(Component.empty())
						}
					} else {
						when {
							slideN(0) -> {
								if (GameRunner.uhc.usingBot) {
									val linked = GameRunner.bot?.isLinked(player.uniqueId)

									if (linked == null || linked) tip(player, playerData)
									else player.sendActionBar(Component.text("${ChatColor.RED}${ChatColor.BOLD}You are not linked! ${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}\"%link [your minecraft username]\" ${ChatColor.GOLD}in discord"))
								} else tip(player, playerData)
							}
							slideN(1) -> {
								tip(player, playerData)
							}
							slideN(2) -> {
								if (team == null) tip(player, playerData)
								else player.sendActionBar(Component.text("${ChatColor.GOLD}Team name: ${team.colorPair.colorStringModified(team.displayName, ChatColor.BOLD)} ${ChatColor.GOLD}Use ${ChatColor.WHITE}${ChatColor.BOLD}/uhc name [name] ${ChatColor.GOLD}to set your team's name"))
							}
						}
					}
				}
			}
		}
	}

	fun prepareWorld(world: World, uhc: UHC) {
		Util.debug("${ChatColor.RED}PREPARING ${world.name}")
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
		world.difficulty = Difficulty.NORMAL

		if (WorldManager.isNonGameWorld(world)) {
			world.worldBorder.center = Location(world, 0.5, 0.0, 0.5)
			world.worldBorder.size = uhc.lobbyRadius * 2 + 1.0

			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
			world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)

			world.time = 6000
			world.isThundering = false
			world.setStorm(false)

			if (world.name == WorldManager.PVP_WORLD_NAME) {
				PvpArena.prepareArena(world, uhc.lobbyRadius, uhc)
			}
		}
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
