package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.AdminCommands
import com.codeland.uhc.command.ParticipantCommands
import com.codeland.uhc.command.TeamMaker
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.EventListener
import com.codeland.uhc.gui.GuiListener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class UHCPlugin : JavaPlugin() {

	private val commandManager: PaperCommandManager by lazy { PaperCommandManager(this) }

	override fun onEnable() {
		commandManager.registerCommand(AdminCommands())
		commandManager.registerCommand(ParticipantCommands())

		server.pluginManager.registerEvents(EventListener(), this)
		server.pluginManager.registerEvents(GuiListener(), this)

		var uhc = UHC(400.0, 25.0, 1200, 2250, 600, 0)

		val niceWebsite = URL("http://checkip.amazonaws.com")
		val `in` = BufferedReader(InputStreamReader(niceWebsite.openStream()))
		val ip = `in`.readLine().trim { it <= ' ' }

		GameRunner(uhc, this, MixerBot("discordData.txt", ip))

		TeamMaker.readData()

		server.scheduler.scheduleSyncDelayedTask(this) {
			if (server.scoreboardManager.mainScoreboard.getObjective("hp") == null) {
				server.scoreboardManager.mainScoreboard.registerNewObjective("hp", "health", "hp", RenderType.HEARTS)
			}
			server.scoreboardManager.mainScoreboard.getObjective("hp")!!.displaySlot = DisplaySlot.PLAYER_LIST
		}
	}

	override fun onDisable() {
		commandManager.unregisterCommands()
	}
}
