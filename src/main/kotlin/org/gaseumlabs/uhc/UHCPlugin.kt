package org.gaseumlabs.uhc

import co.aikar.commands.PaperCommandManager
import org.gaseumlabs.uhc.command.*
import org.gaseumlabs.uhc.core.ConfigFile
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.core.stats.Tracker
import org.gaseumlabs.uhc.database.DataManager
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
import kotlin.system.exitProcess

class UHCPlugin : JavaPlugin() {
	init {
		plugin = this
	}

	companion object {
		lateinit var plugin: JavaPlugin
		val configFile = ConfigFile.load()
	}

	override fun onEnable() {
		val commandManager = PaperCommandManager(this)

		Commands.registerCompletions(commandManager)

		commandManager.registerCommand(AdminCommands(), true)
		commandManager.registerCommand(TeamCommands(), true)
		commandManager.registerCommand(TestCommands(), true)
		commandManager.registerCommand(ParticipantCommands(), true)
		commandManager.registerCommand(ParticipantTeamCommands(), true)
		commandManager.registerCommand(ShareCoordsCommand(), true)
		commandManager.registerCommand(NicknameCommand(), true)

		/* register all events */
		server.pluginManager.registerEvents(ClassesEvents(), this)
		server.pluginManager.registerEvents(Chat(), this)
		server.pluginManager.registerEvents(EventListener(), this)
		server.pluginManager.registerEvents(Generation(), this)
		server.pluginManager.registerEvents(Portal(), this)
		server.pluginManager.registerEvents(PvpListener(), this)
		server.pluginManager.registerEvents(Brew(), this)
		server.pluginManager.registerEvents(Barter(), this)
		server.pluginManager.registerEvents(GuiManager(), this)
		server.pluginManager.registerEvents(Loot(), this)
		server.pluginManager.registerEvents(Enchant(), this)
		server.pluginManager.registerEvents(Parkour(), this)
		server.pluginManager.registerEvents(Snowball(), this)
		server.pluginManager.registerEvents(Fishing(), this)
		server.pluginManager.registerEvents(CaveIndicator(), this)
		server.pluginManager.registerEvents(ResourceEvents(), this)

		Packet.init()

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
				UHC.getConfig().usingBot.set(true)
				bot.loadIcon().thenAccept { image ->
					server.loadServerIcon(image)
				}

			}.exceptionally { ex ->
				Util.warn("}Bot setup failed")
				Util.warn(ex).void()
			}

		DataManager.createDataManager(configFile)
			.thenAccept { dataManager ->
				UHC.dataManager = dataManager
			}.exceptionally { ex ->
				Util.warn("Database connection failed")
				Util.warn(ex).void()
			}

		Tracker.loadCharacters()

		server.scheduler.scheduleSyncDelayedTask(this) {
			val initError = WorldManager.init()
			if (initError != null) {
				println(initError)
				exitProcess(3)
			}

			UHC.startLobby()
		}
	}

	override fun onDisable() {
		ArenaManager.saveWorldInfo(WorldManager.pvpWorld)
		UHC.bot?.stop()
	}
}
