package com.codeland.uhc.core

import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.lobbyPvp.PvpQueue
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.ChatColor.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

object Lobby {
	val LOBBY_RADIUS = 60

	fun lobbyLocation(uhc: UHC): Location {
		val world = WorldManager.getLobbyWorld()
		return Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
	}

	fun resetPlayerStats(player: Player) {
		player.exp = 0.0f
		player.totalExperience = 0
		player.level = 0
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.foodLevel = 20
		player.saturation = 5.0f
		player.fallDistance = 0f
		player.fireTicks = -1
		player.inventory.clear()
		player.setItemOnCursor(null)
		player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
		player.fallDistance = 0f
		player.setStatistic(Statistic.TIME_SINCE_REST, 0)
		player.isFlying = false
		player.isSneaking = false
	}

	fun onSpawnLobby(player: Player): Location {
		resetPlayerStats(player)

		player.gameMode = GameMode.CREATIVE

		/* reset applied status for all active quirks */
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		playerData.staged = false
		playerData.participating = false
		playerData.alive = false

		UHC.quirks.forEach { quirk ->
			if (quirk.enabled.get()) PlayerData.getQuirkDataHolder(playerData, quirk.type).applied = false
		}

		val lobbyLocation = lobbyLocation(UHC)
		if (player.location.world != WorldManager.getLobbyWorld()) player.teleport(lobbyLocation)

		CommandItemType.giveItem(CommandItemType.GUI_OPENER, player.inventory)
		CommandItemType.giveItem(CommandItemType.PVP_OPENER, player.inventory)
		CommandItemType.giveItem(CommandItemType.SPECTATE, player.inventory)

		return lobbyLocation
	}

	fun isLinked(player: Player) = if (UHC.usingBot.get()) {
		GameRunner.bot?.isLinked(player.uniqueId) ?: true
	} else true

	fun lobbyTipsTick(subTick: Int) {
		if (subTick % 20 == 0) {
			fun slideN(slide: Int, num: Int, time: Int) = (subTick / 20) % (num * time) < time * (slide + 1)
			fun isFirst(time: Int) = (subTick / 20) % time == 0

			fun tip(player: Player, playerData: PlayerData) {
				if (isFirst(6)) playerData.loadingTip = (Math.random() * loadingTips.size).toInt()

				player.sendActionBar(Component.text("${GOLD}UHC Tips: $WHITE$BOLD${loadingTips[playerData.loadingTip]}"))
			}

			Bukkit.getOnlinePlayers().forEach { player ->
				val playerData = PlayerData.getPlayerData(player.uniqueId)
				val team = TeamData.playersTeam(player.uniqueId)
				val queueTime = PvpQueue.queueTime(player.uniqueId)
				val game = PvpGameManager.playersGame(player.uniqueId)

				if (!playerData.participating && game == null) {
					if (queueTime != null) {
						val queueType = playerData.inLobbyPvpQueue.get()

						player.sendActionBar(Util.gradientString(
							"${if (queueType == PlayerData.PVP_QUEUE_1V1) "1v1" else "2v2"} | " +
							"Queue Time: ${Util.timeString(queueTime)} | " +
							"Players in Queue: ${PvpQueue.size(queueType)}",
							TextColor.color(0x750c0c), TextColor.color(0xeb1f0c)
						))

					} else if (player.gameMode == GameMode.SPECTATOR) {
						if (slideN(0, 3, 6)) {
							player.sendActionBar(Component.text("${GOLD}Use $WHITE$BOLD/uhc lobby ${GOLD}to return to lobby"))

						} else {
							player.sendActionBar(Component.empty())
						}
					} else if (!isLinked(player)) {
						player.sendActionBar(Component.text("$RED${BOLD}You are not linked! ${GOLD}Use $WHITE$BOLD\"%link [your minecraft username]\" ${GOLD}in discord"))

					} else if (team != null && team.name == null) {
						val warningColor = TextColor.color(if (slideN(0, 2, 1)) 0xFF0000 else 0xFFFFFF)

						val commandComponent = Component.text("\"/teamName [name]\"", warningColor, TextDecoration.BOLD)

						player.sendActionBar(
							Component.text("$RED${BOLD}Your team does not have a name! ${GOLD}Use").append(
								commandComponent.append(Component.text("${GOLD}to set your team's name"))
							)
						)

					} else {
						tip(player, playerData)
					}
				}
			}
		}
	}

