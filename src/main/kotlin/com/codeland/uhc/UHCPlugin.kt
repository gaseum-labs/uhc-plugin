package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.AdminCommands
import com.codeland.uhc.command.ParticipantCommands
import com.codeland.uhc.command.TeamMaker
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.Util
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.EventListener
import com.codeland.uhc.gui.GuiListener
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.phaseType.VariantList
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.URL


class UHCPlugin : JavaPlugin() {

	private val commandManager: PaperCommandManager by lazy { PaperCommandManager(this) }

	override fun onEnable() {
		commandManager.registerCommand(AdminCommands())
		commandManager.registerCommand(ParticipantCommands())

		server.pluginManager.registerEvents(EventListener(), this)
		server.pluginManager.registerEvents(GuiListener(), this)

		VariantList.create()

		var uhc = UHC(Preset.LARGE, arrayOf(
			PhaseVariant.WAITING_DEFAULT,
			PhaseVariant.GRACE_DEFAULT,
			PhaseVariant.SHRINK_DEFAULT,
			PhaseVariant.FINAL_DEFAULT,
			PhaseVariant.GLOWING_TOP_TWO,
			PhaseVariant.ENDGAME_CLEAR_BLOCKS,
			PhaseVariant.POSTGAME_DEFAULT
		))

		val bot = try {
			MixerBot.createMixerBot("./discordData.txt", "./linkData.txt")
		} catch (ex: Exception) {
			Util.log(ex.message ?: "unknown error")
			Util.log("BOT INIT FAILED | STARTING IN NO-BOT MODE")
			null
		}

		GameRunner(uhc, this, bot)

		TeamMaker.readData()

		server.scheduler.scheduleSyncDelayedTask(this) {
			if (server.scoreboardManager.mainScoreboard.getObjective("hp") == null) {
				server.scoreboardManager.mainScoreboard.registerNewObjective("hp", "health", "hp", RenderType.HEARTS)
			}
			server.scoreboardManager.mainScoreboard.getObjective("hp")!!.displaySlot = DisplaySlot.PLAYER_LIST

			Phase.createBossBars(Bukkit.getWorlds())

			GameRunner.uhc.updateDisplays()
			GameRunner.uhc.startWaiting()
		}
	}

	override fun onDisable() {
		commandManager.unregisterCommands()
	}
}
