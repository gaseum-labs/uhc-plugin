package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.CommandSetup
import com.codeland.uhc.core.GameRunner
import org.bukkit.plugin.java.JavaPlugin
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val di = DI {
    bind<GameRunner>() with singleton { GameRunner() }
}

class UHCPlugin : JavaPlugin() {

    private val commandManager: PaperCommandManager by lazy { PaperCommandManager(this) }

    override fun onEnable() {

        commandManager.registerCommand(CommandSetup())
    }

    override fun onDisable() {}
}