package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.event.Enchant
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.quirk.quirks.classes.Classes
import org.gaseumlabs.uhc.quirk.quirks.classes.QuirkClass
import org.gaseumlabs.uhc.util.Action
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.BOLD
import org.bukkit.*
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.lobbyPvp.ArenaType.PARKOUR
import org.gaseumlabs.uhc.lobbyPvp.arena.*
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena.Platform
import org.gaseumlabs.uhc.world.WorldManager

@CommandAlias("uhc")
class ParticipantCommands : BaseCommand() {
	@Subcommand("gui")
	@Description("get the current setup as the gui")
	fun getCurrentSetupGui(sender: CommandSender) {
		UHC.getConfig().gui.open(sender as Player)
	}

	@Subcommand("pvp")
	fun openPvp(sender: CommandSender) {
		sender as Player

		if (
			PlayerData.isParticipating(sender.uniqueId) ||
			ArenaManager.playersArena(sender.uniqueId) is PvpArena
		) return Commands.errorMessage(sender, "You can't use this menu right now")

		PlayerData.getPlayerData(sender.uniqueId).lobbyPvpGui.open(sender)
	}

	@Subcommand("optOut")
	@Description("opt out from participating")
	fun optOutCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (playerData.participating) {
			Commands.errorMessage(sender, "You are already in the game!")

		} else if (playerData.optingOut) {
			Commands.errorMessage(sender, "You have already opted out!")

		} else {
			playerData.optingOut = true

			UHC.preGameTeams.leaveTeam(sender.uniqueId)

			Action.sendGameMessage(sender, "You have opted out of participating")
		}
	}

	@Subcommand("optIn")
	@Description("opt back into participating")
	fun optInCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (playerData.participating) {
			Commands.errorMessage(sender, "You are already in the game!")

		} else if (!playerData.optingOut) {
			Commands.errorMessage(sender, "You already aren't opting out!")

		} else {
			playerData.optingOut = false

			Action.sendGameMessage(sender, "You have opted back into participating")
		}
	}

	@Subcommand("compass")
	@Description("tell which direction a cave will be in based on the cave indicator block")
	fun compassCommand(sender: CommandSender) {
		sender as Player

		val block = sender.getTargetBlock(5)

		if (block == null) {
			Commands.errorMessage(sender, "You are not looking at a block")
		} else {
			val goodMessage = when (block.type) {
				Material.GRANITE -> "Granite indicates a cave to the north"
				Material.DIORITE -> "Diorite indicates a cave to the east"
				Material.ANDESITE -> "Andesite indicates a cave to the south"
				Material.TUFF -> "Tuff indicates a cave to the west"
				else -> null
			}

			if (goodMessage == null) {
				Commands.errorMessage(sender, "This block is not a cave indicator")
			} else {
				Action.sendGameMessage(sender, goodMessage)
			}
		}
	}

	@Subcommand("lobby")
	@Description("return to the lobby")
	fun lobbyCommand(sender: CommandSender) {
		sender as Player

		/* only non players can use this command */
		if (PlayerData.isParticipating(sender.uniqueId))
			return Commands.errorMessage(sender, "You can't use this command in game")

		/* forfeit */
		val arena = ArenaManager.playersArena(sender.uniqueId)
		if (arena is PvpArena && arena.playerIsAlive(sender))
			Action.sendGameMessage(sender, "You have forfeited")

		Lobby.onSpawnLobby(sender)
	}

	@Subcommand("spectate")
	fun spectate(sender: CommandSender) {
		sender as Player

		if (PlayerData.isParticipating(sender.uniqueId)) return

		val game = UHC.game

		if (game != null) {
			sender.gameMode = GameMode.SPECTATOR
			sender.setItemOnCursor(null)
			sender.inventory.clear()
			sender.teleport(game.spectatorSpawnLocation())

		} else {
			Commands.errorMessage(sender, "Game has not started!")
		}
	}

	@CommandCompletion("@quirkclass")
	@Subcommand("class")
	@Description("set your class for classes quirk")
	fun classCommand(sender: CommandSender, quirkClass: QuirkClass) {
		sender as Player

		val game = UHC.game ?: return Commands.errorMessage(sender, "Game has not started")

		val classes =
			game.getQuirk<Classes>(QuirkType.CLASSES) ?: return Commands.errorMessage(sender, "Classes are not enabled")

		if (classes.getClass(sender.uniqueId) != QuirkClass.NO_CLASS) {
			return Commands.errorMessage(sender, "You've already chosen a class")
		}

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(sender, "You must pick a class")

		val playerData = PlayerData.getPlayerData(sender.uniqueId)
		val oldClass = classes.getClass(playerData)

		/* always set their class, even during waiting */
		classes.setClass(sender.uniqueId, quirkClass)

		/* only start them if the game has already started */
		Classes.startAsClass(sender, quirkClass, oldClass)

		Action.sendGameMessage(sender, "Set your class to ${quirkClass.prettyName}")
	}

	@Subcommand("rename")
	@Description("rename a remote control in classes chc")
	fun renameCommand(sender: CommandSender, name: String) {
		sender as Player

		val game = UHC.game ?: return Commands.errorMessage(sender, "Game has not started")

		val classes =
			game.getQuirk<Classes>(QuirkType.CLASSES) ?: return Commands.errorMessage(sender, "Classes are not enabled")

		if (classes.getClass(sender.uniqueId) != QuirkClass.ENGINEER) return Commands.errorMessage(sender,
			"Your class can't use this command.")

		val control = Classes.remoteControls.find { (item, _, _) ->
			item == sender.inventory.itemInMainHand
		}
			?: return Commands.errorMessage(sender, "You're not holding a remote control.")

		control.displayName = name
		sender.inventory.setItemInMainHand(Classes.updateRemoteControl(control))
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tp")
	@Description("teleport to a player as a spectator")
	fun tpHereCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		sender as Player

		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (UHC.game != null && !playerData.participating && sender.gameMode == GameMode.SPECTATOR) {
			val location = Action.getPlayerLocation(toPlayer.uniqueId)

			if (location != null) {
				sender.teleport(location)
				Action.sendGameMessage(sender, "Teleported to ${toPlayer.name}")
			} else {
				Commands.errorMessage(sender, "Could not find player ${toPlayer.name}")
			}
		} else {
			Commands.errorMessage(sender, "You cannot teleport right now")
		}
	}

	/* lobby parkour */
	@Subcommand("parkour test")
	fun parkourTest(sender: CommandSender) {
		sender as Player

		val arena = ArenaManager.playersArena(sender.uniqueId) as? ParkourArena ?: return
		arena.enterPlayer(sender, sender.gameMode === GameMode.CREATIVE, false)
	}

	@Subcommand("parkour checkpoint")
	fun parkourCheckpoint(sender: CommandSender) {
		sender as Player

		val arena = ArenaManager.playersArena(sender.uniqueId) as? ParkourArena ?: return
		arena.enterPlayer(sender, true, true)
	}

	@Subcommand("parkour reset")
	fun parkourReset(sender: CommandSender) {
		sender as Player

		val arena = ArenaManager.playersArena(sender.uniqueId) as? ParkourArena ?: return

		val data = arena.getParkourData(sender.uniqueId)
		data.checkpoint = arena.start
		data.timer = 0
		data.timerGoing = false
		arena.enterPlayer(sender, true, true)
	}

	@Subcommand("enchantFix")
	fun enchantFixHelp(sender: CommandSender, enchantFixType: Enchant.EnchantType) {
		val names = enchantFixType.options.map { it.enchantment.key.key }

		sender.sendMessage(
			Component.text("<< Enchants for ", GOLD)
				.append(Component.text(enchantFixType.name, GOLD, BOLD))
				.append(Component.text(" >>", GOLD))
		)
		sender.sendMessage(
			Component.text("0  1  3  5  7  9  11 13 15", LIGHT_PURPLE, BOLD)
				.append(Component.text(" | Shelves", WHITE))
		)
		sender.sendMessage(Component.empty())

		enchantFixType.options.forEachIndexed { i, option ->
			var baseComponent = Component.empty()

			option.levels.forEach { level ->
				baseComponent = baseComponent.append(Component.text("$level  ", when (level) {
					0 -> BLACK
					1 -> RED
					2 -> GOLD
					3 -> YELLOW
					4 -> GREEN
					else -> AQUA
				}, BOLD))
			}

			sender.sendMessage(
				baseComponent.append(Component.text("| ", WHITE))
					.append(Component.text(names[i], BLUE))
			)
		}
	}

	@CommandCompletion("@uhcblockx @uhcblocky @uhcblockz @uhcblockx @uhcblocky @uhcblockz")
	@Subcommand("definePlatform")
	fun definePlatformCommand(
		sender: CommandSender,
		x0: Int,
		y0: Int,
		z0: Int,
		x1: Int,
		y1: Int,
		z1: Int,
		name: String,
	) {
		val player = sender as? Player ?: return

		val filteredName = name.trim()
		if (filteredName.length !in 3..36) return Commands.errorMessage(player,
			"Arena name must be 3 to 36 characters long")

		val arena = ArenaManager.playersArena(player.uniqueId) ?: return Commands.errorMessage(player,
			"You must be in your parkour arena")

		if (arena.type !== PARKOUR || (arena as ParkourArena).owner != player.uniqueId)
			return Commands.errorMessage(player, "You must be in your parkour arena")

		val world = player.world
		if (world !== WorldManager.pvpWorld) return Commands.errorMessage(player, "How did you get here")

		if (y0 != y1) return Commands.errorMessage(player, "Please only select one y layer")

		if (!arena.inBorder(x0, z0) || !arena.inBorder(x1, z1)) return Commands.errorMessage(player,
			"Please define a shape inside your border")

		val left = Math.min(x0, x1)
		val right = Math.max(x0, x1)
		val up = Math.min(z0, z1)
		val down = Math.max(z0, z1)

		val width = right - left + 1
		val height = down - up + 1

		if (width < 16 || height < 16) return Commands.errorMessage(player, "Min dimensions for arena is 16*16")
		if (width > 48 || height > 48) return Commands.errorMessage(player, "Max dimensions for arena is 48*48")

		val startPositions = ArrayList<Pair<Int, Int>>()

		val upperLayer = Array(width * height) { i ->
			val z = i / width
			val x = i % width

			val block = world.getBlockAt(left + x, y0 + 1, up + z)
			if (block.isPassable) block.blockData else Material.AIR.createBlockData()
		}

		val platformBlockDatas = Array(width * height) { i ->
			val z = i / width
			val x = i % width

			if ((left + x == x0 && up + z == z0) || (left + x == x1 && up + z == z1)) {
				Material.AIR.createBlockData()
			} else {
				val block = world.getBlockAt(left + x, y0, up + z)

				if (block.type === EMERALD_BLOCK) startPositions.add(x to z)

				block.blockData
			}
		}

		val lowerLayer = Array(width * height) { i ->
			val z = i / width
			val x = i % width

			val block = world.getBlockAt(left + x, y0 - 1, up + z)
			if (block.isPassable) block.blockData else Material.AIR.createBlockData()
		}

		if (startPositions.size < 4) return Commands.errorMessage(player,
			"Need at least 4 starting positions (Emerald block) (Not on corners)")

		val platform = Platform(
			player.name,
			filteredName,
			width,
			height,
			upperLayer,
			platformBlockDatas,
			lowerLayer,
			startPositions
		)

		GapSlapArena.submittedPlatforms[player.uniqueId] = platform

		Action.sendGameMessage(player, "Updated your gap slap arena")
	}
}
