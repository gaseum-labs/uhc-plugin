package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.AdminCommands
import com.codeland.uhc.command.ParticipantCommands
import com.codeland.uhc.command.TeamMaker
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.EventListener
import com.codeland.uhc.gui.GuiListener
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.phaseType.VariantList
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.lang.Exception


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
			PhaseVariant.GRACE_FORGIVING,
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
			GameRunner.registerHearts()

			Phase.createBossBars(Bukkit.getWorlds())

			GameRunner.uhc.updateDisplays()
			GameRunner.uhc.startWaiting()
		}
	}

	override fun onDisable() {
		commandManager.unregisterCommands()
	}
}
