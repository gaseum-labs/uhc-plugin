package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.AdminCommands
import com.codeland.uhc.command.ParticipantCommands
import com.codeland.uhc.command.ShareCoordsCommand
import com.codeland.uhc.command.TeamMaker
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.EventListener
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.VariantList
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import com.codeland.uhc.worldgen.ChunkGeneratorOverworld
import com.codeland.uhc.worldgen.OverworldGenSettings
import nl.rutgerkok.worldgeneratorapi.WorldGeneratorApi
import nl.rutgerkok.worldgeneratorapi.WorldRef
import org.bukkit.Bukkit
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin


class UHCPlugin : JavaPlugin() {
	init {
		plugin = this
	}

	companion object {
		lateinit var plugin: JavaPlugin
	}

	private val commandManager: PaperCommandManager by lazy { PaperCommandManager(this) }

	override fun onEnable() {
		commandManager.registerCommand(AdminCommands())
		commandManager.registerCommand(ParticipantCommands())
		commandManager.registerCommand(ShareCoordsCommand())

		server.pluginManager.registerEvents(EventListener(), this)

		VariantList.create()

		GameRunner.bot = try {
			MixerBot.createMixerBot("./discordData.txt", "./linkData.txt")
		} catch (ex: Exception) {
			Util.log(ex.message ?: "unknown error")
			Util.log("BOT INIT FAILED | STARTING IN NO-BOT MODE")
			null
		}

		GameRunner.uhc = UHC(Preset.LARGE, arrayOf(
			PhaseVariant.WAITING_DEFAULT,
			PhaseVariant.GRACE_FORGIVING,
			PhaseVariant.SHRINK_DEFAULT,
			PhaseVariant.ENDGAME_CLEAR_BLOCKS,
			PhaseVariant.POSTGAME_DEFAULT
		))

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

	/* will only get called if you modify bukkit.yml so don't worry */
	override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator? {
		val ref = WorldRef.ofName(worldName)

		return WorldGeneratorApi.getInstance(this, 1, 4).createCustomGenerator(ref) { worldGenerator ->
			val overworldSettings = OverworldGenSettings(this, ref)
			worldGenerator.setBaseNoiseGenerator(ChunkGeneratorOverworld(overworldSettings))
		}
	}
}
