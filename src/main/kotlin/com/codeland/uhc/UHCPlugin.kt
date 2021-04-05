package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.*
import com.codeland.uhc.team.TeamMaker
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.*
import com.codeland.uhc.phase.DimensionBar
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.VariantList
import com.codeland.uhc.util.Util
import com.codeland.uhc.util.WebAddress
import com.codeland.uhc.util.GoogleDDNSUpdater
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

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
		commandManager.registerCommand(ShareCoordsCommand(), true)
		commandManager.registerCommand(NicknameCommand(), true)

		Chat.loadFile()

		/* register all events */
		server.pluginManager.registerEvents(ClassesEvents(), this)
		server.pluginManager.registerEvents(Chat(), this)
		server.pluginManager.registerEvents(Crits(), this)
		server.pluginManager.registerEvents(EventListener(), this)
		server.pluginManager.registerEvents(Generation(), this)
		server.pluginManager.registerEvents(Portal(), this)
		server.pluginManager.registerEvents(PvpListener(), this)
		server.pluginManager.registerEvents(Brew(), this)
		server.pluginManager.registerEvents(Barter(), this)
		Packet.init()

		VariantList.create()

		val address = WebAddress.getLocalAddress()

		try {
			Util.log(GoogleDDNSUpdater.updateDomain(address))
		} catch (ex: Exception) {
			Util.log("$ex")
			Util.log("${ChatColor.RED}DDNS FAILED | STARTING SERVER AT $address")
		}

		GameRunner.bot = try {
			MixerBot.createMixerBot("./discordData.txt", "./linkData.txt", address)
		} catch (ex: Exception) {
			Util.log(ex.message ?: "unknown error")
			Util.log("${ChatColor.RED}BOT INIT FAILED | STARTING IN NO-BOT MODE")
			null
		}

		GameRunner.uhc = UHC(Preset.LARGE, arrayOf(
			PhaseVariant.WAITING_DEFAULT,
			PhaseVariant.GRACE_FORGIVING,
			PhaseVariant.SHRINK_DEFAULT,
			PhaseVariant.ENDGAME_NATURAL_TERRAIN,
			PhaseVariant.POSTGAME_DEFAULT
		))

		server.scheduler.scheduleSyncDelayedTask(this) {
			WorldManager.initWorlds()

			GameRunner.registerHearts()

			DimensionBar.createBossBars(Bukkit.getWorlds())

			GameRunner.uhc.updateDisplays()
			GameRunner.uhc.startWaiting()
		}
	}
	
	override fun onDisable() {
		Chat.saveFile()
	}
}
