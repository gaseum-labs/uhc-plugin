package com.codeland.uhc

import co.aikar.commands.BukkitCommandManager
import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.*
import com.codeland.uhc.team.TeamMaker
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.*
import com.codeland.uhc.phase.DimensionBar
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.VariantList
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.util.Util
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

		commandManager.registerCommand(AdminCommands(), true)
		commandManager.registerCommand(TeamCommands(), true)
		commandManager.registerCommand(TestCommands(), true)
		commandManager.registerCommand(ParticipantCommands(), true)
		commandManager.registerCommand(ShareCoordsCommand(), true)
		commandManager.registerCommand(NicknameCommand(), true)

		Chat.loadFile()

		/* register all events */
		server.pluginManager.registerEvents(Chat(), this)
		server.pluginManager.registerEvents(Crits(), this)
		server.pluginManager.registerEvents(EventListener(), this)
		server.pluginManager.registerEvents(Generation(), this)
		server.pluginManager.registerEvents(Portal(), this)
		server.pluginManager.registerEvents(PvpListener(), this)

		VariantList.create()

		GameRunner.bot = try {
			MixerBot.createMixerBot("./discordData.txt", "./linkData.txt")
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

		TeamMaker.readData()

		server.scheduler.scheduleSyncDelayedTask(this) {
			GameRunner.registerHearts()

			DimensionBar.createBossBars(Bukkit.getWorlds())

			GameRunner.uhc.updateDisplays()
			GameRunner.uhc.startWaiting()

			NameManager.initRecipes()
		}
	}
	
	override fun onDisable() {
		Chat.saveFile()
	}
}
