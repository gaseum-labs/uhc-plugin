package org.gaseumlabs.uhc

import co.aikar.commands.PaperCommandManager
import org.gaseumlabs.uhc.command.*
import org.gaseumlabs.uhc.database.summary.Tracker
import org.gaseumlabs.uhc.discord.MixerBot
import org.gaseumlabs.uhc.event.*
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.util.GoogleDDNSUpdater
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.Util.void
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.gen.WorldGenManager
import org.bukkit.plugin.java.JavaPlugin
import org.gaseumlabs.uhc.chc.chcs.banana.BananaManager
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.util.SchedulerUtil

class UHCPlugin : JavaPlugin() {
	init {
		plugin = this
	}

	companion object {
		lateinit var plugin: JavaPlugin
		val configFile = JsonFiles.load(ConfigFile::class.java, "./config.json")
		val uhcDbFile = JsonFiles.load(UHCDbFile::class.java, "./uhcdb.json")
	}

	override fun onEnable() {
		val commandManager = PaperCommandManager(this)

		Commands.registerCompletions(commandManager)

		commandManager.registerCommand(AdminCommands(), true)
		commandManager.registerCommand(TeamCommands(), true)
		commandManager.registerCommand(TestCommands(), true)
		commandManager.registerCommand(ParticipantCommands(), true)
		commandManager.registerCommand(ParkourCommands(), true)
		commandManager.registerCommand(ParticipantTeamCommands(), true)
		commandManager.registerCommand(ShareCoordsCommand(), true)
		commandManager.registerCommand(NicknameCommand(), true)
		commandManager.registerCommand(LinkCommands(), true)

		server.pluginManager.registerEvents(LobbyEvents(), this)
		server.pluginManager.registerEvents(GameEvents(), this)
		server.pluginManager.registerEvents(Chat(), this)
		server.pluginManager.registerEvents(Portal(), this)
		server.pluginManager.registerEvents(PvpListener(), this)
		server.pluginManager.registerEvents(Brew(), this)
		server.pluginManager.registerEvents(Barter(), this)
		server.pluginManager.registerEvents(GuiManager(), this)
		server.pluginManager.registerEvents(Loot(), this)
		server.pluginManager.registerEvents(Enchant(), this)
		server.pluginManager.registerEvents(Parkour(), this)
		server.pluginManager.registerEvents(Snowball(), this)
		server.pluginManager.registerEvents(CaveIndicator(), this)
		server.pluginManager.registerEvents(ResourceEvents(), this)
		server.pluginManager.registerEvents(TeamShield(), this)
		server.pluginManager.registerEvents(Fishing(), this)
		server.pluginManager.registerEvents(BananaManager, this)

		Packet.registerListeners()

		WorldGenManager.init(server)

		GoogleDDNSUpdater.updateDomain(configFile)
			.thenAccept(::println)
			.exceptionally { ex ->
				Util.warn("DDNS Failed")
				Util.warn(ex).void()
			}

		MixerBot.createMixerBot(configFile)
			.thenAccept { bot ->
				UHC.bot = bot
				UHC.getConfig().usingBot = true
				bot.loadIcon().thenAccept { image ->
					server.loadServerIcon(image)
				}

			}.exceptionally { ex ->
				Util.warn("Bot setup failed")
				Util.warn(ex).void()
			}

		Tracker.loadCharacters()

		SchedulerUtil.nextTick {
			WorldManager.init()
			UHC.start()
		}
	}

	override fun onDisable() {
		ArenaManager.saveWorldInfo(WorldManager.pvpWorld)
		UHC.bot?.stop()
	}
}
