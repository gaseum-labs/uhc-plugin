package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.*
import com.codeland.uhc.core.ConfigFile
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.*
import com.codeland.uhc.gui.GuiManager
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.util.GoogleDDNSUpdater
import com.codeland.uhc.util.WebAddress
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.gen.WorldGenManager
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import kotlin.system.exitProcess

class UHCPlugin : JavaPlugin() {
	init {
		plugin = this
	}

	companion object {
		lateinit var plugin: JavaPlugin
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
		server.pluginManager.registerEvents(Axe(), this)
		server.pluginManager.registerEvents(Snowball(), this)
		server.pluginManager.registerEvents(Fishing(), this)
		server.pluginManager.registerEvents(CaveIndicator(), this)
		Packet.init()

		WorldGenManager.init(server)

		val configFile = ConfigFile.load()
		val address = WebAddress.getLocalAddress()

		if (configFile.production) println(try {
			GoogleDDNSUpdater.updateDomain(address)
		} catch (ex: Exception) {
			"${ex}\n${ChatColor.RED}DDNS FAILED"
		})

		MixerBot.createMixerBot(configFile, address, {
			UHC.bot = it
			UHC.getConfig().usingBot.set(true)
		}, {
			println("${ChatColor.RED}$it")
			println("${ChatColor.RED}BOT INIT FAILED | STARTING IN NO-BOT MODE")
		})

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