	val loadingTips = arrayOf(
		/* generation */
		"Melons are hidden in the jungle brush and drop 1 melon slice when broken",
		"Sugar cane generates at the same rate in all biomes",
		"Sugar cane generates more often in surface water pools",
		"Sugar canes will always be 3 blocks tall",
		"All types of leaves blocks drop apples",
		"Oxeye daisies generate rarely in plains and flower forests",
		"Bone meal can be used in plains and flower forests to obtain oxeye daisies",
		"1 in 25 giant mushroom blocks drops a mushroom",
		"1 in 200 leaves blocks drops an apple",
		"Structures are turned off except for dungeons",
		"Enchanted golden apples cannot generate in dungeon chests",

		/* nether */
		"Each nether wart in the nether drops 1 nether wart",
		"Drive off a cliff in a Boat to negate fall damage",
		"Blazes and nether wart spawn naturally in every biome in the Nether",
		"You will never exit a nether portal outside of the border",
		"All players in the nether will be killed when endgame starts",
		"Portal coordinates are the same in the nether and overworld",
		"You can extinguish yourself in the nether using a cauldron and water bucket",
		"Zombified piglins will scare off piglins",
		"Use warped fungus to scare away hoglins",
		"Use warped fungus to lure striders onto land",
		"Mining nether quartz ore is a great source of experience",
		"Be mindful of random lava while strip mining in the nether",
		"You can fill glass bottles in the nether using a cauldron",

		/* brewing */
		"Remember to get sand for glass bottles",
		"Two blaze rods are required for brewing, one for a brewing stand and blaze powder to fuel it",
		"The duration of regeneration potions is reduced",
		"The duration of poison potions is reduced",
		"Brew glistering melon for instant health and ghast tear for regeneration",
		"Poison can be brewed into instant damage with a fermented spider eye",
		"Glowstone dust increases the level of a potion, while redstone dust increases the duration",
		"Weakness potions do not require nether wart",
		"Remember to kill creepers for gunpowder for splash potions",
		"Remember to save some sugar cane for speed potions",
		"Remember to save some gold nuggets for glistering melons",
		"Remember to save some brown mushrooms, sugar, and spider eyes for fermented spider eyes",
		"Remember to carry redstone dust for longer potion duration",

		/* combat */
		"axes disable shields for 5 seconds, at any charge",
		"splash instant healing or regeneration on you and your teammate to maximize your healing",
		"An arrow shot with the piercing enchantment hits through shields. Take cover!",
		"Firework stars are made with one gunpowder and one dye of any color",
		"Use paper, gunpowder, and firework Stars to make Firework Rockets",
		"You cannot critical hit while sprinting",
		"Arrows cannot critical hit. Random crits aren't fair and balanced",
		"Splash potion effects last longer the closer they hit to the target. Be accurate!",
		"Protect yourself from an end crystal explosion by standing beneath it",
		"You can see the health of other players in the tab menu",

		/* caves */
		"Diamond, lapis lazuli, and gold ores only generate in caves. Don't strip mine!",
		"Ore veins of diamond, lapis lazuli, and gold are guaranteed to be contiguous",
		"The generation of coal, iron, and redstone ores is not changed",
		"Extra brown and red mushrooms spawn in caves below Y level 32",
		"Granite found below Y level 32 indicates a cave to the north",
		"Diorite found below Y level 32 indicates a cave to the east",
		"Andesite found below Y level 32 indicates a cave to the south",
		"Tuff found below Y level 32 indicates a cave to the west",
		"The lava fill level in caves is lowered allowing deeper exploration",
		"Diamond ore veins will always have 3 diamond ore",
		"Gold ore veins will always have 5 gold ore",
		"Lapis lazuli ore veins will always have 4 lapiz lazuli ore",
		"Crawling from a trapdoor or water will let you strip mine faster",
		"Watch out for creepers falling from the top of ravines",
		"Mining redstone ore is useful for gaining experience and for brewing",
		"Remember to make a bucket and grab water before you go underground",

		/* commands */
		"Use '/sharecoords' to quickly tell your teammates where you are",
		"Use '/uhc gui' to see the current game setup",
		"Use '/uhc compass' to tell what direction cave indcators point in",
		"Use '@[username]' in chat to mention a player",
		"Use '/teamcolor' to change your team's color",
		"Use '/teamname' to change your team's name",
		"Prefix your message with '!' to talk in global chat when on a team",

		/* game flow */
		"The border will start damaging you immediately if you move outside it. Stay on the move!",
		"You cannot mine blocks outside of the border",
		"The boss bar will tell you how much time is left in the current phase",
		"Glowing is applied to all players every 15 seconds during endgame",
		"The reward for killing a player is regeneration for two hearts",
		"You cannot naturally regenerate health after grace period ends. Eat up!",
		"Final endgame vertical range will be at least 9 blocks",
		"Skybases slowly disappear in endgame",
		"The rising floor in endgame will push you up",
		"Two players can tie if they die in the same second",
		"You can respawn during grace period",
		"If you disconnect, a dummy zombie will be put in your place until you reconnect. No combat logging!",
		"The UHC game world has no oceans",
		"The UHC game world has two jungles located midway to the starting border",
		"You will be automatically moved into a voice call with your team. Communicate!",
		"The maximum hunger after grace period is reduced, allowing you to eat suspicious stew whenever",
		"Do not help living players as a spectator. No ghosting!",

		/* lobby */
		"Queue for 1v1 KitPVP with the Iron Sword in the Lobby",

		/* crafting */
		"2 spectral arrows are produced from 1 arrow and 4 glowstone dust. Double your money!",
		"suspicious stew crafted with an oxeye daisy gives regeneration for 1 heart",

		/* drops */
		"Spiders always drop 1 string",
		"A trident wielding drowned will always drop a full durability trident",
		"About every other chicken will drop a feather",
		"Cows, horses, donkeys, mules, and llamas each drop 1 leather when killed",
		"Skeletons cannot drop a bow. Get your own!",
		"Blazes will always drop 1 blaze rod",
		"Striders will always drop 3 string",
		"Witches will always drop a potion and various brewing ingredients",
		"Endermen will always drop 1 ender pearl",
		"Armor and weapon wielding Zombies will always drop their item at half durability",
		"Piglin bartering gives better loot",
		"The looting enchantment will always give better drops",

		/* mob spawning */
		"Baby zombies cannot spawn",
		"Each player has their own mob cap",
		"Mobs tend to spawn at your Y level",

		/* misc */
		"1 in 10 gravel drops flint",
		"You only need to mine the logs touching the leaves for them to start decaying",
		"Inventory management is key. Throw out your useless junk!",
		"roobley isn't real",
		"Dylan is a known cheater and should not be trusted",
		"Sneak to hide your location underground",
		"Leaves can be instantly mined with an iron hoe or with shears",
		"Hoes and shears have double durability",
		"Toggle sneak allows you to access block inventories while sneaking. Stay hidden!",
	)
}
