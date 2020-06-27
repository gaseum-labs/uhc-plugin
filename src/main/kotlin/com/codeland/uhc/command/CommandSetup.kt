package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.di
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.command.CommandSender
import org.kodein.di.instance
import java.util.logging.Level

@CommandAlias("uhc")
class CommandSetup : BaseCommand() {

    private val gameRunner: GameRunner by di.instance()

    @CommandAlias("setup")
    @Description("Setup the UHC round")
    fun onSetup(sender: CommandSender, startRadius: Double, endRadius: Double, shrinkTime: Double, graceTime: Double): Boolean {
        val uhc = UHC(startRadius, endRadius, shrinkTime, graceTime)

        gameRunner.setUhc(uhc)

        PaperPluginLogger.getGlobal().log(Level.INFO, "SETUP COMMAND WAS SENT")
        return true
    }
}